package com.arcbees.gwtp.upgrader;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.JTextArea;

public class TextComponentLogger extends Handler {
	private final JTextArea text;

	TextComponentLogger(JTextArea text) {
		this.setFormatter(new SimpleFormatter());
		this.text = text;
	}

	@Override
	public void publish(LogRecord record) {
		if (isLoggable(record)) {
			
			String message = getFormatter().format(record); 
			text.append(message);
		}
	}

	@Override
	public void flush() {/**/
	}

	@Override
	public void close() throws SecurityException {/**/
	}
}