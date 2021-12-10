package de.twoyang.telegram.bot.tb.functions;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Interface which every Funcation of the Bot should implement.
 * An implementation of this interface should handle all messages starting with the same command.
 * Created by chrisotpher on 3/4/17.
 */
public interface BotFunction {

    /**
     * This function is called when the Function is added to the bot.
     *
     * @param manager the Manager which handles this Function (used for sending Messages, requesting shutdowns, etc.)
     */
    default void init(FunctionManager manager) {
        setManager(manager);
        if (needUpgrade())
            upgradeFromVersion(getVersionLastRun());
        setVersionLastRun(getCurrentVersion());
    }

    /**
     * This Function gets called when ist necessary to upgrade from an older Version (determined by getCurrentVersion() > getVersionLastRun())
     *
     * @param oldVersion the version the Bot was run with the last time
     */
    void upgradeFromVersion(int oldVersion);

    /**
     * used to determine the Version this BotFunction had the last time the Bot was started
     *
     * @return as described
     */
    int getVersionLastRun();

    /**
     * @param version the version the versionLastRun should be set to
     * @see BotFunction#getVersionLastRun()
     */
    void setVersionLastRun(int version);

    /**
     * @return the current version of this BotFunction, usually hardcoded, increased at every change made to this Function
     */
    int getCurrentVersion();

    /**
     * @return whether an upgrade is needed (when the {@link BotFunction#getCurrentVersion()} returns an newer Version than {@link BotFunction#getVersionLastRun()})
     */
    default boolean needUpgrade() {
        return getCurrentVersion() > getVersionLastRun();
    }

    /**
     * stops the Function
     */
    default void shutdown() {
    }

    /**
     * @return all commands the Function listens to
     */
    String[] getCommand();

    /**
     * called when the Function should handle a Update
     *
     * @param update the update to be handled
     */
    void handle(Update update);

    /**
     * @return a short description what this Function does.
     */
    String getDescription();

    void setManager(FunctionManager manager);
}
