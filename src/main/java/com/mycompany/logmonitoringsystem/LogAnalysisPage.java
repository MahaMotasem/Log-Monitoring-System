package com.mycompany.logmonitoringsystem;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class LogAnalysisPage {

    private JFrame frame;
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;
    private JButton loadFileButton, pasteLogsButton, exportCSVButton, showChartButton;
    private JProgressBar progressBar;

    private int totalAttempts = 0, successCount = 0, failCount = 0;
    private final Map<String, Integer> failedAttemptsPerUser = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private ChartPanel chartPanel = null;

    public LogAnalysisPage() {
        frame = new JFrame("Log Analysis System");
        frame.setSize(1100, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(Color.decode("#f9f9f9"));

        statsLabel = new JLabel("No data loaded yet.");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statsLabel.setForeground(new Color(50, 50, 50));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        frame.add(statsLabel, BorderLayout.NORTH);

        String[] columns = {"Date", "Time", "Username", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        logTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                String status = (String) getValueAt(row, 3);
                if ("SUCCESS".equalsIgnoreCase(status)) {
                    c.setBackground(new Color(230, 255, 240));
                } else if ("FAILED".equalsIgnoreCase(status)) {
                    c.setBackground(new Color(255, 230, 230));
                } else if ("WARNING".equalsIgnoreCase(status)) {
                    c.setBackground(new Color(255, 255, 200));
                } else {
                    c.setBackground(Color.WHITE);
                }
                c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return c;
            }
        };
        logTable.setRowHeight(24);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        logTable.setShowGrid(false);
        JScrollPane scrollPane = new JScrollPane(logTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.decode("#f9f9f9"));

        loadFileButton = new JButton("Load File");
        pasteLogsButton = new JButton("Paste Logs");
        exportCSVButton = new JButton("Export CSV");
        showChartButton = new JButton("Show Chart");

        for (JButton btn : new JButton[]{loadFileButton, pasteLogsButton, exportCSVButton, showChartButton}) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btn.setFocusPainted(false);
            btn.setBackground(new Color(240, 240, 240));
            btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            buttonPanel.add(btn);
        }

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        loadFileButton.addActionListener(e -> loadLogFile());
        pasteLogsButton.addActionListener(e -> pasteLogsManually());
        exportCSVButton.addActionListener(e -> exportCSV());
        showChartButton.addActionListener(e -> toggleChart());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void loadLogFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            progressBar.setVisible(true);
            executorService.execute(() -> {
                readLogFile(file);
                SwingUtilities.invokeLater(() -> progressBar.setVisible(false));
            });
        }
    }

    private void pasteLogsManually() {
        JTextArea textArea = new JTextArea(10, 40);
        int result = JOptionPane.showConfirmDialog(frame, new JScrollPane(textArea), "Paste Logs", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String logs = textArea.getText();
            if (logs != null && !logs.trim().isEmpty()) {
                progressBar.setVisible(true);
                executorService.execute(() -> {
                    tableModel.setRowCount(0);
                    resetStats();
                    for (String line : logs.split("\n")) {
                        parseLogLine(line.trim());
                    }
                    SwingUtilities.invokeLater(() -> {
                        updateStatsLabel();
                        progressBar.setVisible(false);
                    });
                });
            }
        }
    }

    private void readLogFile(File file) {
        tableModel.setRowCount(0);
        resetStats();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                parseLogLine(line.trim());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error reading file.");
        }
        SwingUtilities.invokeLater(this::updateStatsLabel);
    }

    private synchronized void parseLogLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                String timestamp = parts[0].trim();
                String username = parts[1].split(":")[1].trim();
                String status = parts[2].split(":")[1].trim();

                String[] dateTime = timestamp.split(" ");
                String date = dateTime.length > 0 ? dateTime[0] : "";
                String time = dateTime.length > 1 ? dateTime[1] : "";

                SwingUtilities.invokeLater(() -> tableModel.addRow(new Object[]{date, time, username, status}));

                totalAttempts++;
                switch (status.toUpperCase()) {
                    case "SUCCESS":
                        successCount++;
                        break;
                    case "FAILED":
                        failCount++;
                        failedAttemptsPerUser.merge(username, 1, Integer::sum);
                        if (failedAttemptsPerUser.get(username) > 3) {
                            JOptionPane.showMessageDialog(frame, "\u26A0 Too many failed attempts by user: " + username);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error in line: " + line);
        }
    }

    private void updateStatsLabel() {
        statsLabel.setText(String.format("Total: %d | Success: %d | Failed: %d", totalAttempts, successCount, failCount));
    }

    private void resetStats() {
        totalAttempts = successCount = failCount = 0;
        failedAttemptsPerUser.clear();
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        sb.append(tableModel.getValueAt(i, j)).append(",");
                    }
                    writer.println(sb.substring(0, sb.length() - 1));
                }
                JOptionPane.showMessageDialog(frame, "Exported Successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error exporting file.");
            }
        }
    }

    private void toggleChart() {
        if (chartPanel == null) {
            chartPanel = createPieChartPanel();
            frame.add(chartPanel, BorderLayout.PAGE_END);
        } else {
            frame.remove(chartPanel);
            chartPanel = null;
        }
        frame.revalidate();
        frame.repaint();
    }

    private ChartPanel createPieChartPanel() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Success", successCount);
        dataset.setValue("Failed", failCount);

        JFreeChart chart = ChartFactory.createPieChart("Login Status Distribution", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Success", new Color(102, 204, 153));
        plot.setSectionPaint("Failed", new Color(255, 102, 102));

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(300, 300));
        return panel;
    }

}
