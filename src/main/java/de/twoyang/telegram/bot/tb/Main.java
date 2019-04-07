package de.twoyang.telegram.bot.tb;

import de.twoyang.telegram.bot.tb.functions.reminder.RemindFunction;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * @author chrisotpher
 * @since 2/24/17
 */
public class Main {
    public static void main(String[] args) {

        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            Bot bob = new Bot();
            // if there are 2 or more arguments use them
            if (args.length >= 2) {
                bob.setBotUsername(args[0]);
                bob.setBotToken(args[1]);
            }
            bob.addFunction(new RemindFunction());
            bob.start();
            botsApi.registerBot(bob);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}