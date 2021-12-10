package de.twoyang.telegram.bot.tb;

import de.twoyang.telegram.bot.tb.functions.BotFunction;
import de.twoyang.telegram.bot.tb.functions.FunctionManager;
import de.twoyang.telegram.bot.tb.helper.messages.SerializableTimedMessage;
import de.twoyang.telegram.bot.tb.helper.messages.TimedMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


/**
 * @author chrisotpher
 * @since 2/24/17
 */
public class Bot extends TelegramLongPollingBot implements FunctionManager {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm:ss");
    private ArrayList<BotFunction> functions = new ArrayList<>();
    private ReadWriteLock rwOutstandingLock = new ReentrantReadWriteLock();
    private Lock rOutstandingMessagesLock = rwOutstandingLock.readLock();
    private Lock wOutstandingMessagesLock = rwOutstandingLock.writeLock();
    private ArrayList<TimedMessage> outstandingMessages = new ArrayList<>();
    private HashMap<String, BotFunction> functionCommands = new HashMap<>();
    private String botToken, botUsername;
    private Runnable outstandingMessageSender = () -> {
        rOutstandingMessagesLock.lock();
        List<TimedMessage> messagesToRemove = outstandingMessages.stream().filter(TimedMessage::isOverdue).collect(Collectors.toList());
        if (messagesToRemove.size() != 0) System.out.println(messagesToRemove.size() + " Messages to remeove");
        messagesToRemove.stream().map(TimedMessage::getSendMessage).forEach(this::send);
        rOutstandingMessagesLock.unlock();
        removeOutstandingMessages(messagesToRemove);
    };
    private File timedMessagesFile = new File("data/timedMessages");

    @Override
    public boolean addFunction(BotFunction function) {
        for (String cmd : function.getCommand()) {
            if (functionCommands.containsKey(cmd))
                return false;
        }
        for (String cmd : function.getCommand()) {
            functionCommands.put(cmd, function);
        }
        functions.add(function);
        function.init(this);
        return true;
    }

    void start() {
        load();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                outstandingMessageSender.run();
            }
        }).start();
    }

    @Override
    public void shutdown() {
        //functions.forEach(BotFunction::shutdown);
        for (BotFunction function : functions)
            function.shutdown();
        System.exit(0);
    }

    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("===============================================================================");
            System.out.println(sdf.format(new Date()) + ": Usr: " + update.getMessage().getText());

            if (update.getMessage().getText().startsWith("/start")) {
                startFunction(update);
            } else {
                BotFunction func;
                if ((func = functionCommands.get(update.getMessage().getText().split(" ")[0])) != null) {
                    func.handle(update);
                }
            }
        } else if (update.hasCallbackQuery()) {
            BotFunction func;
            if ((func = functionCommands.get(update.getCallbackQuery().getData().split(" ")[0])) != null) {
                func.handle(update);
            }
        }
    }

    private void startFunction(Update update) {
        StringBuilder sb = new StringBuilder("");
        for (BotFunction function : functions) {
            sb.append(function.getDescription());
            sb.append("\n=======\n");
        }
        if (sb.length() > 0)
            sb.substring(0, sb.length() - "\n=======\n".length());
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(update.getMessage().getChatId()));
        message.setText(sb.toString());
        send(message);
    }

    void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }

    public String getBotUsername() {
        return botUsername;
    }

    void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void sendTimed(TimedMessage message) {
        wOutstandingMessagesLock.lock();
        outstandingMessages.add(message);
        wOutstandingMessagesLock.unlock();
        save();
    }

    @Override
    public void removeOutstandingMessages(Collection<TimedMessage> remToRemove) {
        wOutstandingMessagesLock.lock();
        boolean changed = outstandingMessages.removeAll(remToRemove);
        wOutstandingMessagesLock.unlock();
        if (changed)
            save();
    }



    public Message send(SendMessage m) {
        Message m1 = null;
        try {
            m1 = execute(m);
            System.out.println(sdf.format(new Date()) + ": Bot: " + m1.getText());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return m1;
    }

    private void save() {
        try {
            ObjectOutputStream dos = new ObjectOutputStream(new FileOutputStream(timedMessagesFile));
            dos.writeUnshared(outstandingMessages.stream().map(TimedMessage::getSerializable).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (timedMessagesFile.exists())
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(timedMessagesFile));
                //noinspection unchecked
                outstandingMessages.addAll(((ArrayList<SerializableTimedMessage>) ois.readObject()).stream().map(SerializableTimedMessage::getTimedMessage).collect(Collectors.toList()));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
    }
}
