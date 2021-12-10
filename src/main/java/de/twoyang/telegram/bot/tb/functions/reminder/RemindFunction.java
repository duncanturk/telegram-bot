package de.twoyang.telegram.bot.tb.functions.reminder;

import de.twoyang.telegram.bot.tb.functions.BotFunctionImpl;
import de.twoyang.telegram.bot.tb.functions.FunctionManager;
import de.twoyang.telegram.bot.tb.helper.messages.SerializableTimedMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author chrisotpher
 * @since 3/4/17
 */
public class RemindFunction extends BotFunctionImpl {

    private Vector<SerializableTimedMessage> serializableTimedMessages = new Vector<>();
    private DateFormat dateTimeFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
    private DateFormat shortDateTimeFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    private File persistenceFile = new File("data/serializableTimedMessages");
    private static final int OPTION_LIST = 1, OPTION_COUNT = 2, OPTION_ALL = 4, OPTION_REMOVE = 8;

    @Override
    public void init(FunctionManager manager) {
        super.init(manager);
        load();
    }

    @Override
    public void upgradeFromVersion(int oldVersion) {
        switch (oldVersion) {
            case 0:
                // Version 0 saved ids as long, not as String --> failture on load
                File oldDataFile = new File("data/serializaableTimedMessages");
                if (oldDataFile.exists())
                    oldDataFile.delete();
                // Version 0 saved ids as long, not as String --> failture on load
                File oldDataFileForOutstandingMessages = new File("data/timedMessages");
                if (oldDataFileForOutstandingMessages.exists())
                    oldDataFileForOutstandingMessages.delete();
        }
    }

    @Override
    public int getCurrentVersion() {
        return 1;
    }

    @Override
    public String[] getCommand() {
        return new String[]{"/remind", "/r"};
    }

    private int getOptions(String s) {
        int options = 0;
        if (s.contains("a"))
            options |= OPTION_ALL;
        if (s.contains("c"))
            options |= OPTION_COUNT;
        if (s.contains("l"))
            options |= OPTION_LIST;
        if (s.contains("r"))
            options |= OPTION_REMOVE;
        return options;
    }

    @Override
    public void handle(Update update) {
        removeOverdueReminders();
        String optS = split(update.getMessage().getText())[1];
        int options = getOptions(optS.startsWith("-") ? optS : "");
        if (options == 0) {
            newReminder(update);
        } else if (options == (OPTION_COUNT | OPTION_ALL)) {
            countAllReminders(update);
        } else if (options == OPTION_COUNT) {
            countReminders(update);
        } else if (options == (OPTION_LIST | OPTION_ALL)) {
            listAllReminders(update);
        } else if (options == (OPTION_LIST)) {
            listReminders(update);
        } else if (options == OPTION_REMOVE) {
            removeReminder(update);
        }
    }

    private void removeOverdueReminders() {
        serializableTimedMessages.removeIf(SerializableTimedMessage::isOverdue);
    }

    private void removeReminder(Update update) {
        String msgText = update.getMessage().getText();
        String searchText = msgText.substring(msgText.indexOf(" ", msgText.indexOf(" ") + 1) + 1);
        Collection<SerializableTimedMessage> remToRemove = serializableTimedMessages.stream().filter(r -> r.getChatId() == update.getMessage().getChatId()).filter(r -> r.getText().toLowerCase().contains(searchText.toLowerCase())).collect(Collectors.toList());
        String message = reminderList(remToRemove.stream());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText("Folgende Erinnerungen werden entfernt:\n" + message);
        manager.send(sendMessage);
        serializableTimedMessages.removeAll(remToRemove);
        manager.removeOutstandingMessages(remToRemove.stream().map(SerializableTimedMessage::getTimedMessage).collect(Collectors.toList()));
        save();
    }

    private String reminderList(Stream<SerializableTimedMessage> reminderStream) {
        return reminderStream.map(e -> shortDateTimeFormat.format(e.getDueDate()) + ": " + e.getText()).reduce("", (s1, s2) -> s1 + "\n" + s2);
    }

    private void listReminders(Update update) {
        StringBuilder sb = new StringBuilder();
        if (serializableTimedMessages.size() > 0) {
            sb.append("Deine Erinnerungen:");
            sb.append(reminderList(serializableTimedMessages.stream().filter(serializableTimedMessage -> serializableTimedMessage.getChatId() == update.getMessage().getChatId())));
        } else
            sb.append("Du hast keine Erinnerungen..");
        SendMessage message0 = new SendMessage();
        message0.setChatId(String.valueOf(update.getMessage().getChatId()));
        message0.setText(sb.toString());
        manager.send(message0);
    }

    private void listAllReminders(Update update) {
        StringBuilder sb = new StringBuilder();
        if (serializableTimedMessages.size() > 0) {
            sb.append("Alle Erinnerungen:");
            for (SerializableTimedMessage r : serializableTimedMessages) {
                sb.append("\n  ");
                sb.append(dateTimeFormat.format(r.getDueDate()));
                sb.append(": ");
                if (update.getMessage().getChatId() == r.getChatId())
                    sb.append(r.getText());
                else sb.append("-");
            }
        } else
            sb.append("Es gibt keine Erinnerungen..");
        SendMessage message0 = new SendMessage();
        message0.setChatId(String.valueOf(update.getMessage().getChatId()));
        message0.setText(sb.toString());
        manager.send(message0);
    }

    private void countAllReminders(Update update) {
        SendMessage message0 = new SendMessage();
        message0.setChatId(String.valueOf(update.getMessage().getChatId()));
        message0.setText(serializableTimedMessages.size() + " Erinnerungen stehen insgesamt noch an");
        manager.send(message0);
    }

    private void countReminders(Update update) {
        SendMessage message0 = new SendMessage();
        message0.setChatId(String.valueOf(update.getMessage().getChatId()));
        message0.setText(serializableTimedMessages.stream().filter(serializableTimedMessage -> serializableTimedMessage.getChatId() == update.getMessage().getChatId()).count() + " Erinnerungen stehen für diesen Chat noch an");
        manager.send(message0);
    }

    private void newReminder(Update update) {
        String[] parts = split(update.getMessage().getText());
        // Regex zur Erkennung welches Zeitformat angegeben wurde
        Pattern relativeTimePattern = Pattern.compile("[\\d]+[smhdw]");
        Pattern absoluteTimePattern = Pattern.compile("([\\d]{0,2}[:])?[\\d]{1,2}([:][\\d]{0,2})?");
        Pattern absoluteDatePattern = Pattern.compile("([\\d]{0,2}[.])?[\\d]{1,2}([.]([\\d]{4})?)?");
        int textStartIndex = 1;
        long totalTimeToWait = 0;
        SerializableTimedMessage serializableTimedMessage = new SerializableTimedMessage();
        if (relativeTimePattern.matcher(parts[1]).matches()) {
            textStartIndex = parts.length;
            for (int i = 1; i < parts.length; i++) {
                if (relativeTimePattern.matcher(parts[i]).matches()) {
                    char unit = parts[i].charAt(parts[i].length() - 1);
                    long amount = Long.parseLong(parts[i].substring(0, parts[i].length() - 1));
                    switch (unit) {
                        case 'w':
                            amount *= 7;
                        case 'd':
                            amount *= 24;
                        case 'h':
                            amount *= 60;
                        case 'm':
                            amount *= 60;
                        case 's':
                            amount *= 1000;
                    }
                    totalTimeToWait += amount;
                } else {
                    textStartIndex = i;
                    break;
                }
            }
            serializableTimedMessage.setDueDate(new Date(System.currentTimeMillis() + totalTimeToWait));
        } else if (absoluteDatePattern.matcher(parts[1]).matches() || absoluteTimePattern.matcher(parts[1]).matches()) {
            if (absoluteTimePattern.matcher(parts[1]).matches() && absoluteDatePattern.matcher(parts.length > 2 ? parts[2] : "").matches()) {
                Calendar cal = Helper.getDate(parts[2]);
                serializableTimedMessage.setDueDate(Helper.getTime(parts[1], cal, false).getTime());
                textStartIndex = 3;
            } else if (absoluteTimePattern.matcher(parts.length > 2 ? parts[2] : "").matches() && absoluteDatePattern.matcher(parts[1]).matches()) {
                Calendar cal = Helper.getDate(parts[1]);
                serializableTimedMessage.setDueDate(Helper.getTime(parts[2], cal, false).getTime());
                textStartIndex = 3;
            } else if (absoluteDatePattern.matcher(parts[1]).matches()) {
                serializableTimedMessage.setDueDate(Helper.getDate(parts[1]).getTime());
                textStartIndex = 2;
            } else if (absoluteTimePattern.matcher(parts[1]).matches()) {
                serializableTimedMessage.setDueDate(Helper.getTime(parts[1]).getTime());
                textStartIndex = 2;
            } else
                serializableTimedMessage.setDueDate(new Date());
        } else {
            serializableTimedMessage.setDueDate(new Date());
            System.out.println("No time pattern found!" + Arrays.stream(parts).reduce("", (a, b) -> a + " " + b));
        }
        {
            StringBuilder sb = new StringBuilder();
            for (int i = textStartIndex; i < parts.length; i++) {
                sb.append(parts[i]);
                sb.append(" ");
            }
            String text = sb.toString();
            if (text.trim().isEmpty())
                text = "Deine Erinnerung war leer..";
            serializableTimedMessage.setText(text);
        }
        serializableTimedMessage.setChatId(update.getMessage().getChatId());
        serializableTimedMessages.add(serializableTimedMessage);
        save();
        SendMessage message0 = new SendMessage();
        message0.setChatId(String.valueOf(update.getMessage().getChatId()));
        message0.setText("Ich erinnere dich!\n" + shortDateTimeFormat.format(serializableTimedMessage.getDueDate()));
        manager.sendTimed(serializableTimedMessage.getTimedMessage());
        manager.send(message0);
    }

    @Override
    public String getDescription() {
        return "" +
                "Mit /r können Sie Erinnerungen erstellen und verwalten.\n" +
                " Syntax: /r [-parameter] [timer] [Nachricht]\n" +
                " Parameter:\n" +
                "  a - Aktion auf alle Erinnerungen beziehen\n" +
                "  c - zählen von Erinnerungen\n" +
                "  l - listen von Erinnerungen\n" +
                "  Die Reihenfolge dieser Parameter ist egal (-la=-al)\n" +
                " Erstellen einer Erinnerung:\n" +
                "  Syntax: /r [timer] [Nachricht]\n" +
                "  Beispiel: \"/r 1w 2d 3h 4m 5s Hallo!\" erinnert dich in einer Woche, 2 Tagen, 3 Stunden, 4 Minuten und 5 Sekunden\n" +
                " Listen von Erinnerungen:\n" +
                "  Bsp.: \"/r -la\" listet alle ausstehenden Erinnerungen\n" +
                "        \"/r -l\" listet ausstehende Erinnerungen im aktuellen Chat\n" +
                " Zählen von Erinnerungen:\n" +
                "  Bsp.: \"/r -ca\" zählt alle ausstehenden Erinnerungen\n" +
                "        \"/r -c\" zählt die im aktuellen Chat ausstehenden Erinnerungen\n";
    }

    @Override
    public void shutdown() {
        save();
    }

    private void save() {
        try {
            ObjectOutputStream dos = new ObjectOutputStream(new FileOutputStream(persistenceFile));
            dos.writeUnshared(serializableTimedMessages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (persistenceFile.exists())
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(persistenceFile));
                //noinspection unchecked
                serializableTimedMessages = (Vector<SerializableTimedMessage>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
    }

    private String[] split(String messageText) {
        return messageText.trim().split("[\\s]+");
    }
}
