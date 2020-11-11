package com.cryobot.commands.impl;

import com.cryobot.DiscordBot;
import com.cryobot.db.impl.MiscConnection;
import com.cryobot.entities.Command;
import com.cryobot.utils.Utilities;
import com.mysql.jdbc.StringUtils;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

import java.text.DecimalFormat;
import java.util.Optional;

public class InGameStatsCommands implements Command {

    public static String[] NAMES = {"Hat", "Cape", "Amulet", "Weapon", "Chest", "Shield", null, "Legs", null, "Gloves", "Feet", null, "Ring", "Arrows", "Aura"};

    @Override
    public int getPermissionsReq(String command) {
        return 0;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"get-levels", "get-level", "get-equip", "get-xp"};
    }

    public Optional<Emote> getEmoji(String name) {
        return DiscordBot.getInstance()
                .getJda()
                .getGuildById(MiscConnection.getLong("guild-id"))
                .getEmotes()
                .stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public void handleCommand(Message message, String command, String[] cmd) {
        String prefix = DiscordBot.getInstance().getProperties().getProperty("prefix");
        switch (cmd[0]) {
            case "get-levels":
                if (cmd.length < 2) {
                    message.getChannel().sendMessage("Invalid syntax. Correct usage: " + prefix + "get-levels (name)").queue();
                    return;
                }
                String name = command.substring(11);
                StringBuilder builder = new StringBuilder();
                builder.append("Stats for " + DiscordBot.getInstance().getHelper().getDisplayName(name) + ":\n\n");
                for (int i = 0; i < 25; i++) {
                    int level = DiscordBot.getInstance().getHelper().getLevel(name, i);
                    Optional<Emote> optional = getEmoji(Utilities.SKILL_NAME[i]);
                    if (level == -1) {
                        message.getChannel().sendMessage("Unable to load stats for that player.").queue();
                        return;
                    }
                    if (!optional.isPresent()) {
                        builder.append(Utilities.SKILL_NAME[i] + " - " + level);
                        if (i != 24) builder.append(", ");
                    } else
                        builder.append(optional.get().getAsMention() + " - " + level + " ");
                }
                message.getChannel().sendMessage(builder.toString()).queue();
                break;
            case "get-level":
                if (cmd.length < 3) {
                    message.getChannel().sendMessage("Invalid syntax. Correct usage: " + prefix + "get-level (skill) (name)").queue();
                    return;
                }
                int skill;
                try {
                    skill = Integer.parseInt(cmd[1]);
                } catch (Exception e) {
                    skill = Utilities.getSkill(cmd[1]);
                    if (skill == -1) {
                        message.getChannel().sendMessage("Invalid syntax. Correct usage: " + prefix + "get-level (skill) (name)").queue();
                        return;
                    }
                }
                name = command.replace("get-level " + cmd[1] + " ", "");
                String skillName = Utilities.SKILL_NAME[skill];
                int level = DiscordBot.getInstance().getHelper().getLevel(name, skill);
                if (level == -1)
                    message.getChannel().sendMessage("Unable to find stats on that player.").queue();
                else
                    message.getChannel().sendMessage(DiscordBot.getInstance().getHelper().getDisplayName(name) + "'s " + skillName + " level is " + level + ".").queue();
                break;
            case "get-equip":
                if (cmd.length < 2) {
                    message.getChannel().sendMessage("Invalid syntax. Correct usage: " + prefix + "get-equip (name)").queue();
                    return;
                }
                name = command.substring(14);
                builder = new StringBuilder();
                builder.append("Equipment for " + DiscordBot.getInstance().getHelper().getDisplayName(name) + ":\n\n");
                for (int i = 0; i < 15; i++) {
                    String equipName = DiscordBot.getInstance().getHelper().getEquip(name, i);
                    if (equipName == null) continue;
                    Optional<Emote> optional = getEmoji("equip" + i);
                    if (!optional.isPresent()) {
                        String slotName = NAMES[i];
                        if (slotName == null) continue;
                        builder.append(slotName + " - " + equipName);
                        if (i != 14) builder.append(", ");
                        continue;
                    }
                    builder.append(optional.get().getAsMention() + " - " + equipName + " ");
                }
                message.getChannel().sendMessage(builder.toString()).queue();
                break;
            case "get-xp":
                if (cmd.length < 2) {
                    message.getChannel().sendMessage("Invalid syntax. Correct usage: " + prefix + "get-xp (name)").queue();
                    return;
                }
                name = command.substring(7);
                if (StringUtils.isEmptyOrWhitespaceOnly(name)) {
                    message.getChannel().sendMessage("Invalid syntax. Correct usage: " + prefix + "get-xp (name)").queue();
                    return;
                }
                builder = new StringBuilder();
                builder.append("XP for " + DiscordBot.getInstance().getHelper().getDisplayName(name) + ":\n\n");
                DecimalFormat format = new DecimalFormat("#,###,###");
                for (int i = 0; i < 25; i++) {
                    double xp = DiscordBot.getInstance().getHelper().getXp(name, i);
                    Optional<Emote> optional = getEmoji(Utilities.SKILL_NAME[i]);
                    if (xp == -1) {
                        message.getChannel().sendMessage("Unable to load stats for that player.").queue();
                        return;
                    }
                    if (!optional.isPresent()) {
                        builder.append(Utilities.SKILL_NAME[i] + " - " + format.format(xp));
                        if (i != 24) builder.append(", ");
                    } else
                        builder.append(optional.get().getAsMention() + " - " + format.format(xp) + " ");
                }
                message.getChannel().sendMessage(builder.toString()).queue();
                break;
        }
    }
}
