package co.smartreceipts.android.utils.log;


import org.slf4j.LoggerFactory;

public class Logger {

    private static org.slf4j.Logger getLoggerForCaller(Object caller) {
        if (caller instanceof Class<?>) {
            return LoggerFactory.getLogger((Class<?>)caller);
        } else {
            return LoggerFactory.getLogger(caller.getClass());
        }
    }

    public static void debug(Object caller, String msg) {
        getLoggerForCaller(caller).debug(msg);
    }

    public static void debug(Object caller, String msg, Throwable t) {
        getLoggerForCaller(caller).debug(msg, t);
    }

    public static void debug(Object caller, String format, Object... arg) {
        getLoggerForCaller(caller).debug(format, arg);
    }

    public static void debug(Object caller, String format, Object arg) {
        getLoggerForCaller(caller).debug(format, arg);
    }

    public static void debug(Object caller, String format, Object arg1, Object arg2) {
        getLoggerForCaller(caller).debug(format, arg1, arg2);
    }

    public static void info(Object caller, String msg) {
        getLoggerForCaller(caller).info(msg);
    }

    public static void info(Object caller, String msg, Throwable t) {
        getLoggerForCaller(caller).info(msg, t);
    }

    public static void info(Object caller, String format, Object... arg) {
        getLoggerForCaller(caller).info(format, arg);
    }

    public static void info(Object caller, String format, Object arg) {
        getLoggerForCaller(caller).info(format, arg);
    }

    public static void info(Object caller, String format, Object arg1, Object arg2) {
        getLoggerForCaller(caller).info(format, arg1, arg2);
    }

    public static void trace(Object caller, String msg) {
        getLoggerForCaller(caller).trace(msg);
    }

    public static void trace(Object caller, String msg, Throwable t) {
        getLoggerForCaller(caller).trace(msg, t);
    }

    public static void trace(Object caller, String format, Object... arg) {
        getLoggerForCaller(caller).trace(format, arg);
    }

    public static void trace(Object caller, String format, Object arg) {
        getLoggerForCaller(caller).trace(format, arg);
    }

    public static void trace(Object caller, String format, Object arg1, Object arg2) {
        getLoggerForCaller(caller).trace(format, arg1, arg2);
    }

    public static void warn(Object caller, String msg) {
        getLoggerForCaller(caller).warn(msg);
    }

    public static void warn(Object caller, String msg, Throwable t) {
        getLoggerForCaller(caller).warn(msg, t);
    }

    public static void warn(Object caller,Throwable t) {
        getLoggerForCaller(caller).warn(null, t);
    }

    public static void warn(Object caller, String format, Object... arg) {
        getLoggerForCaller(caller).warn(format, arg);
    }

    public static void warn(Object caller, String format, Object arg) {
        getLoggerForCaller(caller).warn(format, arg);
    }

    public static void warn(Object caller, String format, Object arg1, Object arg2) {
        getLoggerForCaller(caller).warn(format, arg1, arg2);
    }

    public static void error(Object caller, String msg) {
        getLoggerForCaller(caller).error(msg);
    }

    public static void error(Object caller, Throwable t) {
        if (t != null && t.getStackTrace() != null) {
            getLoggerForCaller(caller).error("", t);
        } else {
            getLoggerForCaller(caller).error("Insufficient logging details available for error");
        }
    }

    public static void error(Object caller, String msg, Throwable t) {
        if (t != null && t.getStackTrace() != null) {
            getLoggerForCaller(caller).error(msg, t);
        } else {
            getLoggerForCaller(caller).error(msg);
        }
    }

    public static void error(Object caller, String format, Object... arg) {
        getLoggerForCaller(caller).error(format, arg);
    }

    public static void error(Object caller, String format, Object arg) {
        getLoggerForCaller(caller).error(format, arg);
    }

    public static void error(Object caller, String format, Object arg1, Object arg2) {
        getLoggerForCaller(caller).error(format, arg1, arg2);
    }
}
