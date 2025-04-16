package com.mycompany.logmonitoringsystem;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogWriter {
    private static final String LOG_FILE_PATH = "C://Users//AS//Desktop//LogMonitoringSystem-main//assets//loginLogs.txt";

    public static void writeLog(String username, String status) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.write(timestamp + ", Username:" + username + ", Status:" + status);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}