/*
 * Copyright 2010-2011, Sikuli.org
 * Released under the MIT License.
 *
 */
package org.sikuli.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Debug is a utility class that wraps println statements and allows more or
 * less command line output to be turned on.<br /> <br /> For debug messages
 * only ( Debug.log() ):<br /> Use system property: sikuli.Debug to set the
 * debug level (default = 1)<br /> On the command line, use -Dsikuli.Debug=n to
 * set it to level n<br /> -Dsikuli.Debug will disable any debug messages <br />
 * (which is equivalent to using Settings.Debuglogs = false)<br /> <br /> It
 * prints if the level number is less than or equal to the currently set
 * DEBUG_LEVEL.<br /> <br /> For messages ActionLogs, InfoLogs see Settings<br
 * /> <br /> You might send all messages generated by this class to a file:<br
 * /> -Dsikuli.logfile=pathname or logfile=pathname
 */
public class Debug {

  private static final int DEFAULT_LEVEL = 1;
  private static int DEBUG_LEVEL = DEFAULT_LEVEL;
  private long _beginTime;
  private static PrintStream out;
  private static String logfile;
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
      } catch (NumberFormatException numberFormatException) {
      }
    }
    logfile = System.getProperty("sikuli.Logfile");
    out = System.out;
    if (logfile != null) {
      if ("".equals(logfile)) {
        logfile = Settings.slashify(System.getProperty("user.dir"), true) + "SikuliLog.txt";
      }
      try {
        out = new PrintStream(logfile);
      } catch (FileNotFoundException ex) {
        System.out.printf("[Error] Logfile %s not found", logfile);
        System.out.println();
      }
    }
  }

  public static void history(String message, Object... args) {
    if (Settings.ActionLogs) {
      log(-1, "[log] ", message, args);
    }
  }

  public static void info(String message, Object... args) {
    if (Settings.InfoLogs) {
      log(-1, "[info] ", message, args);
    }
  }

  public static void error(String message, Object... args) {
    log(-1, "[error] ", message, args);
  }

  public static void log(String message, Object... args) {
    log(DEFAULT_LEVEL, message, args);
  }

  public static void user(String message, Object... args) {
    if (Settings.UserLogs) {
      if (Settings.UserLogTime) {
        log(-1, String.format("[%s (%s)] ",
                Settings.UserLogPrefix, df.format(new Date())), message, args);
      } else {
        log(-1, String.format("[%s] ", Settings.UserLogPrefix), message, args);
      }
    }
  }

  public static void log(int level, String message, Object... args) {
    if (Settings.DebugLogs) {
      log(level, "[debug] ", message, args);
    }
  }

  private static void log(int level, String prefix, String message, Object... args) {
    if (isEnabled(level)) {
      if (args.length != 0) {
        out.printf(prefix + message, args);
      } else {
        out.print(prefix + message);
      }
      out.println();
    }
  }

  /**
   *
   * @param level
   * @return true if level &lt;= current level
   */
  private static boolean isEnabled(int level) {
    return level <= DEBUG_LEVEL;
  }

  /**
   *
   * @return true if level &lt;= default level
   */
  private static boolean isEnabled() {
    return isEnabled(DEFAULT_LEVEL);
  }

  private static void profile(String message, Object... args) {
    if (DEBUG_LEVEL > DEFAULT_LEVEL) {
      log(-1, "[profile] ", message, args);
    }
  }

  public void startTiming(String message) {
    Debug.profile(message + " START");
    _beginTime = (new Date()).getTime();
  }

  public long endTiming(String message) {
    long t = (new Date()).getTime();
    long dt = t - _beginTime;
    Debug.profile(message + " END: " + dt + "ms");
    return dt;
  }
}
