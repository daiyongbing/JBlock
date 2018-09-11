package jblock.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JBlockLog extends Logger {
    JBlockLog(String name, String resourceBundleName){
        super(name, resourceBundleName);
    };

    @Override
    public void log(Level level, String msg) {
        super.log(level, msg);
    }

    @Override
    public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
        super.logp(level, sourceClass, sourceMethod, msg, param1);
    }
}
