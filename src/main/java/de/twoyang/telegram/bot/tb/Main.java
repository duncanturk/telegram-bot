package de.twoyang.telegram.bot.tb;

import de.twoyang.telegram.bot.tb.functions.reminder.RemindFunction;
import de.twoyang.telegram.bot.tb.helper.config.Config;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;

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
            } else {
                String token = Config.getConfig("TGBOT_BOT_TOKEN"),
                        username = Config.getConfig("TGBOT_BOT_USERNAME");
                if (token == null || username == null) {
                    System.out.println("TGBOT_BOT_USERNAME and TGBOT_BOT_TOKEN must be set");
                    System.exit(1);
                }
                bob.setBotToken(token);
                bob.setBotUsername(username);
            }
            bob.addFunction(new RemindFunction());
            bob.start();
            botsApi.registerBot(bob);
        } catch (TelegramApiException | IOException e) {
            e.printStackTrace();
        }
    }
}