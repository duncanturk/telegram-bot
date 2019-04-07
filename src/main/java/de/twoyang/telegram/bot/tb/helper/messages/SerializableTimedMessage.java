package de.twoyang.telegram.bot.tb.helper.messages;

import de.twoyang.telegram.bot.tb.helper.Helper;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chrisotpher on 3/5/17.
 */
public class SerializableTimedMessage implements Serializable {
    private String text;
    private long chatId;
    private String id;
    private Date dueDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SerializableTimedMessage() {
        super();
        id = Helper.getUniqueId();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getDueDate() {
        if (dueDate == null)
            return new Date(0);
        return dueDate;
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.before(new Date());
    }

    public TimedMessage getTimedMessage() {
        return new TimedMessage(getDueDate(), new SendMessage().setText(getText()).setChatId(getChatId()), getId());
    }
}
