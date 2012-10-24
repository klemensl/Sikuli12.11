/*
 * Copyright 2010-2011, Sikuli.org
 * Released under the MIT License.
 *
 * Edited 2012 by RaiMan
 */
package org.sikuli.core;

import java.io.File;
import java.lang.reflect.Constructor;
import org.sikuli.system.OSUtil;
import org.sikuli.utility.Debug;

public class Settings {

  static {
  }
  public final static String SikuliVersion = "X-1.0rc3";
  public static final int JavaVersion = Integer.parseInt(java.lang.System.getProperty("java.version").substring(2, 3));
  public static float DefaultHighlightTime = 2f;
  public static FindFailedResponse defaultFindFailedResponse = FindFailedResponse.ABORT;
  public static boolean ThrowException = true; // throw FindFailed exception
  public static float AutoWaitTimeout = 3f; // in seconds
  public static float WaitScanRate = 3f; // frames per second
  public static float ObserveScanRate = 3f; // frames per second
  public static int ObserveMinChangedPixels = 50; // in pixels
  public static double MinSimilarity = 0.7;
  public static double DelayBeforeDrop = 0.3;
  public static double DelayAfterDrag = 0.3;
  public static float SlowMotionDelay = 2.0f; // in seconds
  public static float MoveMouseDelay = 0.5f; // in seconds
  public static String BundlePath = null;
  public static String OcrDataPath = null;
  public static String libSource = "META-INF/lib/";
  public static String libPathMac = "/Applications/RaiManSikuli2012-IDE.app/Contents/Frameworks";
  public static boolean ShowActions = false;
  public static boolean ActionLogs = true;
  public static boolean InfoLogs = true;
  public static boolean DebugLogs = true;
  public static boolean ProfileLogs = false;

  static OSUtil _osUtil = null;

	public static String getFilePathSeperator() {
		return File.separator;
	}

  public static String getPathSeparator() {
    if (isWindows()) {
      return ";";
    }
    return ":";
  }

  public static String getSikuliDataPath() {
    String home, sikuliPath;
    if (isWindows()) {
      home = System.getenv("APPDATA");
      sikuliPath = "Sikuli";
    } else if (isMac()) {
      home = System.getProperty("user.home")
              + "/Library/Application Support";
      sikuliPath = "Sikuli";
    } else {
      home = System.getProperty("user.home");
      sikuliPath = ".sikuli";
    }
    File fHome = new File(home, sikuliPath);
    return fHome.getAbsolutePath();
  }

  public static int getOS() {
    int osRet = Constants.ISNOTSUPPORTED;
    String os = System.getProperty("os.name").toLowerCase();
    if (os.startsWith("mac")) {
      osRet = Constants.ISMAC;
    } else if (os.startsWith("windows")) {
      osRet = Constants.ISWINDOWS;
    } else if (os.startsWith("linux")) {
      osRet = Constants.ISLINUX;
    }
    return osRet;
  }

  public static boolean isWindows() {
    return getOS() == Constants.ISWINDOWS;
  }

  public static boolean isLinux() {
    return getOS() == Constants.ISLINUX;
  }

  public static boolean isMac() {
    return getOS() == Constants.ISMAC;
  }

  public static String getOSVersion() {
    return System.getProperty("os.version");
  }

  static String getOSUtilClass() {
    String pkg = "org.sikuli.system.";
    switch (getOS()) {
      case Constants.ISMAC:
        return pkg + "MacUtil";
      case Constants.ISWINDOWS:
        return pkg + "Win32Util";
      case Constants.ISLINUX:
        return pkg + "LinuxUtil";
      default:
        Debug.error("Warning: Sikuli doesn't fully support your OS.");
        return pkg + "DummyUtil";
    }
  }

  public static OSUtil getOSUtil() {
    if (_osUtil == null) {
      try {
        Class c = Class.forName(getOSUtilClass());
        Constructor constr = c.getConstructor();
        _osUtil = (OSUtil) constr.newInstance();
      } catch (Exception e) {
// TODO Java 7
//              ClassNotFoundException | NoSuchMethodException |
//              SecurityException | InstantiationException |
//              IllegalAccessException | IllegalArgumentException |
//              InvocationTargetException e) {
        Debug.error("Can't create OS Util: " + e.getMessage());
      }
    }
    return _osUtil;
  }

}