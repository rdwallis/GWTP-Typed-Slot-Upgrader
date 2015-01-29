package com.arcbees.gwtp.upgrader;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;

public class SlotCollector {

	private final static Logger LOGGER = Logger.getGlobal();

	public SlotCollector(File rootDir) {
		PresenterCollector presenterCollector = new PresenterCollector();
		processDir(rootDir, presenterCollector);
		Set<String> allPresenters = presenterCollector.getPresenters();
		LOGGER.info("Found the following Presenter Classes:\n\n" + allPresenters);
		
		ContentSlotRewriter slotFinder = new ContentSlotRewriter(allPresenters);
		processDir(rootDir, slotFinder);
		
		ObjectSlotRewriter objectSlotRewriter = new ObjectSlotRewriter(slotFinder.getSlotNames());
		processDir(rootDir, objectSlotRewriter);
		
	}

	private void processDir(File dir, AbstractReWriter cuProcessor) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				processDir(file, cuProcessor);
			} else if (file.getName().endsWith(".java")) {
				cuProcessor.processJavaFile(file);
			}
		}
	}
}
