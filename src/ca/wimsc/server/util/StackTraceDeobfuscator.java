package ca.wimsc.server.util;

/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a clone of Google's class of the same name, but this version doesn't need filesystem access, so it's
 * suitable for hosting on app engine.
 */
public class StackTraceDeobfuscator {

	@SuppressWarnings("serial")
	public static class SymbolMap extends HashMap<String, String> {
	}

	// From JsniRef class, which is in gwt-dev and so can't be accessed here
	// TODO(unnurg) once there is a place for shared code, move this to there.
	private static Pattern JsniRefPattern = Pattern.compile("@?([^:]+)::([^(]+)(\\((.*)\\))?");

	private String symbolMapsDirectory;

	private Map<String, SymbolMap> symbolMaps = new HashMap<String, SymbolMap>();


	public StackTraceDeobfuscator(String symbolMapsDirectory) {
		this.symbolMapsDirectory = symbolMapsDirectory;
	}


	public LogRecord deobfuscateLogRecord(LogRecord lr, String strongName) {
		if (lr.getThrown() != null && strongName != null) {
			lr.setThrown(deobfuscateThrowable(lr.getThrown(), strongName));
		}
		return lr;
	}


	public void setSymbolMapsDirectory(String dir) {
		// Switching the directory should clear the symbolMaps variable (which
		// is read in lazily), but causing the symbolMaps variable to be re-read
		// is somewhat expensive, so we only want to do this if the directory is
		// actually different.
		if (!dir.equals(symbolMapsDirectory)) {
			symbolMapsDirectory = dir;
			symbolMaps = new HashMap<String, SymbolMap>();
		}
	}


	private StackTraceElement[] deobfuscateStackTrace(StackTraceElement[] st, String strongName) {
		StackTraceElement[] newSt = new StackTraceElement[st.length];
		for (int i = 0; i < st.length; i++) {
			newSt[i] = resymbolize(st[i], strongName);
		}
		return newSt;
	}


	private Throwable deobfuscateThrowable(Throwable old, String strongName) {
		Throwable t = new Throwable(old.getMessage());
		if (old.getStackTrace() != null) {
			t.setStackTrace(deobfuscateStackTrace(old.getStackTrace(), strongName));
		} else {
			t.setStackTrace(new StackTraceElement[0]);
		}
		if (old.getCause() != null) {
			t.initCause(deobfuscateThrowable(old.getCause(), strongName));
		}
		return t;
	}


	public SymbolMap loadSymbolMap(String strongName) {
		SymbolMap toReturn = symbolMaps.get(strongName);
		if (toReturn != null) {
			return toReturn;
		}
		toReturn = new SymbolMap();
		String line;
		String filename = symbolMapsDirectory + strongName + ".symbolMap";
		try {
			InputStream inputStream = StackTraceDeobfuscator.class.getClassLoader().getResourceAsStream(filename);
			BufferedReader bin = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = bin.readLine()) != null) {
				if (line.charAt(0) == '#') {
					continue;
				}
				int idx = line.indexOf(',');
				toReturn.put(new String(line.substring(0, idx)), line.substring(idx + 1));
			}
		} catch (NullPointerException e) {
			toReturn = null;
		} catch (IOException e) {
			toReturn = null;
		}

		symbolMaps.put(strongName, toReturn);
		return toReturn;
	}


	private String[] parse(String refString) {
		Matcher matcher = JsniRefPattern.matcher(refString);
		if (!matcher.matches()) {
			return null;
		}
		String className = matcher.group(1);
		String memberName = matcher.group(2);
		String[] toReturn = new String[] { className, memberName };
		return toReturn;
	}


	private StackTraceElement resymbolize(StackTraceElement ste, String strongName) {
		SymbolMap map = loadSymbolMap(strongName);
		String symbolData = map == null ? null : map.get(ste.getMethodName());

		if (symbolData != null) {
			// jsniIdent, className, memberName, sourceUri, sourceLine
			String[] parts = symbolData.split(",");
			if (parts.length == 5) {
				String[] ref = parse(parts[0].substring(0, parts[0].lastIndexOf(')') + 1));
				return new StackTraceElement(ref[0], ref[1], ste.getFileName(), ste.getLineNumber());
			}
		}
		// If anything goes wrong, just return the unobfuscated element
		return ste;
	}
}
