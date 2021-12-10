package de.twoyang.telegram.bot.tb.functions;

import de.twoyang.telegram.bot.tb.helper.messages.TimedMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Collection;

/**
 * @author chrisotpher
 * @since 3/4/17
 */
public interface FunctionManager {

    /**
     * This function sends the given Message
     *
     * @param message the message to send
     * @return
     */
    Message send(SendMessage message);

    /**
     * adds a Function to the bot. The Function is active as it gets added
     *
     * @param function the Function to add and activate
     * @return whether the Function was added (may not be added if any other functions listens to the same commands)
     */
    boolean addFunction(BotFunction function);

    /**
     * shuts down the bot and warns all functions by calling their {@link BotFunction#shutdown()}
     */
    void shutdown();

    /**
     * Function to send Messages later
     *
     * @param message the TimedMessage to be send later
     */
    void sendTimed(TimedMessage message);

    void removeOutstandingMessages(Collection<TimedMessage> remToRemove);
}
