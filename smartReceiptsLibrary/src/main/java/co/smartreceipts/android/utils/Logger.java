package co.smartreceipts.android.utils;


import org.slf4j.LoggerFactory;

public class Logger {

    private static org.slf4j.Logger getLoggerForCaller(Object caller) {
        return LoggerFactory.getLogger(caller.getClass());
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

    public static void info(Object caller, String msg) {
        getLoggerForCaller(caller).info(msg);
    }

    public static void info(Object caller, String msg, Throwable t) {
        getLoggerForCaller(caller).info(msg, t);
    }

    public static void info(Object caller, String format, Object... arg) {
        getLoggerForCaller(caller).info(format, arg);
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

    public static void error(Object caller, String msg) {
        getLoggerForCaller(caller).error(msg);
    }

    public static void error(Object caller, Throwable t) {
        getLoggerForCaller(caller).error(null, t);
    }

    public static void error(Object caller, String msg, Throwable t) {
        getLoggerForCaller(caller).error(msg, t);
    }

    public static void error(Object caller, String format, Object... arg) {
        getLoggerForCaller(caller).error(format, arg);
    }

}
