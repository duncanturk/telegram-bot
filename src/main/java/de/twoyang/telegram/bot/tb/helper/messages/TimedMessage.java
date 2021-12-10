package de.twoyang.telegram.bot.tb.helper.messages;

import de.twoyang.telegram.bot.tb.helper.Helper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Date;

/**
 * Created by Christopher on 06.04.2017.
 */
public class TimedMessage {
    private Date dueDate;
    private SendMessage sendMessage;
    private String id;

    public TimedMessage() {
        id = Helper.getUniqueId();
    }

    public TimedMessage(Date dueDate) {
        this();
        this.dueDate = dueDate;
    }

    public TimedMessage(Date dueDate, SendMessage sendMessage) {
        this(dueDate);
        this.sendMessage = sendMessage;
    }

    public TimedMessage(Date dueDate, SendMessage sendMessage, String timedMessageId) {
        this(dueDate, sendMessage);
        setId(timedMessageId);
    }

    public boolean isOverdue() {
        if (dueDate.before(new Date()))
            return true;
        return false;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public SendMessage getSendMessage() {
        return sendMessage;
    }

    public void setSendMessage(SendMessage sendMessage) {
        this.sendMessage = sendMessage;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object j) {
        if (j instanceof TimedMessage)
            return getId().equals(((TimedMessage) j).getId());
        return false;
    }

    public SerializableTimedMessage getSerializable() {
        SerializableTimedMessage seri = new SerializableTimedMessage();
        seri.setChatId(Long.valueOf(getSendMessage().getChatId()));
        seri.setText(getSendMessage().getText());
        seri.setDueDate(getDueDate());
        seri.setId(getId());
        return seri;
    }
}
