/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.loader.logging;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The DeferredLogger class is used for logging during the Spring Boot's application
 * loader phase.
 * <p>
 * The DeferredLogger mimics part of the public interface of the java.util.logging.Logger
 * class. It does not provide a full implementation as the Launcher is not supposed to do
 * any sophisticated logging during the launcher phase because the logging sub-system has
 * not been fully initialized at that point in time.
 * </p>
 *
 * <p>
 * The important part is that the java.util.logging.Logger's internal method
 * getLogManager() must not be called. When this method is called the JUL logging
 * sub-systems gets statically statically to the JUL default LogManager implementation
 * without any chance of redirecting JUL logging to the application logging framework
 * later on.
 * </p>
 *
 * <p>
 * This class collects information about the loggers, the programmatically configured
 * levels and the log records that have been issued by the launcher code. When the
 * SpringApplication class' main method is invoked, it makes sure that all this
 * information is passed to the SpringApplication so that it can be replayed, using the
 * application's logging framework of choice.
 * </p>
 *
 * <p>
 * To transfer all information from the launcher phase to the application phase classes
 * from Java Util Logging are used directly as they are loaded by the system classloader
 * and are available in both phases, not introducing any implicit dependencies to custom
 * classes.
 * <ul>
 * <li>Using the class java.util.logging.LogRecord captures all information that is
 * available to JUL LogRecords, although it might be possible that this information cannot
 * be used later, because the logging framework might not be able to use it anymore (e.g.
 * the timestamp when the LogRecord was created cannot be used to set the timestamp with
 * the Commons Logging wrapper)</li>
 * <li>The class java.util.logging.Logger is used to hold information like the name and
 * the level. Unfortunately the level cannot be set because there is no API in with the
 * Commons Logging framework to set the level programmatically.</li>
 * </ul>
 * </p>
 *
 * @author Michael Rumpf
 *
 */
public final class DeferredLogger {

	private static Map<String, DeferredLogger> DEFERRED_LOGGERS = new HashMap<String, DeferredLogger>();

	private Logger logger = null;
	private List<LogRecord> logRecords = new ArrayList<LogRecord>();

	private DeferredLogger() {
	}

	/**
	 * Check if a message of the given level would actually be logged by this logger.
	 * @param level a message logging level
	 * @return true if the given message level is currently being logged.
	 * @see java.util.logging.Logger#isLoggable(Level)
	 */
	public boolean isLoggable(Level level) {
		return this.logger.isLoggable(level);
	}

	/**
	 * Get the log Level that has been specified for this Logger. The result may be null,
	 * which means that this logger's effective level will be inherited from its parent.
	 * @return this Logger's level
	 * @see java.util.logging.Logger#getLevel()
	 */
	public Level getLevel() {
		return this.logger.getLevel();
	}

	/**
	 * Set the log level specifying which message levels will be logged by this logger.
	 * Message levels lower than this value will be discarded. The level value Level.OFF
	 * can be used to turn off logging.
	 *
	 * @param newLevel the new value for the log level
	 * @see java.util.logging.Logger#setLevel(Level)
	 */
	public void setLevel(Level newLevel) {
		this.logger.setLevel(newLevel);
	}

	/**
	 * Log a FINE message.
	 * @param msg The string message
	 * @see java.util.logging.Logger#fine(String)
	 */
	public void fine(String msg) {
		LogRecord logRecord = new LogRecord(Level.FINE, msg);
		addLogRecord(logRecord);
	}

	/**
	 * Log a FINER message.
	 * @param msg The string message
	 * @see java.util.logging.Logger#finer(String)
	 */
	public void finer(String msg) {
		LogRecord logRecord = new LogRecord(Level.FINER, msg);
		addLogRecord(logRecord);
	}

	/**
	 * Log a FINEST message.
	 * @param msg The string message
	 * @see java.util.logging.Logger#finest(String)
	 */
	public void finest(String msg) {
		LogRecord logRecord = new LogRecord(Level.FINEST, msg);
		addLogRecord(logRecord);
	}

	/**
	 * Log an INFO message.
	 * @param msg The string message
	 * @see java.util.logging.Logger#info(String)
	 */
	public void info(String msg) {
		LogRecord logRecord = new LogRecord(Level.INFO, msg);
		addLogRecord(logRecord);
	}

	/**
	 * Log a SEVERE message.
	 * @param msg The string message
	 * @see java.util.logging.Logger#severe(String)
	 */
	public void severe(String msg) {
		LogRecord logRecord = new LogRecord(Level.SEVERE, msg);
		addLogRecord(logRecord);
	}

	/**
	 * Log a WARNING message.
	 * @param msg The string message
	 * @see java.util.logging.Logger#warning(String)
	 */
	public void warning(String msg) {
		LogRecord logRecord = new LogRecord(Level.WARNING, msg);
		addLogRecord(logRecord);
	}

	/**
	 * Log a method entry.
	 * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that is being entered
	 * @see java.util.logging.Logger#entering(String, String)
	 */
	public void entering(String sourceClass, String sourceMethod) {
		LogRecord logRecord = new LogRecord(Level.FINER, "ENTRY");
		logRecord.setSourceClassName(sourceClass);
		logRecord.setSourceMethodName(sourceMethod);
		addLogRecord(logRecord);
	}

	/**
	 * Log a method entry, with one parameter.
	 * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that is being entered
     * @param param1 parameter to the method being entered
	 * @see java.util.logging.Logger#entering(String, String, Object)
	 */
	public void entering(String sourceClass, String sourceMethod, Object param1) {
		LogRecord logRecord = new LogRecord(Level.FINER, "ENTRY");
		logRecord.setSourceClassName(sourceClass);
		logRecord.setSourceMethodName(sourceMethod);
		Object[] params = { param1 };
		logRecord.setParameters(params);
		addLogRecord(logRecord);
	}

	/**
	 * Log a method entry, with an array of parameters.
	 * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that is being entered
     * @param params array of parameters to the method being entered
	 * @see java.util.logging.Logger#entering(String, String, Object[])
	 */
	public void entering(String sourceClass, String sourceMethod, Object[] params) {
		LogRecord logRecord = new LogRecord(Level.FINER, "ENTRY");
		logRecord.setSourceClassName(sourceClass);
		logRecord.setSourceMethodName(sourceMethod);
		logRecord.setParameters(params);
		addLogRecord(logRecord);
	}

	/**
	 * Log a method return.
	 * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method
	 * @see java.util.logging.Logger#exiting(String, String)
	 */
	public void exiting(String sourceClass, String sourceMethod) {
		LogRecord logRecord = new LogRecord(Level.FINER, "RETURN");
		logRecord.setSourceClassName(sourceClass);
		logRecord.setSourceMethodName(sourceMethod);
		addLogRecord(logRecord);
	}

	/**
	 * Log a method return, with a result object.
	 * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method
     * @param result Object that is being returned
	 * @see java.util.logging.Logger#exiting(String, String, Object)
	 */
	public void exiting(String sourceClass, String sourceMethod, Object result) {
		LogRecord logRecord = new LogRecord(Level.FINER, "RETURN {0}");
		logRecord.setSourceClassName(sourceClass);
		logRecord.setSourceMethodName(sourceMethod);
		Object[] params = { result };
		logRecord.setParameters(params);
		addLogRecord(logRecord);
	}

	/**
	 * Log throwing an exception.
	 * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method
	 * @param thrown The Throwable that is being thrown.
	 * @see java.util.logging.Logger#throwing(String, String, Throwable)
	 */
	public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
		LogRecord logRecord = new LogRecord(Level.FINER, "THROW");
		logRecord.setSourceClassName(sourceClass);
		logRecord.setSourceMethodName(sourceMethod);
		logRecord.setThrown(thrown);
		addLogRecord(logRecord);
	}

	/**
	 * Log a message, with no arguments.
	 * @param level One of the message level identifiers, e.g. SEVERE
     * @param msg The string message
	 * @see java.util.logging.Logger#log(Level, String, Object)
	 */
	public void log(Level level, String msg) {
		LogRecord logRecord = new LogRecord(level, msg);
		addLogRecord(logRecord);
	}

	/**
	 * Log a message, with one object parameter.
	 * @param level One of the message level identifiers, e.g. SEVERE
     * @param msg The string message
     * @param param1 parameter to the message
	 * @see java.util.logging.Logger#log(Level, String, Object)
	 */
	public void log(Level level, String msg, Object param1) {
		LogRecord logRecord = new LogRecord(level, msg);
		Object[] params = { param1 };
		logRecord.setParameters(params);
		addLogRecord(logRecord);
	}

	/**
	 * Log a message, with an array of object parameters.
	 * @param level One of the message level identifiers, e.g. SEVERE
     * @param msg The string message
     * @param params array of parameter to the message
	 * @see java.util.logging.Logger#log(Level, String, Object[])
	 */
	public void log(Level level, String msg, Object[] params) {
		LogRecord logRecord = new LogRecord(level, msg);
		logRecord.setParameters(params);
		addLogRecord(logRecord);
	}

	/**
	 * Log a message, with associated Throwable information.
	 * @param level One of the message level identifiers, e.g. SEVERE
     * @param msg The string message
     * @param thrown Throwable associated with log message
	 * @see java.util.logging.Logger#log(Level, String, Throwable)
	 */
	public void log(Level level, String msg, Throwable thrown) {
		LogRecord logRecord = new LogRecord(level, msg);
		logRecord.setThrown(thrown);
		addLogRecord(logRecord);
	}

	/**
	 * This method adds LogRecords to the queue.
	 * @param logRecord the log record to add to this logger's list
	 */
	private void addLogRecord(LogRecord logRecord) {
		String name = this.logger.getName();
		logRecord.setLoggerName(name);
		this.logRecords.add(logRecord);
	}

	/**
	 * Find or create a logger for a named subsystem.
	 * @param name A name for the logger
	 * @return a suitable Logger
	 * @see java.util.logging.Logger#getLogger(String)
	 */
	public static DeferredLogger getLogger(String name) {
		DeferredLogger deferredLogger = DEFERRED_LOGGERS.get(name);
		if (deferredLogger == null) {
			deferredLogger = new DeferredLogger();
			Logger logger = createJulLoggerInstance(name);
			deferredLogger.logger = logger;
			DEFERRED_LOGGERS.put(name, deferredLogger);
		}
		return deferredLogger;
	}

	/**
	 * This method encapsulates all the ugly code to get a new instance of a JUL Logger by
	 * using the private constructor of this class in order to avoid calling the
	 * {@link java.util.logging.LogManager#getManager}. When the internal implementation
	 * changes, this code will throw an IllegalStateException.
	 * @param name the name of the logger
	 * @return the JUL logger instance
	 */
	private static Logger createJulLoggerInstance(String name) {
		Logger logger = null;
		try {
			// This is ugly, but the private method is the only way to get around
			// initializing the LogManager!!! (see JDK source code)
			Constructor<?> privateLoggerConstructor = Logger.class
					.getDeclaredConstructor(new Class<?>[] { String.class, String.class,
							Class.class, LogManager.class, boolean.class });
			privateLoggerConstructor.setAccessible(true);
			logger = (Logger) privateLoggerConstructor
					.newInstance(new Object[] { name, null, null, null, false });
			logger.setLevel(Level.INFO);
		}
		catch (Exception e) {
			throw new IllegalStateException("JUL Logger instantiation failed", e);
		}
		return logger;
	}

	/**
	 * This method is used to pass the Loggers and their LogRecords to the
	 * SpringApplication.
	 * <p>
	 * It uses JDK classes only so that we do not need to convert from a Launcher class to
	 * a SpringApplication class by using the Reflection API for accessing a custom
	 * transfer class.
	 * </p>
	 * @return the Loggers and their LogRecords
	 */
	public static Map<Logger, List<LogRecord>> getLoggers() {
		Map<Logger, List<LogRecord>> l = new HashMap<Logger, List<LogRecord>>();
		for (String name : DEFERRED_LOGGERS.keySet()) {
			DeferredLogger deferredLogger = DEFERRED_LOGGERS.get(name);
			l.put(deferredLogger.logger, deferredLogger.logRecords);
		}
		return l;
	}
}
