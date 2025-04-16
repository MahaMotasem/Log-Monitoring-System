package com.mycompany.logmonitoringsystem;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class LogFileReader {

    public static void readLogFileNormal(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            LogProcessor logProcessor = new LogProcessor(lines);
            logProcessor.call(); 
        } catch (IOException e) {
            System.err.println("Error reading file.");
        }
    }

    public static void readLogFileParallel(File file, ForkJoinPool forkJoinPool) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            LogProcessor logProcessor = new LogProcessor(lines);
            forkJoinPool.submit(logProcessor).join(); 
        } catch (IOException e) {
            System.err.println("Error reading file.");
        }
    }

    public static void compareProcessingTimes(File file, ForkJoinPool forkJoinPool) {
        long startTime, endTime, durationNormal, durationParallel;

        startTime = System.currentTimeMillis();
        readLogFileNormal(file);
        endTime = System.currentTimeMillis();
        durationNormal = endTime - startTime;

        startTime = System.currentTimeMillis();
        readLogFileParallel(file, forkJoinPool);
        endTime = System.currentTimeMillis();
        durationParallel = endTime - startTime;

        System.out.println("Processing Times Comparison:");
        System.out.println("Time taken for normal processing: " + durationNormal  + " milliseconds");
        System.out.println("Time taken for parallel processing: " + durationParallel + " milliseconds");
    }
}
