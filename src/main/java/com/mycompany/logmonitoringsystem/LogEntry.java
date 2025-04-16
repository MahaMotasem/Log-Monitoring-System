package com.mycompany.logmonitoringsystem;

public class LogEntry {
    private String timestamp;
    private String status;
    private String username;

    public LogEntry(String timestamp, String status, String username) {
        this.timestamp = timestamp;
        this.status = status;
        this.username = username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getUsername() {
        return username;
    }
}