package com.mycompany.logmonitoringsystem;

import java.util.*;
import java.util.concurrent.Callable;

public class LogProcessor implements Callable<Map<String, List<LogEntry>>> {
    private List<String> logLines;

    public LogProcessor(List<String> logLines) {
        this.logLines = logLines;
    }

    @Override
    public Map<String, List<LogEntry>> call() {
        Map<String, List<LogEntry>> userLogEntries = new HashMap<>();
        Map<String, List<String>> failTimes = new HashMap<>();
        Map<String, Boolean> successLoginStatus = new HashMap<>();
        Map<String, Integer> failCount = new HashMap<>();

        for (String line : logLines) {
            if (line != null && !line.trim().isEmpty()) {
                String[] parts = line.split(",");
                
                if (parts.length >= 3) {
                    try {
                        String timestamp = parts[0].trim();
                        String username = "";
                        String status = parts[2].split(":")[1].trim();

                   
                        if (parts[1].contains("Username:")) {
                            username = parts[1].split(":")[1].trim();
                        }

                        if (!username.isEmpty() && !status.isEmpty()) {
                            LogEntry entry = new LogEntry(timestamp, status, username);

                            userLogEntries
                                .computeIfAbsent(username, k -> new ArrayList<>())
                                .add(entry);

                            if ("FAILED".equals(status)) {
                                failTimes
                                    .computeIfAbsent(username, k -> new ArrayList<>())
                                    .add(timestamp);
                                failCount.put(username, failCount.getOrDefault(username, 0) + 1);
                            }

                            if ("SUCCESS".equals(status)) {
                                successLoginStatus.put(username, true);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error in line: " + line);
                    }
                }
            }
        }

        for (String user : userLogEntries.keySet()) {
            int failedCount = failCount.getOrDefault(user, 0);
            boolean successAfterFail = successLoginStatus.getOrDefault(user, false);
            List<String> failTimeList = failTimes.get(user);
            
            if (failedCount > 0 && successAfterFail) {
                System.out.println(user + " - Normal: " + failedCount + " failed attempt(s), then success.");
            } 
            else if (failedCount > 1 && !successAfterFail) {
                System.out.println(user + " - Suspicious activity detected (Possible hacker). Times: " + String.join(", ", failTimeList));
            } 
            else if (failedCount == 0) {
                System.out.println(user + " - Normal: No failed login attempts.");
            } 
            else {
                System.out.println(user + " - Failed once, no successful login. Times: " + String.join(", ", failTimeList));
            }
        }

        return userLogEntries;
    }
}
