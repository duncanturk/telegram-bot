package de.twoyang.telegram.bot.tb.functions;

import java.io.*;
import java.net.URLEncoder;

/**
 * @author chrisotpher
 * @since 3/4/17
 */
public abstract class BotFunctionImpl implements BotFunction {
    protected FunctionManager manager;

    @Override
    public void setManager(FunctionManager manager) {
        this.manager = manager;
    }

    @Override
    public int getVersionLastRun() {
        File versionFile = getVersionFile();
        try {
            @SuppressWarnings("ConstantConditions") ObjectInputStream ois = new ObjectInputStream(new FileInputStream(versionFile));
            int version = ois.readInt();
            ois.close();
            return version;
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public void setVersionLastRun(int version) {
        File versionFile = getVersionFile();
        versionFile.getParentFile().mkdirs();
        try {
            @SuppressWarnings("ConstantConditions") ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(versionFile));
            ois.writeInt(version);
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getVersionFile() {
        try {
            return new File("data/" + URLEncoder.encode(getClass().getName(), "UTF-8") + ".version");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new File("data/this file should not exist error.version");
    }
}
