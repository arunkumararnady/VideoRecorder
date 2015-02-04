/*
 * Copyright (c) 2014 Diona Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Diona
 * ("Confidential Information"). You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license agreement you
 * entered into with Diona.
 */

package com.diona.videoplugin;

import android.content.Context;
import android.util.Log;

/**
 * A utility for writing logs. Logged messages will appear in the LogCat when device is connected to a computer running
 * the Android DDMS. Uses the boolean Config.SHOW_LOGGING to decide whether messages should actually be printed or not.
 * There are varying levels of logging and the appropriate type of log should be used for the situation (eg
 * LogUtil#exceptionLog should be used for exceptions as they will be displayed differently in LogCat than
 * LogUtil#debugLog). What level of log is shown in LogCat can be configured using the Log level dropdown in Android
 * DDMS.
 */
public class LogUtil {

  private static final int TAG_MAX_CHARACTER_COUNT = 23;

  /**
   * Constant for configuring the default log level. It overwrites log level configured on the device. This value can be
   * overridden on startup by adding a log.level.default entry to bootstrap.properties.
   */
  private static int defaultLogLevel = Log.INFO;

  /**
   * Constructor.
   */
  protected LogUtil() {
    // do nothing
  }

  /**
   * Initialises the LogUtil class, setting the default log level value from bootstrap.properties if available.
   * 
   * @param applicationContext
   *          The application context.
   */
  public static void init(final Context applicationContext) {
   // final BootstrapProperties bootstrapProperties = BootstrapProperties.getInstance();
    final String bootstrapLogLevel = "verbose";

    if (null != bootstrapLogLevel && !bootstrapLogLevel.trim().isEmpty()) {
      if (bootstrapLogLevel.equalsIgnoreCase("error")) {
        defaultLogLevel = Log.ERROR;
      } else if (bootstrapLogLevel.equalsIgnoreCase("warn")) {
        defaultLogLevel = Log.WARN;
      } else if (bootstrapLogLevel.equalsIgnoreCase("info")) {
        defaultLogLevel = Log.INFO;
      } else if (bootstrapLogLevel.equalsIgnoreCase("debug")) {
        defaultLogLevel = Log.DEBUG;
      } else if (bootstrapLogLevel.equalsIgnoreCase("verbose")) {
        defaultLogLevel = Log.VERBOSE;
      }
    }
  }

  /**
   * Logs error by printing the tag that contains the name of the class where the exception is thrown, message and the
   * exception trace.
   *
   * @param tag
   *          A tag to associate with the log.
   * @param message
   *          Message associated with the log.
   * @param tr
   *          Exception thrown
   */
  public static synchronized void error(String tag, final String message, final Throwable tr) {
    tag = getTag(tag);
    if (isLoggable(tag, Log.ERROR)) {
      Log.e(tag, message, tr);
    }
  }

  /**
   * Logs error by printing the tag that contains the name of the class where the exception is thrown and the exception
   * trace.
   *
   * @param tag
   *          A tag to associate with the log.
   * @param tr
   *          Exception thrown
   */
  public static synchronized void error(String tag, final Throwable tr) {
    tag = getTag(tag);
    if (isLoggable(tag, Log.ERROR)) {
      final String msg = tr.getLocalizedMessage();
      Log.e(tag, msg, tr);
    }
  }

  /**
   * Logs an error by printing the tag that contains class name and message related to the error.
   *
   * @param tag
   *          A tag to associate with the log.
   * @param message
   *          the message you want to log.
   */
  public static synchronized void error(String tag, final String message) {
    tag = getTag(tag);
    if (isLoggable(tag, Log.ERROR)) {
      Log.e(tag, message);
    }
  }

  /**
   * Logs a warning message. Use this when something has happened that could indicate an error but is not definitely an
   * error.
   *
   * @param tag
   *          A tag to associate with the log.
   * @param log
   *          the message you want to log.
   */
  public static synchronized void warn(String tag, final String log) {
    tag = getTag(tag);
    if (isLoggable(tag, Log.WARN)) {
      Log.w(tag, log);
    }
  }

  /**
   * Logs a warning message. Use this when something has happened that could indicate an error but is not definitely an
   * error.
   *
   * @param tag
   *          A tag to associate with the log.
   * @param log
   *          the message you want to log.
   * @param throwable
   *          a throwable you want to log the stack trace of
   */
  public static synchronized void warn(String tag, final String log, final Throwable throwable) {
    tag = getTag(tag);
    if (isLoggable(tag, Log.WARN)) {
      Log.w(tag, log, throwable);
    }

  }

  /**
   * Logs an info message. Use this to report on status of operations such as successfully connecting to a server.
   *
   * @param tag
   *          A tag to associate with the log.
   * @param log
   *          the message you want to log.
   */
  public static synchronized void info(String tag, final String log) {
    tag = getTag(tag);
    if (isLoggable(tag, Log.INFO)) {
      Log.i(tag, log);
    }
  }

  /**
   * Logs a debug message. Use this when you wish to make a record of variable information or chart the flow of an app.
   *
   * @param tag
   *          A tag to associate with the log.
   * @param msg
   *          message
   * @param tr
   *          throwable
   */
  public static synchronized void debug(String tag, final String msg, final Throwable tr) {
    tag = getTag(tag);
    if (isLoggable(tag, Log.DEBUG)) {
      Log.d(tag, msg, tr);
    }
  }

  /**
   * Logs a debug message. Use this when you wish to make a record of variable information or chart the flow of an app.
   *
   * @param tag
   *          A tag associated with the log.
   * @param msg
   *          message
   */
  public static synchronized void debug(String tag, final String msg) {
    tag = getTag(tag);
    if (isLoggable(tag, Log.DEBUG)) {
      Log.d(tag, msg);
    }
  }

  /**
   * Logs a verbose message. Use this when you are logging at a very detailed level.
   *
   * @param tag
   *          A tag to associate with the log.
   * @param log
   *          the message you want to log.
   */
  public static synchronized void verbose(String tag, final String log) {
    tag = getTag(tag);
    if (isLoggable(tag, Log.VERBOSE)) {
      Log.v(tag, log);
    }
  }

  /**
   * Check whether a tag is loggable at a certain level. The check is done against the development log level and the
   * android log level, where development log level has higher priority.
   * 
   * @param tag
   * @param logLevel
   * @return
   */
  private static boolean isLoggable(final String tag, final int logLevel) {
    if ((defaultLogLevel <= logLevel) && true) {
      return true;
    }

    return Log.isLoggable(tag, logLevel);

  }

  private static String getTag(final String tag) {
    return StringUtil.truncateString("SWA-" + tag, TAG_MAX_CHARACTER_COUNT);
  }

}
