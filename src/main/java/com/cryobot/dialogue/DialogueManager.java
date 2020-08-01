package com.cryobot.dialogue;

import com.cryobot.DiscordBot;
import com.cryobot.dialogue.impl.GuessThatItemDialogue;
import com.cryobot.dialogue.impl.GuessThatPlaceDialogue;
import com.cryobot.dialogue.impl.SetupDialogue;
import com.cryobot.dialogue.impl.TriviaDialogue;
import com.cryobot.entities.Dialogue;
import com.cryobot.entities.Game;
import com.cryobot.entities.Trivia;
import com.cryobot.games.impl.GuessThatItemGame;
import com.cryobot.games.impl.GuessThatPlaceGame;
import com.cryobot.utils.Utilities;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class DialogueManager {

    private HashMap<String, Class<Dialogue>> dialogues;
    private HashMap<Long, Dialogue> conversations;

    public DialogueManager() {
        dialogues = new HashMap<>();
        conversations = new HashMap<>();
    }

    public void startConversation(long id, String dialogueName, String... parameters) {
        if (!dialogues.containsKey(dialogueName)) return;
        if (conversations.containsKey(id)) {
            Dialogue dialogue = conversations.get(id);
            dialogue.end();
        }
        Class<Dialogue> dialogueClass = dialogues.get(dialogueName);
        try {
            Constructor constructor = dialogueClass.getConstructor(new Class[]{long.class});
            Dialogue dialogue = (Dialogue) constructor.newInstance(new Object[]{id});
            conversations.put(id, dialogue);
            dialogue.start(parameters);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void endConversation(long id) {
        conversations.remove(id);
    }

    public void continueConversation(PrivateChannel channel, Message message, long id, String response) {
        if (!conversations.containsKey(id)) return;
        String res[] = response.split(" ");
        conversations.get(id).run(channel, message, response, res);
    }

    public Dialogue getConversation(long id) {
        if (!conversations.containsKey(id)) return null;
        return conversations.get(id);
    }

    public String[] getDialogueNameList() {
        return dialogues.keySet().toArray(new String[dialogues.size()]);
    }

    private static Class[] DIALOGUES = {GuessThatItemDialogue.class, GuessThatPlaceDialogue.class, SetupDialogue.class, TriviaDialogue.class};

    public void load() {
        try {
            for (Class<Dialogue> c : DIALOGUES) {
                if (!Dialogue.class.isAssignableFrom(c)) continue;
                Constructor constructor = c.getConstructor(new Class[]{long.class});
                Dialogue dialogue = (Dialogue) constructor.newInstance(new Object[]{0L});
                dialogues.put(dialogue.getName(), c);
            }
            ArrayList<Dialogue> extra = DiscordBot.getInstance().getHelper().getExtraDialogues();
            if(extra == null || extra.size() == 0) return;
            for(Dialogue dialogue : extra) {
                if(dialogues.containsKey(dialogue.getName())) {
                    System.err.println("[DialogueManager]Duplicate dialogue exists: "+dialogue.getName());
                    continue;
                }
                dialogues.put(dialogue.getName(), (Class<Dialogue>) dialogue.getClass());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
