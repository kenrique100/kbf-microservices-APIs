package com.akentech.kbf.kafka.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUtil {

    private static final Logger logger = LoggerFactory.getLogger(LoggingUtil.class);

    // Log info messages
    public static void logInfo(String message) {
        logger.info(message);
    }

    // Log error messages
    public static void logError(String message) {
        logger.error(message);
    }
}