/*
 * Copyright 2010-2011, Sikuli.org
 * Released under the MIT License.
 *
 */
package org.sikuli.script;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;

/**
 * Debug is a utility class that wraps println statements and allows more or less command line
 * output to be turned on.<br /> <br /> For debug messages only ( Debug.log() ):<br /> Use system
 * property: sikuli.Debug to set the debug level (default = 1)<br /> On the command line, use
 * -Dsikuli.Debug=n to set it to level n<br /> -Dsikuli.Debug will disable any debug messages <br />
 * (which is equivalent to using Settings.Debuglogs = false)<br /> <br /> It prints if the level
 * number is less than or equal to the currently set DEBUG_LEVEL.<br /> <br /> For messages
 * ActionLogs, InfoLogs see Settings<br /> <br /> You might send all messages generated by this
 * class to a file:<br />-Dsikuli.Logfile=pathname (no path given: SikuliLog.txt in working
 * folder)<br /> This can be restricted to Debug.user only (others go to System.out):<br />
 * -Dsikuli.LogfileUser=pathname (no path given: UserLog.txt in working folder)<br />
 *
 * This solution is NOT treadsafe !!!
 */
public class Debug {

  private static final int DEFAULT_LEVEL = 1;
  private static int DEBUG_LEVEL = DEFAULT_LEVEL;
  private long _beginTime;
  private static PrintStream printout = null;
  private static PrintStream printoutuser = null;
  private static String logfile;
  private static String logfileuser;
  private static final DateFormat df =
          DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

  static {
    String debug = System.getProperty("sikuli.Debug");
    if (debug != null && "".equals(debug)) {
      DEBUG_LEVEL = 0;
      Settings.DebugLogs = false;
    } else {
      try {
        DEBUG_LEVEL = Integer.parseInt(debug);
        Settings.DebugLogs = true;
      } catch (NumberFormatException numberFormatException) {
      }
    }
    logfile = System.getProperty("sikuli.Logfile");
    if (logfile != null) {
      if ("".equals(logfile)) {
        logfile = Settings.slashify(System.getProperty("user.dir"), true) + "SikuliLog.txt";
      }
      try {
        printout = new PrintStream(logfile);
      } catch (FileNotFoundException ex) {
        System.out.printf("[Error] Logfile %s not accessible - check given path", logfile);
        System.out.println();
      }
    }
    logfileuser = System.getProperty("sikuli.LogfileUser");
    if (logfileuser != null) {
      if ("".equals(logfileuser)) {
        logfileuser = Settings.slashify(System.getProperty("user.dir"), true) + "UserLog.txt";
      }
      try {
        printoutuser = new PrintStream(logfileuser);
      } catch (FileNotFoundException ex) {
        System.out.printf("[Error] User logfile %s not accessible - check given path", logfileuser);
        System.out.println();
      }
    }
  }

  /**
   *
   * @return current debug level
   */
  public static int getDebugLevel() {
    return DEBUG_LEVEL;
  }

  /**
   * set debug level to default level
   * @return default level
   */
  public static int setDebugLevel() {
    setDebugLevel(DEFAULT_LEVEL);
    return DEBUG_LEVEL;
  }

  /**
   * set debug level to given value
   * @param level
   */
  public static void setDebugLevel(int level) {
    DEBUG_LEVEL = level;
  }

  /**
   * Sikuli messages from actions like click, ...<br /> switch on/off: Settings.ActionLogs
   *
   * @param message (String or format string)
   * @param args to use with format string
   */
  public static void history(String message, Object... args) {
    if (Settings.ActionLogs) {
      log(-1, "[log] ", message, args);
    }
  }

  /**
   * informative Sikuli messages <br /> switch on/off: Settings.InfoLogs
   *
   * @param message (String or format string)
   * @param args to use with format string
   */
  public static void info(String message, Object... args) {
    if (Settings.InfoLogs) {
      log(-1, "[info] ", message, args);
    }
  }

  /**
   * Sikuli error messages<br /> switch on/off: always on
   *
   * @param message (String or format string)
   * @param args to use with format string
   */
  public static void error(String message, Object... args) {
    log(-1, "[error] ", message, args);
  }

  /**
   * Sikuli debug messages with default level<br /> switch on/off: Settings.DebugLogs (off) and/or
   * -Dsikuli.Debug
   *
   * @param message (String or format string)
   * @param args to use with format string
   */
  public static void log(String message, Object... args) {
    log(DEFAULT_LEVEL, message, args);
  }

  /**
   * messages given by the user<br /> switch on/off: Settings.UserLogs<br /> depending on
   * Settings.UserLogTime, the prefix contains a timestamp <br /> the user prefix (default "user")
   * can be set: Settings,UserLogPrefix
   *
   * @param message (String or format string)
   * @param args to use with format string
   */
  public static void user(String message, Object... args) {
    if (Settings.UserLogs) {
      if (Settings.UserLogTime) {
//TODO replace the hack -99 to filter user logs
        log(-99, String.format("[%s (%s)] ",
                Settings.UserLogPrefix, df.format(new Date())), message, args);
      } else {
        log(-99, String.format("[%s] ", Settings.UserLogPrefix), message, args);
      }
    }
  }

  /**
   * Sikuli debug messages with level<br /> switch on/off: Settings.DebugLogs (off) and/or
   * -Dsikuli.Debug
   *
   * @param level
   * @param message (String or format string)
   * @param args to use with format string
   */
  public static void log(int level, String message, Object... args) {
    if (Settings.DebugLogs) {
      log(level, "[debug] ", message, args);
    }
  }

  private static void log(int level, String prefix, String message, Object... args) {
    String sout;
    if (isEnabled(level)) {
      if (args.length != 0) {
        sout = String.format(prefix + message, args);
      } else {
        sout = prefix + message;
      }
//TODO replace the hack -99 to filter user logs
      if (level == -99 && printoutuser != null) {
        printoutuser.print(sout);
        printoutuser.println();
      } else if (printout != null) {
        printout.print(sout);
        printout.println();
      } else {
        System.out.print(sout);
        System.out.println();
      }
    }
  }

  private static boolean isEnabled(int level) {
    return level <= DEBUG_LEVEL;
  }

  private static boolean isEnabled() {
    return isEnabled(DEFAULT_LEVEL);
  }

  /**
   * Sikuli profiling messages<br /> switch on/off: Settings.ProfileLogs, default off
   *
   * @param message (String or format string)
   * @param args to use with format string
   */
  public static void profile(String message, Object... args) {
    if (Settings.ProfileLogs) {
      log(-1, "[profile] ", message, args);
    }
  }

  /**
   * start the timer<br /> usage: Debug timer = new Debug(); timer.startTiming(...)
   *
   * @param message (String or format string)
   * @param args to use with format string
   */
  public void startTiming(String message, Object... args) {
    if ("".equals(message)) {
      Debug.profile("TimerStart: " + message, args);
    }
    _beginTime = (new Date()).getTime();
  }

  /**
   * convenience: silently start timer<br /> usage: Debug timer = new Debug(); timer.start(); ...
   * timer.end();
   */
  public void start() {
    startTiming("");
  }

  /**
   * stop the timer and print message, if current debug level > default level
   *
   * @param message
   * @return the time in msec
   */
  public long endTiming(String message, Object... args) {
    long t = (new Date()).getTime();
    long dt = t - _beginTime;
    Debug.profile(String.format("TimerEnd (%.3f sec): ", (float) dt / 1000) + message, args);
    return dt;
  }

  /**
   * convenience: stop timer and print standard message
   *
   * @return the time in msec
   */
  public long end() {
    return endTiming("");
  }
}
