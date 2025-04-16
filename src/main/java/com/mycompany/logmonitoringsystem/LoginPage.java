package com.mycompany.logmonitoringsystem;

import java.awt.*;
import javax.swing.*;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginPage() {
        setTitle("Login Page");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon icon = new ImageIcon("C://Users//AS//Downloads//download (1).jpeg");
                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    System.out.println("Failed to load image.");
                }
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        backgroundPanel.add(usernameLabel, gbc);

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 30)); 
        gbc.gridx = 1;
        backgroundPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        backgroundPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30)); 
        gbc.gridx = 1;
        backgroundPanel.add(passwordField, gbc);

        loginButton = new JButton("Login");
        gbc.gridx = 1;
        gbc.gridy = 2;
        backgroundPanel.add(loginButton, gbc);

        statusLabel = new JLabel("");
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 1;
        gbc.gridy = 3;
        backgroundPanel.add(statusLabel, gbc);

        add(backgroundPanel);

        loginButton.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (validateLogin(username, password)) {
            statusLabel.setText("Login Successful!");
            statusLabel.setForeground(Color.GREEN);
            LogWriter.writeLog(username, "SUCCESS");
            new LogAnalysisPage(); 
            dispose(); 
        } else {
            statusLabel.setText("Invalid credentials, please try again.");
            statusLabel.setForeground(Color.RED);
            LogWriter.writeLog(username, "FAILED");
        }
    }

    private boolean validateLogin(String username, String password) {
        return "admin".equals(username) && "admin".equals(password);
    }

    
}

