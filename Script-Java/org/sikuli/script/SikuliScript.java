/*
 * Copyright 2010-2011, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2012
 */
package org.sikuli.script;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import org.apache.commons.cli.CommandLine;
import org.python.util.jython;
import org.sikuli.core.Settings;
import org.sikuli.system.App;
import org.sikuli.utility.CommandArgs;
import org.sikuli.utility.Debug;

public class SikuliScript {

  private static CommandLine cmdLine;

  public SikuliScript() throws AWTException {
  }

  public static void main(String[] args) {
    int exitCode = 0;

		Debug.log(2, "Running on Java " + Settings.JavaVersion);

    CommandArgs cmdArgs = new CommandArgs("SCRIPT");
    cmdLine = cmdArgs.getCommandLine(args);

    //TODO downward compatibel
    if (!args[0].startsWith("-")) {
      ScriptRunner runner = new ScriptRunner(CommandArgs.getPyArgs(cmdLine));
      exitCode = runner.runPython(null);
      Debug.info("You are using deprecated command line argument syntax!");
      cmdArgs.printHelp();
      System.exit(exitCode);
    }

    if (cmdLine != null) {
      if (cmdLine.hasOption("h")) {
        cmdArgs.printHelp();
        return;
      }

      if (cmdLine.hasOption("run")) {
        ScriptRunner runner = new ScriptRunner(CommandArgs.getPyArgs(cmdLine), "SCRIPT");
        System.exit(runner.runPython(null));
      }
    } else {
      Debug.error("Nothing to do! No valid arguments on commandline!");
      cmdArgs.printHelp();
    }
  }

  private static void startInteractiveMode(String[] args) {
    String[] jy_args = {"-i", "-c",
      "from sikuli import *;"
      + "print \"Hello, this is your interactive Sikuli (rules for interactive Python apply)\\n"
      + "... use ctrl-d to end the session\""};
    jython.main(jy_args);
  }

  public static void setShowActions(boolean flag) {
    Settings.ShowActions = flag;
    if (flag) {
      if (Settings.MoveMouseDelay < 1f) {
        Settings.MoveMouseDelay = 1f;
      }
    }
  }

  public static String input(String msg) {
    return (String) JOptionPane.showInputDialog(msg);
  }

  public static String input(String msg, String preset) {
    return (String) JOptionPane.showInputDialog(msg, preset);
  }

  public static int switchApp(String appName) {
    if (App.focus(appName) != null) {
      return 0;
    }
    return -1;
  }

  public static int openApp(String appName) {
    if (App.open(appName) != null) {
      return 0;
    }
    return -1;
  }

  public static int closeApp(String appName) {
    return App.close(appName);
  }

  public static void popup(String message, String title) {
    JOptionPane.showMessageDialog(null, message,
            title, JOptionPane.PLAIN_MESSAGE);
  }

  public static void popup(String message) {
    popup(message, "Sikuli");
  }

  public static String run(String cmdline) {
    String lines = "";
    try {
      String line;
      Process p = Runtime.getRuntime().exec(cmdline);
      BufferedReader input =
              new BufferedReader(new InputStreamReader(p.getInputStream()));
      while ((line = input.readLine()) != null) {
        lines = lines + '\n' + line;
      }
    } catch (Exception err) {
      err.printStackTrace();
    }
    return lines;
  }
}