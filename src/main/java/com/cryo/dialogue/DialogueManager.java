package com.cryo.dialogue;

import com.cryo.entities.Dialogue;
import com.cryo.utils.Utilities;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    public void load() {
        try {
            for (Class<Dialogue> c : Utilities.getClasses("com.cryo.dialogue.impl")) {
                if (!Dialogue.class.isAssignableFrom(c)) continue;
                Constructor constructor = c.getConstructor(new Class[]{long.class});
                Dialogue dialogue = (Dialogue) constructor.newInstance(new Object[]{0L});
                dialogues.put(dialogue.getName(), c);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
