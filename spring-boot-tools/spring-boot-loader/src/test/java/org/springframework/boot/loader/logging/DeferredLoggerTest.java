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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link DefferedLogger}
 *
 * @author Michael Rumpf
 *
 */
public class DeferredLoggerTest {

	@Test
	public void testIsLoggable() {
		DeferredLogger a = DeferredLogger.getLogger("A");
		assertEquals(Level.INFO, a.getLevel());
		assertTrue(a.isLoggable(Level.SEVERE));
		assertFalse(a.isLoggable(Level.FINE));
		a.setLevel(Level.SEVERE);
		assertEquals(Level.SEVERE, a.getLevel());
		assertFalse(a.isLoggable(Level.INFO));
	}

	@Test
	public void testGetDefaultLevel() {
		DeferredLogger a = DeferredLogger.getLogger("A");
		assertEquals(Level.INFO, a.getLevel());
	}

	@Test
	public void testSetLevel() {
		DeferredLogger a = DeferredLogger.getLogger("A");
		a.setLevel(Level.SEVERE);
		assertEquals(Level.SEVERE, a.getLevel());
	}

	@Test
	public void testAllLogMethods() {
		DeferredLogger a = DeferredLogger.getLogger("A");
		a.severe("severe");
		a.warning("warning");
		a.info("info");
		a.fine("fine");
		a.finer("finer");
		a.finest("finest");
		a.entering("class", "method");
		a.entering("class", "method", Boolean.TRUE);
		a.entering("class", "method", new Object[] {Boolean.TRUE});
		a.exiting("class", "method", Boolean.TRUE);
		a.exiting("class", "method", new Object[] {Boolean.TRUE});
		a.throwing("class", "method", new Exception("exception"));
		a.log(Level.ALL, "msg");
		a.log(Level.ALL, "one param", Boolean.TRUE);
		a.log(Level.ALL, "array or params", new Object[] {Boolean.TRUE});
		a.log(Level.ALL, "thrown", new Exception());
		Map<Logger, List<LogRecord>> loggers = DeferredLogger.getLoggers();
		assertTrue(loggers.keySet().size() == 1);
		assertTrue(loggers.values().size() == 1);
		List<LogRecord> logRecords = loggers.values().iterator().next();
		assertTrue(logRecords.size() == 16);
	}
}
