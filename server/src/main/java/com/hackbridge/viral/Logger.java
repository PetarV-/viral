package com.hackbridge.viral;

import java.io.PrintWriter;

/**
 * A simple verbosity logger.
 */
public class Logger {
    private static int verbosityLevel = 3;

    public static void setVerbosityLevel(int level) {
        verbosityLevel = level;
    }

    public static void log(int verbosity, String s) {
        if (verbosity <= verbosityLevel) {
            System.out.println(s);
        }
    }

    public static void logError(int verbosity, String s) {
        if (verbosity <= verbosityLevel) {
            System.err.println(s);
        }
    }

    public static void log(int verbosity, String s, PrintWriter writer) {
        if (verbosity <= verbosityLevel) {
            writer.println(s);
        }
    }

}
