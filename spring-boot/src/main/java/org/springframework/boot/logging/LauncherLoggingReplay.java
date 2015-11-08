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

package org.springframework.boot.logging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class replays the JUL LogRecords, gathered during the Spring Boot Launcher phase,
 * to the logging sub-system that is configured for the Spring Boot application.
 * <p>
 * It uses the same mapping logic
 * </p>
 *
 * @author Michael Rumpf
 *
 */
public final class LauncherLoggingReplay {

	private static Map<Logger, List<LogRecord>> loggers = new HashMap<Logger, List<LogRecord>>();

	private LauncherLoggingReplay() {
	}

	/**
	 * Transfers the logger and log records from the launcher to the application.
	 * @param launcherLoggers the launcher loggers and log records
	 */
	public static void setLoggers(Map<Logger, List<LogRecord>> launcherLoggers) {
		loggers = launcherLoggers;
	}

	/**
	 * Replays the log records from the launcher to the Commons Logging API.
	 */
	public static void replay() {
		if (loggers != null) {
			for (Logger logger : loggers.keySet()) {
				Log log = LogFactory.getLog(logger.getName());
				for (LogRecord logRecord : loggers.get(logger)) {
					if (Level.SEVERE.equals(logger.getLevel())) {
						log.error(logRecord.getMessage());
					}
					else if (Level.WARNING.equals(logger.getLevel())) {
						log.warn(logRecord.getMessage());
					}
					else if (Level.INFO.equals(logger.getLevel())
							|| Level.CONFIG.equals(logger.getLevel())) {
						log.info(logRecord.getMessage());
					}
					else if (Level.FINE.equals(logger.getLevel())) {
						log.debug(logRecord.getMessage());
					}
					else if (Level.FINER.equals(logger.getLevel())
							|| Level.FINEST.equals(logger.getLevel())
							|| Level.ALL.equals(logger.getLevel())) {
						log.trace(logRecord.getMessage());
					}
					else if (null == logger.getLevel()) {
						log.info(logRecord.getMessage());
					}
				}
			}
		}
	}
}
