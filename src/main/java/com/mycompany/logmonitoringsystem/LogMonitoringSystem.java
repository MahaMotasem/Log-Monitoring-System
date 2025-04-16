package com.mycompany.logmonitoringsystem;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.*;

public class LogMonitoringSystem {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage();
            loginPage.setVisible(true);
        });

        
        File logFile = new File("C://Users//AS//Desktop//LogMonitoringSystem-main//assets//loginLogs.txt");

        
        int numberOfThreads = Runtime.getRuntime().availableProcessors();  
        
        ForkJoinPool forkJoinPool = new ForkJoinPool(numberOfThreads);

        LogFileReader.compareProcessingTimes(logFile, forkJoinPool);
    }
}
