package com.tradingjournal;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        // Create data directory first
        try {
            System.out.println("Starting Trading Journal application...");
            java.io.File dataDir = new java.io.File(System.getProperty("user.home") + 
                java.io.File.separator + "TradingJournalData");
            if (!dataDir.exists()) {
                boolean created = dataDir.mkdirs();
                System.out.println("Creating data directory: " + (created ? "success" : "failed"));
            }
        } catch (Exception e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
        
        // Launch the application
        Application.launch(TradingJournalApp.class, args);
    }
}