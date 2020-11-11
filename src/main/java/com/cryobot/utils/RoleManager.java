package com.cryobot.utils;

import com.cryobot.DiscordBot;
import com.cryobot.db.impl.AccountConnection;
import com.cryobot.db.impl.GamesConnection;
import com.cryobot.db.impl.MiscConnection;
import com.cryobot.db.impl.RolesConnection;
import com.cryobot.entities.Role;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RoleManager {

    private HashMap<Long, Role> roles;

    public RoleManager() {
        roles = new HashMap<>();
    }

    public void load() {
        Object[] data = RolesConnection.connection().handleRequest("get-roles");
        if (data != null) roles = (HashMap<Long, Role>) data[0];
    }

    public void saveRole(Role role) {
        saveRole(role, false);
    }

    public void saveRole(Role role, boolean overwrite) {
        long guildId = MiscConnection.getLong("guild-id");
        if (guildId == 0L) return;
        if (overwrite) {
            deleteRolesWithSameName(role);
            List<Role> toRemove = roles.values()
                    .stream()
                    .filter(r -> r.getName().equalsIgnoreCase(role.getName()))
                    .collect(Collectors.toList());

            toRemove.forEach(r -> {
                RolesConnection.connection().handleRequest("remove-role", r.getId());
                roles.remove(r.getRoleId());
            });
        }
        Guild guild = DiscordBot.getInstance().getJda().getGuildById(guildId);
        RoleAction action = guild.createRole();
        if (role.isUsingColour())
            action.setColor(Color.decode(role.getRoleColour()));
        net.dv8tion.jda.api.entities.Role newRole = action.setName(role.getName())
                .setMentionable(role.isMentionableByAnyone())
                .setHoisted(role.isDisplaySeparately())
                .complete();
        if (newRole == null) return;
        role.setRoleId(newRole.getIdLong());
        roles.put(newRole.getIdLong(), role);
        RolesConnection.connection().handleRequest("add-role", role);
    }

    public void editRole(Role role) {
        long guildId = MiscConnection.getLong("guild-id");
        if (guildId == 0L) return;
        Guild guild = DiscordBot.getInstance().getJda().getGuildById(guildId);
        net.dv8tion.jda.api.entities.Role existing = guild.getRoleById(role.getRoleId());
        existing.getManager().setHoisted(role.isDisplaySeparately()).queue();
        existing.getManager().setMentionable(role.isMentionableByAnyone()).queue();
        if(role.isUsingColour())
            existing.getManager().setColor(Color.decode(role.getRoleColour()));
        roles.put(role.getRoleId(), role);
        RolesConnection.connection().handleRequest("remove-role", role.getId());
        RolesConnection.connection().handleRequest("add-role", role);
    }

    public void roleDeleted(long id) {
        if (!roles.containsKey(id)) return;
        Role role = roles.get(id);
        RolesConnection.connection().handleRequest("remove-role", role.getId());

    }

    public void recheckRoles(long id) {
        User user = DiscordBot.getInstance().getJda().getUserById(id);
        if (user != null) recheckRoles(user);
    }

    public void recheckRoles(User user) {
        int points = GamesConnection.getPoints(user.getIdLong());
        Object[] data = GamesConnection.connection().handleRequest("get-points-leader");
        boolean isPointsLeader = false;
        if (data != null && (long) data[1] == user.getIdLong()) isPointsLeader = true;
        for (Role role : roles.values()) {
            if (role.isGiveToPointsLeader() && isPointsLeader && !hasRole(user, role) && points > 0) {
                giveRole(user, role);
                checkPointsLeader(role, points);
            }
            if (role.isGiveOnPointsRequirement()) {
                int totalPoints = GamesConnection.getTotalPoints(user.getIdLong());
                if (totalPoints >= role.getPointsRequirement() && !hasRole(user, role))
                    giveRole(user, role);
            }
            if (role.isGiveOnCurrentPointsRequirement()) {
                int pointsReq = role.getCurrentPointsRequirement();
                if (points >= pointsReq && !hasRole(user, role))
                    giveRole(user, role);
                else if (points < pointsReq && hasRole(user, role))
                    removeRole(user, role);
            }
            if (role.isGiveToInGameStatus()) {
                data = AccountConnection.connection().handleRequest("get-username", user.getIdLong());
                if (data != null) {
                    String username = (String) data[0];
                    String[] statuses = DiscordBot.getInstance().getHelper().getStatuses(username);
                    if (ArrayUtils.contains(statuses, role.getInGameStatus()) && !hasRole(user, role))
                        giveRole(user, role);
                }
            }
        }
    }

    public void checkPointsLeader(Role role, int points) {
        Guild guild = DiscordBot.getInstance().getJda().getGuildById(MiscConnection.getLong("guild-id"));
        net.dv8tion.jda.api.entities.Role r = guild.getRoleById(role.getRoleId());
        List<Member> list = guild.getMembersWithRoles(r);
        list.forEach(m -> {
            int memberPoints = GamesConnection.getPoints(m.getUser().getIdLong());
            if (memberPoints < points)
                guild.removeRoleFromMember(m, r);
        });
    }

    public void recheckAllRoles() {
        Object[] data = AccountConnection.connection().handleRequest("get-all-discord-ids");
        ArrayList<Long> list = (ArrayList<Long>) data[0];
        for (long id : list) {
            User user = DiscordBot.getInstance().getJda().getUserById(id);
            if (user != null) recheckRoles(user);
        }
    }

    public void deleteRole(Role role) {
        net.dv8tion.jda.api.entities.Role r = DiscordBot.getInstance().getJda().getGuildById(MiscConnection.getLong("guild-id")).getRoleById(role.getRoleId());
        if (r != null) r.delete().queue();
    }

    public void deleteRolesWithSameName(Role role) {
        Guild guild = DiscordBot.getInstance().getJda().getGuildById(MiscConnection.getLong("guild-id"));
        guild.getRoles()
                .stream()
                .filter(r -> r.getName().equalsIgnoreCase(role.getName()))
                .forEach(r -> r.delete().queue());
    }

    public void giveRole(User user, Role role) {
        long guildId = MiscConnection.getLong("guild-id");
        if (guildId == 0L) return;
        Guild guild = DiscordBot.getInstance().getJda().getGuildById(guildId);
        net.dv8tion.jda.api.entities.Role r = guild.getRoleById(role.getRoleId());
        if (r == null) return;
        Member member = guild.getMember(user);
        if (member == null) return;
        guild.addRoleToMember(member, r).queue();
        //Notes - /If the role you're trying to assign is higher in roles list than DiscordHelper,
        // you will receive an error as the bot cannot modify a role higher than it's own

    }

    public void removeRole(User user, Role role) {
        long guildId = MiscConnection.getLong("guild-id");
        if (guildId == 0L) return;
        Guild guild = DiscordBot.getInstance().getJda().getGuildById(guildId);
        net.dv8tion.jda.api.entities.Role r = guild.getRoleById(role.getRoleId());
        if (r == null) return;
        Member member = guild.getMember(user);
        if (member == null) return;
        guild.removeRoleFromMember(member, r).queue();
    }

    public boolean roleNameExists(String name) {
        Guild guild = DiscordBot.getInstance().getJda().getGuildById(MiscConnection.getLong("guild-id"));
        return guild.getRolesByName(name, true).size() > 0;
    }

    public net.dv8tion.jda.api.entities.Role getRole(String name) {
        if(!roleNameExists(name)) return null;
        Guild guild = DiscordBot.getInstance().getJda().getGuildById(MiscConnection.getLong("guild-id"));
        return guild.getRolesByName(name, true).get(0);
    }

    public boolean hasRole(User user, Role role) {
        Member member = DiscordBot.getInstance().getJda().getGuildById(MiscConnection.getLong("guild-id")).getMember(user);
        if (member == null) return false;
        return member.getRoles().stream().filter(r -> r.getIdLong() == role.getRoleId()).count() > 0;
    }

    public MessageEmbed getRolesEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Roles");
        if (roles.size() == 0)
            return builder.setDescription("No roles detected. Follow the dialogue to add some.").build();
        for (Role role : roles.values())
            builder.addField(role.getName(), role.getDescription(), false);
        return builder.build();
    }

    public MessageEmbed getRoleGiveOptionsEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Role Given Options");
        builder.addField("1 - Given on in-game rank change", "Given when a user's in-game rank changes and they have their account's linked. Or when they link their account.", false);
        builder.addField("2 - Given on # of points earned", "Given when a user has gained a 'total' # of points. (Regardless of if they've spent them or not.)", false);
        builder.addField("3 - Given on # of points currently", "Given when a user has a current # of points. Removed when they have less than # of points", false);
        builder.addField("4 - Given to points leader", "Given to the user with the highest # of points. Removed when a new user becomes points leader.", false);
        return builder.build();
    }
}
