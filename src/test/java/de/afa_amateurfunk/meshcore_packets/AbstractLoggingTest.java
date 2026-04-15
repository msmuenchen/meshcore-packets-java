package de.afa_amateurfunk.meshcore_packets;

import org.junit.jupiter.api.BeforeAll;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * superclass for all testcases that enables logging
 * <p>
 * We are using SLF4J to allow using applications to ship whatever logging solution they want and still have it work with  our library. To keep it simple, during tests we use plain old JUL. Unfortunately JUL is already set up and configured by the initialization of the JUnit test runner, so we have to set the log level to FINEST by hand as we can't use logging.properties.
 * </p>
 *
 * @author Marco Schuschnig
 */
public abstract class AbstractLoggingTest {
    /**
     * Initialize JUL to log everything so we can see logs of failed test cases
     */
    @BeforeAll
    static void setupLogging() {
        // This does not work, but is kept here as a reference for *why* it does not work
        //System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.ALL);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
    }
}
