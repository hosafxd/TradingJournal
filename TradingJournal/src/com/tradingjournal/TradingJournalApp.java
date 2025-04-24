package com.tradingjournal;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Cursor;
import javafx.scene.shape.Rectangle;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Platform;

public class TradingJournalApp extends Application {

    private AccountManager accountManager;
    private ComboBox<Account> accountSelector;

    // Model classes
    public static class Trade implements Serializable {

        private static final long serialVersionUID = 1L;
  private double balance;
        private String status;
        private LocalDate date;
        private String duration;
        private String symbol;
        private double entry;
        private double exit;
        private double size;
        private String side;
        private double returns;
        private double returnPercentage;
        private String setup;
        private String entryType;
        private List<String> images;
        private String notes;
   public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
        public Trade() {
            this.images = new ArrayList<>();
        }

        public Trade(String status, LocalDate date, String duration, String symbol, 
                double entry, double exit, double size, String side, 
                String setup, String entryType, double returns, double balance) {
        this.status = status;
        this.date = date;
        this.duration = duration;
        this.symbol = symbol;
        this.entry = entry;
        this.exit = exit;
        this.size = size;
        this.side = side;
        this.setup = setup;
        this.entryType = entryType;
        this.returns = returns;
        this.balance = balance;
        this.images = new ArrayList<>();
        
        // Calculate percentage
        updateReturnPercentage();
        
        // Set status based on returns
        this.status = returns >= 0 ? "WIN" : "LOSS";
    }

public void updateReturnPercentage() {
        if (entry > 0 && size > 0) { if (side.equals("LONG")) {
               // returns = (exit - entry) * size;
                returnPercentage = ((exit - entry) / entry) * 100;
            } else if (side.equals("SHORT")) {
                //returns = (entry - exit) * size;
                returnPercentage = ((entry - exit) / entry) * 100;
            }} else {
            returnPercentage = 0;
        }
    }
        public void calculateReturns() {
            if (side.equals("LONG")) {
               // returns = (exit - entry) * size;
                returnPercentage = ((exit - entry) / entry) * 100;
            } else if (side.equals("SHORT")) {
                //returns = (entry - exit) * size;
                returnPercentage = ((entry - exit) / entry) * 100;
            }

            // Set status based on returns
            status = returnPercentage > 0 ? "WIN" : "LOSS";
        }

        // Getters and Setters
        public String getStatus() {
            calculateReturns();
            return status;
        }

        public void setStatus(String status) {
           calculateReturns();
            this.status = status;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public double getEntry() {
            return entry;
        }

        public void setEntry(double entry) {
            
            this.entry = entry;
                   updateReturnPercentage();
          
        }
        public void setReturns(double returns) {
        this.returns = returns;
        
        // Update status based on returns
        this.status = returns >= 0 ? "WIN" : "LOSS";
        
        // Recalculate percentage
          updateReturnPercentage();
        
    }

        public double getExit() {
            return exit;
        }

        public void setExit(double exit) {
            this.exit = exit;
            
        }

        public double getSize() {
            return size;
        }

        public void setSize(double size) {
            this.size = size;
            
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
            calculateReturns();
        }

        public double getReturns() {
            return returns;
        }

        public double getReturnPercentage() {
            return returnPercentage;
        }

        public String getSetup() {
            return setup;
        }

        public void setSetup(String setup) {
            this.setup = setup;
        }

        public String getEntryType() {
            return entryType;
        }

        public void setEntryType(String entryType) {
            this.entryType = entryType;
        }

        public List<String> getImages() {
            return images;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }

        public void addImage(String imagePath) {
            this.images.add(imagePath);
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class JournalSettings implements Serializable {

        private static final long serialVersionUID = 2L;
    
    private double accountBalance;
    private double averageRisk;
    private String tradingRules;
    private List<String> symbols;
    private List<SetupEntryDetails> setupDetails;
    private List<SetupEntryDetails> entryTypeDetails;

        public JournalSettings() {
            this.symbols = new ArrayList<>(Arrays.asList("XAUUSD", "XLMUSD", "EURUSD", "GBPUSD", "NAS100"));
             // Initialize with default setups and entry types
        this.setupDetails = new ArrayList<>();
        for (String setup : Arrays.asList("setup1", "setup2", "setup3", "setup4")) {
            this.setupDetails.add(new SetupEntryDetails(setup));
        }
        
        this.entryTypeDetails = new ArrayList<>();
        for (String entry : Arrays.asList("entry1", "entry2", "entry3", "entry4")) {
            this.entryTypeDetails.add(new SetupEntryDetails(entry));
        }
        
        this.accountBalance = 10000.0;
        this.averageRisk = 1.0;
            this.tradingRules = "1. Always use stop loss\n2. Risk max 1% per trade\n3. Follow the trading plan";
        }

        // Getters and Setters
        public double getAccountBalance() {
            return accountBalance;
        }

        public void setAccountBalance(double accountBalance) {
            this.accountBalance = accountBalance;
        }

        public double getAverageRisk() {
            return averageRisk;
        }

        public void setAverageRisk(double averageRisk) {
            this.averageRisk = averageRisk;
        }

        public String getTradingRules() {
            return tradingRules;
        }

        public void setTradingRules(String tradingRules) {
            this.tradingRules = tradingRules;
        }

        public List<String> getSymbols() {
            return symbols;
        }

        public void setSymbols(List<String> symbols) {
            this.symbols = symbols;
        }

        public void addSymbol(String symbol) {
            this.symbols.add(symbol);
        }

       public List<String> getSetupTypes() {
        return setupDetails.stream().map(SetupEntryDetails::getName).collect(Collectors.toList());
    }

      

          public List<String> getEntryTypes() {
        return entryTypeDetails.stream().map(SetupEntryDetails::getName).collect(Collectors.toList());
    }
public List<SetupEntryDetails> getSetupDetails() {
        return setupDetails;
    }
    
    public List<SetupEntryDetails> getEntryTypeDetails() {
        return entryTypeDetails;
    }
        // Add methods to set details lists
    public void setSetupDetails(List<SetupEntryDetails> setupDetails) {
        this.setupDetails = setupDetails;
    }
    
    public void setEntryTypeDetails(List<SetupEntryDetails> entryTypeDetails) {
        this.entryTypeDetails = entryTypeDetails;
    } 
    }

    private class DataManager {
        // In both AccountManager and DataManager classes

        private static final String DATA_DIR = "E:\\netbeansprojects\\TradingJournal\\TradingJournalData";
        private static final String TRADES_FILE = "trades.dat";
        private static final String SETTINGS_FILE = "settings.dat";
        private static final String IMAGES_DIR = "images";
        // Add accountManager reference
        private AccountManager accountManager;
        private JournalSettings settings;
        private ObservableList<Trade> trades;

        private void ensureDirectoryExists(String path) {
            File dir = new File(path);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    System.err.println("Failed to create directory: " + path);
                } else {
                    System.out.println("Successfully created directory: " + path);
                }
            }
        }

        private String getAccountDir() {
            return DATA_DIR + File.separator + accountManager.getCurrentAccount().getId();
        }

        public DataManager(AccountManager accountManager) {
    this.accountManager = accountManager;
    this.trades = FXCollections.observableArrayList();
    
    // Always create a new settings object first
    this.settings = new JournalSettings();
    
    try {
        // Create main data directory
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            System.out.println("Creating main data directory: " + (created ? "success" : "failed") + " - " + DATA_DIR);
        }
        
        // Create account directory
        String accountDirPath = getAccountDir();
        System.out.println("Account directory path: " + accountDirPath);
        ensureDirectoryExists(accountDirPath);
        
        // Create images directory for account
        String imagesDirPath = accountDirPath + File.separator + IMAGES_DIR;
        ensureDirectoryExists(imagesDirPath);
        
        // Load data only after ensuring directories exist
        loadData();
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Error in DataManager constructor: " + e.getMessage());
    }
}

        public void loadData() {
            try {
                loadSettings();
                loadTrades();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in loadData: " + e.getMessage() + " at " + e.getStackTrace()[0]);
            }
        }

        @SuppressWarnings("unchecked")
        private void loadTrades() {
            try {
                String filePath = getAccountDir() + File.separator + TRADES_FILE;
                System.out.println("Loading trades from: " + filePath);
                File tradesFile = new File(filePath);

                if (tradesFile.exists()) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tradesFile))) {
                        List<Trade> loadedTrades = (List<Trade>) ois.readObject();
                        trades.setAll(loadedTrades);
                        System.out.println("Successfully loaded " + loadedTrades.size() + " trades");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error loading trades from existing file: " + e.getMessage() + " at " + e.getStackTrace()[0]);
                        showAlertHere("Error", "Failed to load trades data: " + e.getMessage());
                    }
                } else {
                    System.out.println("No trades file exists yet at: " + filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in loadTrades: " + e.getMessage() + " at " + e.getStackTrace()[0]);
                showAlertHere("Error", "Failed to load trades data: " + e.getMessage());
            }
        }

        private void loadSettings() {
            try {
                String filePath = getAccountDir() + File.separator + SETTINGS_FILE;
                System.out.println("Loading settings from: " + filePath);
                File settingsFile = new File(filePath);

                if (settingsFile.exists()) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(settingsFile))) {
                        settings = (JournalSettings) ois.readObject();
                        System.out.println("Successfully loaded settings");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error loading settings from existing file: " + e.getMessage() + " at " + e.getStackTrace()[0]);
                        showAlertHere("Error", "Failed to load settings data: " + e.getMessage());
                    }
                } else {
                    System.out.println("No settings file exists yet at: " + filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in loadSettings: " + e.getMessage() + " at " + e.getStackTrace()[0]);
                showAlertHere("Error", "Failed to load settings data: " + e.getMessage());
            }
        }

        public void saveData() {
            try {
                System.out.println("Saving all data for account: " + accountManager.getCurrentAccount().getName());
                saveSettings();
                saveTrades();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in saveData: " + e.getMessage() + " at " + e.getStackTrace()[0]);
            }
        }

        private void saveTrades() {
            try {
                String dirPath = getAccountDir();
                System.out.println("Saving trades to directory: " + dirPath);
                ensureDirectoryExists(dirPath);

                String filePath = dirPath + File.separator + TRADES_FILE;
                System.out.println("Trades file path: " + filePath);
                File tradesFile = new File(filePath);

                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tradesFile))) {
                    List<Trade> tradeList = new ArrayList<>(trades);
                    oos.writeObject(tradeList);
                    System.out.println("Successfully saved " + tradeList.size() + " trades");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error writing trades data: " + e.getMessage() + " at " + e.getStackTrace()[0]);
                    showAlertHere("Error", "Failed to save trades data: " + e.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in saveTrades: " + e.getMessage() + " at " + e.getStackTrace()[0]);
                showAlertHere("Error", "Failed to save trades data: " + e.getMessage());
            }
        }

        private void saveSettings() {
            try {
                String dirPath = getAccountDir();
                System.out.println("Saving settings to directory: " + dirPath);
                ensureDirectoryExists(dirPath);

                String filePath = dirPath + File.separator + SETTINGS_FILE;
                System.out.println("Settings file path: " + filePath);
                File settingsFile = new File(filePath);

                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(settingsFile))) {
                    oos.writeObject(settings);
                    System.out.println("Successfully saved settings");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error writing settings data: " + e.getMessage() + " at " + e.getStackTrace()[0]);
                    showAlertHere("Error", "Failed to save settings data: " + e.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in saveSettings: " + e.getMessage() + " at " + e.getStackTrace()[0]);
                showAlertHere("Error", "Failed to save settings data: " + e.getMessage());
            }
        }

        public String saveImage(File imageFile) {
            try {
                String targetPath = getAccountDir() + File.separator + IMAGES_DIR + File.separator;
                System.out.println("Saving image to: " + targetPath);
                ensureDirectoryExists(targetPath);

                String filename = System.currentTimeMillis() + "_" + imageFile.getName();
                Path target = Paths.get(targetPath + filename);
                System.out.println("Image target path: " + target);

                try {
                    Files.copy(imageFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Successfully saved image: " + target);
                    return target.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Error copying image file: " + e.getMessage() + " at " + e.getStackTrace()[0]);
                    showAlertHere("Error", "Failed to save image: " + e.getMessage());
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in saveImage: " + e.getMessage() + " at " + e.getStackTrace()[0]);
                showAlertHere("Error", "Failed to save image: " + e.getMessage());
                return null;
            }
        }

        public ObservableList<Trade> getTrades() {
            return trades;
        }

        public void addTrade(Trade trade) {
            trades.add(trade);
            saveTrades();
        }

        public void updateTrade(Trade trade) {
            saveTrades();
          // Force the ObservableList to refresh
    int index = trades.indexOf(trade);
    if (index >= 0) {
        Trade current = trades.get(index);
        // This triggers a list update event
        trades.set(index, current);
    }
}

        public void deleteTrade(Trade trade) {
            trades.remove(trade);
            saveTrades();
        }

        public JournalSettings getSettings() {
            return settings;
        }

        public void updateSettings() {
            saveSettings();
        }
    }

    // UI Components
    private Stage primaryStage;
    private Scene mainScene;
    private BorderPane root;
    private TableView<Trade> tradeTable;
    private DataManager dataManager;
    private FilteredList<Trade> filteredTrades;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.accountManager = new AccountManager();
        this.dataManager = new DataManager(accountManager);
        this.filteredTrades = new FilteredList<>(dataManager.getTrades(), p -> true);

        initUI();

        primaryStage.setTitle("Trading Journal - " + accountManager.getCurrentAccount().getName());
        primaryStage.setScene(mainScene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();
    }
private void showAlertHere(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    
    // Style the dialog
    DialogPane dialogPane = alert.getDialogPane();
    dialogPane.setStyle("-fx-background-color: #2d2d2d;");
    dialogPane.lookupAll(".label").forEach(node -> {
        ((Label)node).setStyle("-fx-text-fill: white;");
    });
    
    alert.showAndWait();
}
    private void initUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e1e;");

        // Top section - Menu bar with account selector
        HBox topContainer = new HBox(10);
        topContainer.setStyle("-fx-background-color: #2d2d2d;");
        topContainer.setAlignment(Pos.CENTER_LEFT);
        topContainer.setPadding(new Insets(5, 10, 5, 10));

        // Account selector
        Label accountLabel = new Label("Account:");
        accountLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); // Make label white and bold

        accountSelector = new ComboBox<>();
        accountSelector.setItems(FXCollections.observableArrayList(accountManager.getAccounts()));
        accountSelector.setValue(accountManager.getCurrentAccount());
        accountSelector.setStyle("-fx-base: #3a3a3a; -fx-control-inner-background: #3a3a3a; -fx-background-color: #3a3a3a; -fx-text-fill: white;");
        accountSelector.setPrefWidth(200);
        // Add this to make the dropdown items also visible
        accountSelector.setCellFactory(lv -> new ListCell<Account>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: #e0e0e0;");
                }
            }
        });
        // Add this code for accountSelector after you set its style
accountSelector.setButtonCell(new ListCell<Account>() {
    @Override
    protected void updateItem(Account item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            setText(item.toString());
            setStyle("-fx-text-fill: white;");
        }
    }
});

        accountSelector.setOnAction(e -> {
            Account selectedAccount = accountSelector.getValue();
            if (selectedAccount != null && !selectedAccount.equals(accountManager.getCurrentAccount())) {
                switchAccount(selectedAccount);
            }
        });

        Button addAccountBtn = new Button("+");
        addAccountBtn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        addAccountBtn.setOnAction(e -> showAddAccountDialog());

        Button removeAccountBtn = new Button("-");
        removeAccountBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
        removeAccountBtn.setOnAction(e -> {
            if (accountManager.getAccounts().size() > 1) {
                Account accountToRemove = accountSelector.getValue();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Delete");
                alert.setHeaderText("Delete Account");
                alert.setContentText("Are you sure you want to delete the account \""
                        + accountToRemove.getName() + "\"? All associated data will be lost.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    accountManager.removeAccount(accountToRemove);
                    accountSelector.setItems(FXCollections.observableArrayList(accountManager.getAccounts()));
                    accountSelector.setValue(accountManager.getCurrentAccount());
                    switchAccount(accountManager.getCurrentAccount());
                }
            } else {
                showAlertHere("Cannot Delete", "You cannot delete the only account. Create a new account first.");
            }
        });

        HBox accountBox = new HBox(10);
        accountBox.setAlignment(Pos.CENTER_LEFT);
       accountBox.getChildren().addAll(accountLabel, accountSelector);
        MenuBar menuBar = createMenuBar();

        topContainer.getChildren().addAll(accountBox, menuBar);
        HBox.setHgrow(menuBar, Priority.ALWAYS);

        root.setTop(topContainer);

        // Left section - Sidebar
        VBox sidebar = createSidebar();
        BorderPane.setMargin(sidebar, new Insets(0, 5, 0, 0));
root.setLeft(sidebar);

        // Center section - Trade table
        tradeTable = createTradeTable(); // This directly assigns to the class field
        VBox tableContainer = createTradeTableContainer(tradeTable); // New helper method
      BorderPane.setMargin(tableContainer, new Insets(0, 0, 0, 5));
root.setCenter(tableContainer);

        mainScene = new Scene(root, 1200, 800);
        // Add CSS for styling
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }

    private void showAddAccountDialog() {
          TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Add Account");
    dialog.setHeaderText("Create New Trading Account");
    dialog.setContentText("Enter account name:");
    
    // Apply custom styling to the dialog
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setStyle("-fx-background-color: #2d2d2d;");
    
    // Fix label text color - this targets all labels in the dialog
    dialogPane.lookupAll(".label").forEach(node -> {
        ((Label)node).setStyle("-fx-text-fill: white;");
    });
    
    // Fix the content text specifically
    Label contentLabel = (Label) dialogPane.lookup(".content.label");
    if (contentLabel != null) {
        contentLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
    }
    
    // Fix the header text
    Label headerLabel = (Label) dialogPane.lookup(".header-panel > .label");
    if (headerLabel != null) {
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
    }
    
    // Style the text field
    TextField textField = dialog.getEditor();
    textField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-prompt-text-fill: lightgray;");
    
    // Style the buttons
    dialogPane.lookupAll(".button").forEach(node -> {
        Button button = (Button) node;
        button.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
    });
    
    // Add extra CSS for dialog
    dialogPane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    
    Optional<String> result = dialog.showAndWait();
       
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                accountManager.addAccount(name.trim());
                accountSelector.setItems(FXCollections.observableArrayList(accountManager.getAccounts()));
                accountSelector.setValue(accountManager.getAccounts().get(accountManager.getAccounts().size() - 1));
                switchAccount(accountSelector.getValue());
            }
        });
    }

    private VBox createTradeTableContainer(TableView<Trade> table) {
        // Create search functionality

        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(10));
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search trades...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All", "WIN", "LOSS");
        filterCombo.setValue("All");
        filterCombo.setStyle("-fx-base: #3a3a3a; -fx-control-inner-background: #3a3a3a; -fx-background-color: #3a3a3a; -fx-text-fill: white;");
        Label searchLabel = new Label("Search:");
        searchLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
filterCombo.setButtonCell(new ListCell<String>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            setText(item);
            setStyle("-fx-text-fill: white;");
        }
    }
});
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Add the search functionality as before
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Search functionality code here (copy from original method)
        });

        filterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Filter functionality code here (copy from original method)
        });

        searchBox.getChildren().addAll(searchLabel, searchField, filterLabel, filterCombo);
        VBox tableContainer = new VBox();
        tableContainer.setPadding(new Insets(0, 0, 0, 5)); // Add left padding of 10px
   
        tableContainer.getChildren().addAll(searchBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    tableContainer.getStyleClass().add("table-container");
    table.getStyleClass().add("trade-table");
        return tableContainer;
    }

    // In TradingJournalApp class, fix the switchAccount method:
private void switchAccount(Account account) {
    // Save current account data before switching
    if (dataManager != null) {
        dataManager.saveData();
    }
    
    // Update current account in account manager
    accountManager.setCurrentAccount(account);
    
    // Create new data manager for selected account
    dataManager = new DataManager(accountManager);
    filteredTrades = new FilteredList<>(dataManager.getTrades(), p -> true);
    
    // Update UI
    root.setLeft(createSidebar());
    
    // Update the table
    tradeTable = createTradeTable();
    VBox tableContainer = createTradeTableContainer(tradeTable);
    root.setCenter(tableContainer);
    
    // Update window title
    primaryStage.setTitle("Trading Journal - " + accountManager.getCurrentAccount().getName());
    
    // Force UI refresh
    Platform.runLater(() -> {
        root.requestLayout();
    });
}

private void showManageAccountsDialog() {
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initOwner(primaryStage);
    dialog.setTitle("Manage Trading Accounts");
    
    BorderPane mainPane = new BorderPane();
    mainPane.setPadding(new Insets(15));
    mainPane.setStyle("-fx-background-color: #2d2d2d;");
    
    // Title
    Label titleLabel = new Label("Manage Trading Accounts");
    titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
    BorderPane.setAlignment(titleLabel, Pos.CENTER);
    mainPane.setTop(titleLabel);
    
    // Accounts list
    ListView<Account> accountsListView = new ListView<>();
    accountsListView.setItems(FXCollections.observableArrayList(accountManager.getAccounts()));
    accountsListView.setCellFactory(lv -> new ListCell<Account>() {
        @Override
        protected void updateItem(Account item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.toString());
                
                // Check if this is the current account
                boolean isCurrent = item.equals(accountManager.getCurrentAccount());
                
                // Set style based on selection state
                if (isSelected()) {
                    // Selected item: use black text on blue background
                    setStyle("-fx-text-fill: black; -fx-background-color: #4da6ff;");
                } else if (isCurrent) {
                    // Current account (not selected): use white text on darker blue
                    setStyle("-fx-text-fill: black; -fx-background-color: #0078d7;");
                } else {
                    // Regular item: use white text on dark background (fixed this to white)
                    setStyle("-fx-text-fill: black;");
                }
            }
        }
        
        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
            
            // Force update whenever selection changes
            updateItem(getItem(), isEmpty());
        }
    }); 
    accountsListView.setStyle("-fx-background-color: #3a3a3a;");
    
    // Buttons for account management
    Button addButton = new Button("Add Account");
    addButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
    addButton.setOnAction(e -> {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Add Account");
        inputDialog.setHeaderText("Create New Trading Account");
        inputDialog.setContentText("Enter account name:");
        
        // Style the dialog
        DialogPane dialogPane = inputDialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2d2d2d;");
        dialogPane.lookupAll(".label").forEach(node -> {
            ((Label)node).setStyle("-fx-text-fill: white;");
        });
        
        TextField textField = inputDialog.getEditor();
        textField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");
        
        // Get result
        Optional<String> result = inputDialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                accountManager.addAccount(name.trim());
                
                // Refresh list in the dialog
                accountsListView.setItems(FXCollections.observableArrayList(accountManager.getAccounts()));
                
                // Update the main UI account selector - ADDED THIS LINE
                accountSelector.setItems(FXCollections.observableArrayList(accountManager.getAccounts()));
            }
        });
    });
    
    Button removeButton = new Button("Remove Selected");
    removeButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
    removeButton.setOnAction(e -> {
        Account selectedAccount = accountsListView.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            if (accountManager.getAccounts().size() > 1) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Delete");
                confirmAlert.setHeaderText("Delete Account");
                confirmAlert.setContentText("Are you sure you want to delete the account \"" + 
                                         selectedAccount.getName() + "\"?\nAll associated data will be lost.");
                
                // Style the alert
                DialogPane alertPane = confirmAlert.getDialogPane();
                alertPane.setStyle("-fx-background-color: #2d2d2d;");
                alertPane.lookupAll(".label").forEach(node -> {
                    ((Label)node).setStyle("-fx-text-fill: white;");
                });
                
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    Account currentAccount = accountManager.getCurrentAccount();
                    accountManager.removeAccount(selectedAccount);
                    
                    // Update selector in main UI if needed
                    accountSelector.setItems(FXCollections.observableArrayList(accountManager.getAccounts()));
                    accountSelector.setValue(accountManager.getCurrentAccount());
                    
                    // If current account was deleted, switch to the new current
                    if (currentAccount.equals(selectedAccount)) {
                        switchAccount(accountManager.getCurrentAccount());
                    }
                    
                    // Refresh list
                    accountsListView.setItems(FXCollections.observableArrayList(accountManager.getAccounts()));
                }
            } else {
                showAlertHere("Cannot Delete", "You must have at least one account.");
            }
        } else {
            showAlertHere("No Selection", "Please select an account to remove.");
        }
    });
    
    Button switchToButton = new Button("Switch To Selected");
    switchToButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
    switchToButton.setOnAction(e -> {
        Account selectedAccount = accountsListView.getSelectionModel().getSelectedItem();
        if (selectedAccount != null && !selectedAccount.equals(accountManager.getCurrentAccount())) {
            accountSelector.setValue(selectedAccount);
            switchAccount(selectedAccount);
            dialog.close();
        } else if (selectedAccount == null) {
            showAlertHere("No Selection", "Please select an account to switch to.");
        }
    });
    
    // Button layout
    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().addAll(addButton, removeButton, switchToButton);
    
    // Close button at bottom
    Button closeButton = new Button("Close");
    closeButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
    closeButton.setOnAction(e -> dialog.close());
    HBox closeBox = new HBox(closeButton);
    closeBox.setAlignment(Pos.CENTER_RIGHT);
    
    // Vertical layout for list and buttons
    VBox contentBox = new VBox(15);
    contentBox.getChildren().addAll(accountsListView, buttonBox, closeBox);
    mainPane.setCenter(contentBox);
    
    // Create scene and show dialog
    Scene scene = new Scene(mainPane, 450, 400);
    dialog.setScene(scene);
    dialog.showAndWait();
}

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #2d2d2d;");

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem exportItem = new MenuItem("Export Data");
        exportItem.setOnAction(e -> exportData());
        MenuItem importItem = new MenuItem("Import Data");
        importItem.setOnAction(e -> importData());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(exportItem, importItem, new SeparatorMenuItem(), exitItem);

        // Settings menu
        Menu settingsMenu = new Menu("Settings");
        
        MenuItem accountItem = new MenuItem("Account Settings");
        accountItem.setOnAction(e -> showSettingsDialog());
        MenuItem symbolsItem = new MenuItem("Manage Symbols");
        symbolsItem.setOnAction(e -> manageSymbols());
        MenuItem setupsItem = new MenuItem("Manage Setups & Entry Types");
setupsItem.setOnAction(e -> manageSetups());
      MenuItem manageAccountsItem = new MenuItem("Manage Accounts");
manageAccountsItem.setOnAction(e -> showManageAccountsDialog());

// Update the items list:
settingsMenu.getItems().addAll(accountItem, symbolsItem, setupsItem, new SeparatorMenuItem(), manageAccountsItem);

        // Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, settingsMenu, helpMenu);
        return menuBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(5));
        sidebar.setStyle("-fx-background-color: #252525;");
        sidebar.setPrefWidth(200);

        Label accountLabel = new Label("Account Overview");
        accountLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        VBox accountInfo = new VBox(5);
        Label balanceLabel = new Label("Balance: $" + String.format("%.2f", dataManager.getSettings().getAccountBalance()));
        balanceLabel.setStyle("-fx-text-fill: #cccccc;");
        Label riskLabel = new Label("Avg Risk: " + String.format("%.2f", dataManager.getSettings().getAverageRisk()) + "%");
        riskLabel.setStyle("-fx-text-fill: #cccccc;");

        accountInfo.getChildren().addAll(balanceLabel, riskLabel);

        Label rulesLabel = new Label("Trading Rules");
        rulesLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        TextArea rulesArea = new TextArea(dataManager.getSettings().getTradingRules());
        rulesArea.setEditable(false);
        rulesArea.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: #cccccc;");
        rulesArea.setPrefHeight(150);

        Button addTradeBtn = new Button("Add Trade");
        addTradeBtn.setMaxWidth(Double.MAX_VALUE);
        addTradeBtn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        addTradeBtn.setOnAction(e -> showAddTradeDialog());

        Button dashboardBtn = new Button("Dashboard");
        dashboardBtn.setMaxWidth(Double.MAX_VALUE);
        dashboardBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
        dashboardBtn.setOnAction(e -> showDashboard());

        Button calendarBtn = new Button("Calendar View");
        calendarBtn.setMaxWidth(Double.MAX_VALUE);
        calendarBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
        calendarBtn.setOnAction(e -> showCalendarView());

        sidebar.getChildren().addAll(accountLabel, accountInfo, rulesLabel, rulesArea,
                new Separator(), addTradeBtn, dashboardBtn, calendarBtn);

        return sidebar;
    }

    private TableView<Trade> createTradeTable() {
        TableView<Trade> table = new TableView<>(filteredTrades);
        table.setStyle("-fx-background-color: #2d2d2d; -fx-control-inner-background: #3a3a3a;");

        // Create columns
        TableColumn<Trade, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<Trade, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("WIN")) {
                        setStyle("-fx-background-color: #1e5531; -fx-text-fill: white;");
                    } else {
                        setStyle("-fx-background-color: #5e1e1e; -fx-text-fill: white;");
                    }
                }
            }
        });

        TableColumn<Trade, LocalDate> dateCol = new TableColumn<>("DATE");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(column -> new TableCell<Trade, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        TableColumn<Trade, String> durationCol = new TableColumn<>("DURATION");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

        TableColumn<Trade, String> symbolCol = new TableColumn<>("SYMBOL");
        symbolCol.setCellValueFactory(new PropertyValueFactory<>("symbol"));

        TableColumn<Trade, Double> entryCol = new TableColumn<>("ENTRY");
        entryCol.setCellValueFactory(new PropertyValueFactory<>("entry"));
        entryCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        TableColumn<Trade, Double> exitCol = new TableColumn<>("EXIT");
        exitCol.setCellValueFactory(new PropertyValueFactory<>("exit"));
        exitCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        TableColumn<Trade, Integer> sizeCol = new TableColumn<>("SIZE");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));

        TableColumn<Trade, String> sideCol = new TableColumn<>("SIDE");
        sideCol.setCellValueFactory(new PropertyValueFactory<>("side"));

        TableColumn<Trade, Double> returnsCol = new TableColumn<>("RETURNS");
        returnsCol.setCellValueFactory(new PropertyValueFactory<>("returns"));
        returnsCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%.2f", item));
                    if (item > 0) {
                        setStyle("-fx-text-fill: #4caf50;");
                    } else {
                        setStyle("-fx-text-fill: #f44336;");
                    }
                }
            }
        });

      TableColumn<Trade, Double> balanceCol = new TableColumn<>("BALANCE");
balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
balanceCol.setCellFactory(column -> new TableCell<Trade, Double>() {
    @Override
    protected void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setStyle("");
        } else {
            setText(String.format("$%.2f", item));
            setStyle("-fx-text-fill: white;");
        }
    }
});


        TableColumn<Trade, String> setupCol = new TableColumn<>("SETUPS");
        setupCol.setCellValueFactory(new PropertyValueFactory<>("setup"));

        TableColumn<Trade, String> entryTypeCol = new TableColumn<>("ENTRY");
        entryTypeCol.setCellValueFactory(new PropertyValueFactory<>("entryType"));

        TableColumn<Trade, Void> actionsCol = new TableColumn<>("ACTIONS");
        
        actionsCol.setPrefWidth(200);  // Make it wider - adjust as needed

actionsCol.setResizable(true); // Allow users to resize if needed
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button detailsBtn = new Button("Details");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, detailsBtn, editBtn, deleteBtn);

            {
                detailsBtn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
                detailsBtn.setOnAction(event -> {
                    Trade trade = getTableView().getItems().get(getIndex());
                    showTradeDetails(trade);
                });

                editBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
                editBtn.setOnAction(event -> {
                    Trade trade = getTableView().getItems().get(getIndex());
                    editTrade(trade);
                });

                deleteBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    Trade trade = getTableView().getItems().get(getIndex());
                    deleteTrade(trade);
                });

                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

   table.getColumns().addAll(statusCol, dateCol, durationCol, symbolCol, entryCol, 
                        exitCol, sizeCol, sideCol, returnsCol, balanceCol, 
                        setupCol, entryTypeCol, actionsCol);
        // Add search functionality
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(5));
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search trades...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All", "WIN", "LOSS");
        filterCombo.setValue("All");
        filterCombo.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");
        filterCombo.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #e0e0e0;");
                }
            }
        });
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredTrades.setPredicate(trade -> {
                // If filter text is empty, display all trades
                if (newValue == null || newValue.isEmpty()) {
                    return applyStatusFilter(trade, filterCombo.getValue());
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (trade.getSymbol().toLowerCase().contains(lowerCaseFilter)) {
                    return applyStatusFilter(trade, filterCombo.getValue());
                } else if (trade.getSetup().toLowerCase().contains(lowerCaseFilter)) {
                    return applyStatusFilter(trade, filterCombo.getValue());
                } else if (trade.getEntryType().toLowerCase().contains(lowerCaseFilter)) {
                    return applyStatusFilter(trade, filterCombo.getValue());
                }
                return false;
            });
        });

        filterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredTrades.setPredicate(trade -> {
                // Apply text filter
                String searchText = searchField.getText();
                if (searchText == null || searchText.isEmpty()) {
                    return applyStatusFilter(trade, newValue);
                }

                String lowerCaseFilter = searchText.toLowerCase();

                if (trade.getSymbol().toLowerCase().contains(lowerCaseFilter)) {
                    return applyStatusFilter(trade, newValue);
                } else if (trade.getSetup().toLowerCase().contains(lowerCaseFilter)) {
                    return applyStatusFilter(trade, newValue);
                } else if (trade.getEntryType().toLowerCase().contains(lowerCaseFilter)) {
                    return applyStatusFilter(trade, newValue);
                }
                return false;
            });
        });

        searchBox.getChildren().addAll(new Label("Search: "), searchField,
                new Label("Filter: "), filterCombo);

        VBox tableContainer = new VBox();
        tableContainer.getChildren().addAll(searchBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        return table;
    }

    private boolean applyStatusFilter(Trade trade, String filter) {
        if ("All".equals(filter)) {
            return true;
        }
        return trade.getStatus().equals(filter);
    }

    private void showAddTradeDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Add New Trade");
Label returnsLabel = new Label("Returns ($):");
returnsLabel.setStyle("-fx-text-fill: white;");
TextField returnsField = new TextField("0.0");
returnsField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");
Label balanceLabel = new Label("Balance ($):");
balanceLabel.setStyle("-fx-text-fill: white;");
TextField balanceField = new TextField();
balanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #2d2d2d;");

        // Date
        Label dateLabel = new Label("Date:");
        dateLabel.setStyle("-fx-text-fill: white;");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Duration
        Label durationLabel = new Label("Duration:");
        durationLabel.setStyle("-fx-text-fill: white;");
        TextField durationField = new TextField();
        durationField.setPromptText("e.g. 2h 15m");
        durationField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Symbol
        Label symbolLabel = new Label("Symbol:");
        symbolLabel.setStyle("-fx-text-fill: white;");
        ComboBox<String> symbolCombo = new ComboBox<>();
        symbolCombo.getItems().addAll(dataManager.getSettings().getSymbols());
        symbolCombo.setEditable(true);
        symbolCombo.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Entry Price
        Label entryLabel = new Label("Entry Price:");
        entryLabel.setStyle("-fx-text-fill: white;");
        TextField entryField = new TextField();
        entryField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Exit Price
        Label exitLabel = new Label("Exit Price:");
        exitLabel.setStyle("-fx-text-fill: white;");
        TextField exitField = new TextField();
        exitField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Size
        Label sizeLabel = new Label("Size:");
        sizeLabel.setStyle("-fx-text-fill: white;");
        TextField sizeField = new TextField();
        sizeField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Side
        Label sideLabel = new Label("Side:");
        sideLabel.setStyle("-fx-text-fill: white;");
        ComboBox<String> sideCombo = new ComboBox<>();
        sideCombo.getItems().addAll("LONG", "SHORT");
        sideCombo.setValue("LONG");
        sideCombo.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Setup
        Label setupLabel = new Label("Setup:");
        setupLabel.setStyle("-fx-text-fill: white;");
        ComboBox<String> setupCombo = new ComboBox<>();
        setupCombo.getItems().addAll(dataManager.getSettings().getSetupTypes());
        setupCombo.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Entry Type
        Label entryTypeLabel = new Label("Entry Type:");
        entryTypeLabel.setStyle("-fx-text-fill: white;");
        ComboBox<String> entryTypeCombo = new ComboBox<>();
        entryTypeCombo.getItems().addAll(dataManager.getSettings().getEntryTypes());
        entryTypeCombo.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Notes
        Label notesLabel = new Label("Notes:");
        notesLabel.setStyle("-fx-text-fill: white;");
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(3);
        notesArea.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        
        // Side ComboBox fix
sideCombo.setButtonCell(new ListCell<String>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            setText(item);
            setStyle("-fx-text-fill: white;");
        }
    }
});

// Setup ComboBox fix
setupCombo.setButtonCell(new ListCell<String>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            setText(item);
            setStyle("-fx-text-fill: white;");
        }
    }
});

// Entry Type ComboBox fix
entryTypeCombo.setButtonCell(new ListCell<String>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            setText(item);
            setStyle("-fx-text-fill: white;");
        }
    }
});

        // Add to grid
        grid.add(returnsLabel, 0, 11); // Adjust row index as needed
grid.add(returnsField, 1, 11);

        grid.add(dateLabel, 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(durationLabel, 0, 1);
        grid.add(durationField, 1, 1);
        grid.add(symbolLabel, 0, 2);
        grid.add(symbolCombo, 1, 2);
        grid.add(entryLabel, 0, 3);
        grid.add(entryField, 1, 3);
        grid.add(exitLabel, 0, 4);
        grid.add(exitField, 1, 4);
        grid.add(sizeLabel, 0, 5);
        grid.add(sizeField, 1, 5);
        grid.add(sideLabel, 0, 6);
        grid.add(sideCombo, 1, 6);
        grid.add(setupLabel, 0, 7);
        grid.add(setupCombo, 1, 7);
        grid.add(entryTypeLabel, 0, 8);
        grid.add(entryTypeCombo, 1, 8);
        grid.add(notesLabel, 0, 9);
        grid.add(notesArea, 1, 9);
grid.add(balanceLabel, 0, 12); // Adjust row index as needed
grid.add(balanceField, 1, 12);
        // Buttons
        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, saveButton);

        grid.add(buttonBox, 1, 13);

        // Handle events
       
    saveButton.setOnAction(e -> {
        try {
            double entryPrice = Double.parseDouble(entryField.getText());
            double exitPrice = Double.parseDouble(exitField.getText());
            double size = Double.parseDouble(sizeField.getText()); // Changed to double
            double returns = Double.parseDouble(returnsField.getText());
            double balance = 0.0; // Default value
            
            // Try to parse balance, but don't fail if it's empty or invalid
            try {
                if (!balanceField.getText().isEmpty()) {
                    balance = Double.parseDouble(balanceField.getText());
                }
            } catch (NumberFormatException ex) {
                // Use default if parsing fails
            }
            
            Trade newTrade = new Trade(
                null, // Status will be determined by returns
                datePicker.getValue(),
                durationField.getText(),
                symbolCombo.getValue(),
                entryPrice,
                exitPrice,
                size,
                sideCombo.getValue(),
                setupCombo.getValue(),
                entryTypeCombo.getValue(),
                returns,
                balance // Add balance
            );
            
            newTrade.setNotes(notesArea.getText());
            
            // Add new symbol if not in the list
            if (!dataManager.getSettings().getSymbols().contains(symbolCombo.getValue())) {
                dataManager.getSettings().addSymbol(symbolCombo.getValue());
                dataManager.updateSettings();
            }
            
            dataManager.addTrade(newTrade);
            dialog.close();
        } catch (NumberFormatException ex) {
            // Use showAlertHere instead of showAlert
            showAlertHere("Error", "Please enter valid numbers for Entry, Exit, Size, and Returns fields.");
        }
    });


        cancelButton.setOnAction(e -> dialog.close());

        Scene scene = new Scene(grid, 700, 800);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showTradeDetails(Trade trade) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Trade Details");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #2d2d2d;");

        // Overview Tab
        Tab overviewTab = new Tab("Overview");
        GridPane overviewGrid = new GridPane();
        overviewGrid.setHgap(10);
        overviewGrid.setVgap(10);
        overviewGrid.setPadding(new Insets(15));
        overviewGrid.setStyle("-fx-background-color: #2d2d2d;");

        // Trade info in overview
        addLabelValue(overviewGrid, 0, "Status:", trade.getStatus(), trade.getStatus().equals("WIN") ? "#4caf50" : "#f44336");
        addLabelValue(overviewGrid, 1, "Date:", trade.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), null);
        addLabelValue(overviewGrid, 2, "Symbol:", trade.getSymbol(), null);
        addLabelValue(overviewGrid, 3, "Side:", trade.getSide(), null);
        addLabelValue(overviewGrid, 4, "Entry:", String.format("$%.6f", trade.getEntry()), null);
        addLabelValue(overviewGrid, 5, "Exit:", String.format("$%.6f", trade.getExit()), null);
        addLabelValue(overviewGrid, 6, "Size:", String.valueOf(trade.getSize()), null);
        addLabelValue(overviewGrid, 7, "Account Balance:", String.valueOf(trade.getBalance()),null);
        addLabelValue(overviewGrid, 8, "Setup:", trade.getSetup(), null);
        addLabelValue(overviewGrid, 9, "Entry Type:", trade.getEntryType(), null);
        addLabelValue(overviewGrid, 10, "Duration:", trade.getDuration(), null);

        ScrollPane overviewScrollPane = new ScrollPane(overviewGrid);
        overviewScrollPane.setFitToWidth(true);
        overviewScrollPane.setStyle("-fx-background: #2d2d2d;");
        overviewTab.setContent(overviewScrollPane);

        // Notes Tab
        Tab notesTab = new Tab("Notes");
        TextArea notesArea = new TextArea(trade.getNotes());
        notesArea.setEditable(true);
        notesArea.setWrapText(true);
        notesArea.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        Button saveNotesBtn = new Button("Save Notes");
        saveNotesBtn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        saveNotesBtn.setOnAction(e -> {
            trade.setNotes(notesArea.getText());
            dataManager.updateTrade(trade);
            showAlertHere("Success", "Notes saved successfully.");
        });

        VBox notesBox = new VBox(10);
        notesBox.setPadding(new Insets(10));
        notesBox.getChildren().addAll(notesArea, saveNotesBtn);
        VBox.setVgrow(notesArea, Priority.ALWAYS);
        notesTab.setContent(notesBox);

        // Images Tab
        Tab imagesTab = new Tab("Images");
        VBox imagesBox = new VBox(10);
        imagesBox.setPadding(new Insets(10));
        imagesBox.setStyle("-fx-background-color: #2d2d2d;");

        ScrollPane imageScrollPane = new ScrollPane();
        imageScrollPane.setFitToWidth(true);
        imageScrollPane.setStyle("-fx-background: #2d2d2d;");

        VBox imageContainer = new VBox(20);
        imageContainer.setPadding(new Insets(10));

        // Display existing images
        if (trade.getImages() != null && !trade.getImages().isEmpty()) {
            for (String imagePath : trade.getImages()) {
                try {
                    Image image = new Image(new File(imagePath).toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(500);
                    imageView.setPreserveRatio(true);

                    VBox imageBox = new VBox(5);
                    Button deleteBtn = new Button("Delete Image");
                    deleteBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
                    deleteBtn.setOnAction(e -> {
                        trade.getImages().remove(imagePath);
                        dataManager.updateTrade(trade);
                        imageContainer.getChildren().remove(imageBox);
                    });

                    imageBox.getChildren().addAll(imageView, deleteBtn);
                    imageContainer.getChildren().add(imageBox);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        imageScrollPane.setContent(imageContainer);

        Button addImageBtn = new Button("Add Image");
        addImageBtn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        addImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog);
            if (selectedFile != null) {
                String savedPath = dataManager.saveImage(selectedFile);
                if (savedPath != null) {
                    trade.addImage(savedPath);
                    dataManager.updateTrade(trade);

                    // Add new image to display
                    Image image = new Image(new File(savedPath).toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(500);
                    imageView.setPreserveRatio(true);

                    VBox imageBox = new VBox(5);
                    Button deleteBtn = new Button("Delete Image");
                    deleteBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
                    deleteBtn.setOnAction(ev -> {
                        trade.getImages().remove(savedPath);
                        dataManager.updateTrade(trade);
                        imageContainer.getChildren().remove(imageBox);
                    });

                    imageBox.getChildren().addAll(imageView, deleteBtn);
                    imageContainer.getChildren().add(imageBox);
                }
            }
        });

        imagesBox.getChildren().addAll(addImageBtn, imageScrollPane);
        VBox.setVgrow(imageScrollPane, Priority.ALWAYS);
        imagesTab.setContent(imagesBox);

        // Add tabs to pane
        tabPane.getTabs().addAll(overviewTab, notesTab, imagesTab);

        // Add close button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
        closeButton.setOnAction(e -> dialog.close());

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(tabPane);

        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);

        mainPane.setBottom(buttonBox);
        mainPane.setStyle("-fx-background-color: #2d2d2d;");

        Scene scene = new Scene(mainPane, 600, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void addLabelValue(GridPane grid, int row, String labelText, String value, String valueColor) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: white;");
        Label valueLabel = new Label(value);
        if (valueColor != null) {
            valueLabel.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-weight: bold;");
        } else {
            valueLabel.setStyle("-fx-text-fill: #cccccc;");
        }
        grid.add(label, 0, row);
        grid.add(valueLabel, 1, row);
    }

    private void editTrade(Trade trade) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Edit Trade");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2d2d2d;");

        // Date
        Label dateLabel = new Label("Date:");
        dateLabel.setStyle("-fx-text-fill: white;");
        DatePicker datePicker = new DatePicker(trade.getDate());
        datePicker.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Duration
        Label durationLabel = new Label("Duration:");
        durationLabel.setStyle("-fx-text-fill: white;");
        TextField durationField = new TextField(trade.getDuration());
        durationField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Symbol
        Label symbolLabel = new Label("Symbol:");
        symbolLabel.setStyle("-fx-text-fill: white;");
        ComboBox<String> symbolCombo = new ComboBox<>();
        symbolCombo.getItems().addAll(dataManager.getSettings().getSymbols());
        symbolCombo.setValue(trade.getSymbol());
        symbolCombo.setEditable(true);
        symbolCombo.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Entry Price
        Label entryLabel = new Label("Entry Price:");
        entryLabel.setStyle("-fx-text-fill: white;");
        TextField entryField = new TextField(String.valueOf(trade.getEntry()));
        entryField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Exit Price
        Label exitLabel = new Label("Exit Price:");
        exitLabel.setStyle("-fx-text-fill: white;");
        TextField exitField = new TextField(String.valueOf(trade.getExit()));
        exitField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Size
        Label sizeLabel = new Label("Size:");
        sizeLabel.setStyle("-fx-text-fill: white;");
        TextField sizeField = new TextField(String.valueOf(trade.getSize()));
        sizeField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Side
        Label sideLabel = new Label("Side:");
        sideLabel.setStyle("-fx-text-fill: white;");
        ComboBox<String> sideCombo = new ComboBox<>();
        sideCombo.getItems().addAll("LONG", "SHORT");
        sideCombo.setValue(trade.getSide());
        sideCombo.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Setup
        Label setupLabel = new Label("Setup:");
        setupLabel.setStyle("-fx-text-fill: white;");
        ComboBox<String> setupCombo = new ComboBox<>();
        setupCombo.getItems().addAll(dataManager.getSettings().getSetupTypes());
        setupCombo.setValue(trade.getSetup());
        setupCombo.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Entry Type
        Label entryTypeLabel = new Label("Entry Type:");
        entryTypeLabel.setStyle("-fx-text-fill: white;");
        ComboBox<String> entryTypeCombo = new ComboBox<>();
        entryTypeCombo.getItems().addAll(dataManager.getSettings().getEntryTypes());
        entryTypeCombo.setValue(trade.getEntryType());
        entryTypeCombo.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        Label returnsLabel = new Label("Returns ($):");
returnsLabel.setStyle("-fx-text-fill: white;");
TextField returnsField = new TextField(String.valueOf(trade.getReturns()));
returnsField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");
Label balanceLabel = new Label("Balance ($):");
balanceLabel.setStyle("-fx-text-fill: white;");
TextField balanceField = new TextField(String.valueOf(trade.getBalance()));
balanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

// Add to grid
grid.add(balanceLabel, 0, 10); // Adjust row index as needed
grid.add(balanceField, 1, 10);
        
        // Side ComboBox fix
sideCombo.setButtonCell(new ListCell<String>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            setText(item);
            setStyle("-fx-text-fill: white;");
        }
    }
});

// Setup ComboBox fix
setupCombo.setButtonCell(new ListCell<String>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            setText(item);
            setStyle("-fx-text-fill: white;");
        }
    }
});

// Entry Type ComboBox fix
entryTypeCombo.setButtonCell(new ListCell<String>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            setText(item);
            setStyle("-fx-text-fill: white;");
        }
    }
});

        // Add to grid
        grid.add(returnsLabel, 0, 9); // Adjust row index as needed
grid.add(returnsField, 1, 9);
        grid.add(dateLabel, 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(durationLabel, 0, 1);
        grid.add(durationField, 1, 1);
        grid.add(symbolLabel, 0, 2);
        grid.add(symbolCombo, 1, 2);
        grid.add(entryLabel, 0, 3);
        grid.add(entryField, 1, 3);
        grid.add(exitLabel, 0, 4);
        grid.add(exitField, 1, 4);
        grid.add(sizeLabel, 0, 5);
        grid.add(sizeField, 1, 5);
        grid.add(sideLabel, 0, 6);
        grid.add(sideCombo, 1, 6);
        grid.add(setupLabel, 0, 7);
        grid.add(setupCombo, 1, 7);
        grid.add(entryTypeLabel, 0, 8);
        grid.add(entryTypeCombo, 1, 8);

        // Buttons
        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, saveButton);

        grid.add(buttonBox, 1, 11);

        // Handle events
       saveButton.setOnAction(e -> {
        try {
            double entryPrice = Double.parseDouble(entryField.getText());
            double exitPrice = Double.parseDouble(exitField.getText());
            double size = Double.parseDouble(sizeField.getText()); // Changed to double
            double returns = Double.parseDouble(returnsField.getText());
            double balance = 0.0; // Default value
            
            // Try to parse balance, but don't fail if it's empty or invalid
            try {
                if (!balanceField.getText().isEmpty()) {
                    balance = Double.parseDouble(balanceField.getText());
                }
            } catch (NumberFormatException ex) {
                // Use default if parsing fails
            }
            
            trade.setDate(datePicker.getValue());
            trade.setDuration(durationField.getText());
            trade.setSymbol(symbolCombo.getValue());
            trade.setEntry(entryPrice);
            trade.setExit(exitPrice);
            trade.setSize(size);
            trade.setSide(sideCombo.getValue());
            trade.setSetup(setupCombo.getValue());
            trade.setEntryType(entryTypeCombo.getValue());
            trade.setReturns(returns);
            trade.setBalance(balance);
            
            // Add new symbol if not in the list
            if (!dataManager.getSettings().getSymbols().contains(symbolCombo.getValue())) {
                dataManager.getSettings().addSymbol(symbolCombo.getValue());
                dataManager.updateSettings();
            }
            
            dataManager.updateTrade(trade);
            tradeTable.refresh(); // Refresh the table to show updated values
            dialog.close();
        } catch (NumberFormatException ex) {
            // Use showAlertHere instead of showAlert
            showAlertHere("Error", "Please enter valid numbers for Entry, Exit, Size, and Returns fields.");
        }
    });

        cancelButton.setOnAction(e -> dialog.close());

        Scene scene = new Scene(grid, 500, 450);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    

    private void deleteTrade(Trade trade) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Trade");
        alert.setContentText("Are you sure you want to delete this trade?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dataManager.deleteTrade(trade);
        }
    }

    private void showSettingsDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Account Settings");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2d2d2d;");

        // Account Balance
        Label balanceLabel = new Label("Account Balance:");
        balanceLabel.setStyle("-fx-text-fill: white;");
        TextField balanceField = new TextField(String.valueOf(dataManager.getSettings().getAccountBalance()));
        balanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Average Risk
        Label riskLabel = new Label("Average Risk (%):");
        riskLabel.setStyle("-fx-text-fill: white;");
        TextField riskField = new TextField(String.valueOf(dataManager.getSettings().getAverageRisk()));
        riskField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        // Trading Rules
        Label rulesLabel = new Label("Trading Rules:");
        rulesLabel.setStyle("-fx-text-fill: white;");
        TextArea rulesArea = new TextArea(dataManager.getSettings().getTradingRules());
        rulesArea.setPrefRowCount(5);
        rulesArea.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        // Add to grid
        grid.add(balanceLabel, 0, 0);
        grid.add(balanceField, 1, 0);
        grid.add(riskLabel, 0, 1);
        grid.add(riskField, 1, 1);
        grid.add(rulesLabel, 0, 2);
        grid.add(rulesArea, 1, 2);

        // Buttons
        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, saveButton);

        grid.add(buttonBox, 1, 3);

        // Handle events
        saveButton.setOnAction(e -> {
            try {
                double balance = Double.parseDouble(balanceField.getText());
                double risk = Double.parseDouble(riskField.getText());

                dataManager.getSettings().setAccountBalance(balance);
                dataManager.getSettings().setAverageRisk(risk);
                dataManager.getSettings().setTradingRules(rulesArea.getText());

                dataManager.updateSettings();
                dialog.close();

                // Refresh sidebar to show updated values
                root.setLeft(createSidebar());
            } catch (NumberFormatException ex) {
                showAlertHere("Error", "Please enter valid numbers for Balance and Risk fields.");
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        Scene scene = new Scene(grid, 500, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
private <T extends SetupEntryDetails> VBox createEnhancedListManagerBox(
        String title, 
        List<T> items, 
        boolean isSetup,
        java.util.function.Consumer<List<T>> saveAction) {
    
    VBox mainBox = new VBox(10);
    mainBox.setPadding(new Insets(20));
    mainBox.setStyle("-fx-background-color: #2d2d2d;");
    
    Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
    
    // Create a custom list view
    VBox listBox = new VBox(5);
    listBox.setStyle("-fx-background-color: #3a3a3a; -fx-padding: 10;");
    
    ScrollPane scrollPane = new ScrollPane(listBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background: #3a3a3a;");
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    
    // Populate list
    refreshItemsList(listBox, items, isSetup);
    
    // Add input section
    HBox inputBox = new HBox(10);
    TextField newItemField = new TextField();
    newItemField.setPromptText("New item name");
    newItemField.setStyle("-fx-background-color: #444444; -fx-text-fill: white;");
    Button addButton = new Button("ADD");
    addButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
    
    inputBox.getChildren().addAll(newItemField, addButton);
    
    addButton.setOnAction(e -> {
        String newItem = newItemField.getText().trim();
        if (!newItem.isEmpty() && items.stream().noneMatch(item -> item.getName().equals(newItem))) {
            T newDetail = (T) new SetupEntryDetails(newItem);
            items.add(newDetail);
            newItemField.clear();
            
            // Refresh the list
            refreshItemsList(listBox, items, isSetup);
            
            // Save the changes
            saveAction.accept(items);
        }
    });
    
    Button closeButton = new Button("Close");
    closeButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
    
    VBox.setMargin(closeButton, new Insets(10, 0, 0, 0));
    HBox buttonBox = new HBox();
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.getChildren().add(closeButton);
    
    mainBox.getChildren().addAll(titleLabel, scrollPane, inputBox, buttonBox);
    
    return mainBox;
}

private <T extends SetupEntryDetails> void refreshItemsList(VBox listBox, List<T> items, boolean isSetup) {
    listBox.getChildren().clear();
    
    for (T item : items) {
        HBox itemRow = new HBox(10);
        itemRow.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-text-fill: white;");
        nameLabel.setPrefWidth(200);
        
        Button notesButton = new Button("Notes");
        notesButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        notesButton.setOnAction(e -> showItemNotesDialog(item, isSetup ? "Setup" : "Entry Type"));
        
       Button removeButton = new Button("Remove");
removeButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
removeButton.setOnAction(e -> {
    // Create confirmation dialog
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Confirm Delete");
    confirmAlert.setHeaderText("Delete " + (isSetup ? "Setup" : "Entry Type"));
    confirmAlert.setContentText("Are you sure you want to remove \"" + item.getName() + "\"?");
    
    // Style the alert dialog to match your dark theme
    DialogPane alertPane = confirmAlert.getDialogPane();
    alertPane.setStyle("-fx-background-color: #2d2d2d;");
    alertPane.lookupAll(".label").forEach(node -> {
        ((Label)node).setStyle("-fx-text-fill: white;");
    });
    
    // Get result and perform deletion if confirmed
    Optional<ButtonType> result = confirmAlert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
        items.remove(item);
        refreshItemsList(listBox, items, isSetup);
        
        // Save the changes by calling updateSettings
        dataManager.updateSettings();
    }
});
        
        itemRow.getChildren().addAll(nameLabel, notesButton, removeButton);
        listBox.getChildren().add(itemRow);
    }
}

private void showItemNotesDialog(SetupEntryDetails item, String itemType) {
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initOwner(primaryStage);
    dialog.setTitle(itemType + " Details: " + item.getName());
    
    TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.setStyle("-fx-background-color: #2d2d2d;");
    
    // Notes Tab
    Tab notesTab = new Tab("Notes");
    TextArea notesArea = new TextArea(item.getNotes());
    notesArea.setWrapText(true);
    notesArea.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");
    
    Button saveNotesBtn = new Button("Save Notes");
    saveNotesBtn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
    saveNotesBtn.setOnAction(e -> {
        item.setNotes(notesArea.getText());
        dataManager.updateSettings();
        showAlertHere("Success", "Notes saved successfully.");
    });
    
    VBox notesBox = new VBox(10);
    notesBox.setPadding(new Insets(10));
    notesBox.getChildren().addAll(notesArea, saveNotesBtn);
    VBox.setVgrow(notesArea, Priority.ALWAYS);
    notesTab.setContent(notesBox);
    
    // Images Tab
    Tab imagesTab = new Tab("Images");
    VBox imagesBox = new VBox(10);
    imagesBox.setPadding(new Insets(10));
    
    ScrollPane imageScrollPane = new ScrollPane();
    imageScrollPane.setFitToWidth(true);
    imageScrollPane.setStyle("-fx-background: #2d2d2d;");
    
    VBox imageContainer = new VBox(20);
    imageContainer.setPadding(new Insets(10));
    
    // Display existing images
    if (item.getImages() != null && !item.getImages().isEmpty()) {
        for (String imagePath : item.getImages()) {
            try {
                Image image = new Image(new File(imagePath).toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(400);
                imageView.setPreserveRatio(true);
                
                VBox imageBox = new VBox(5);
                Button deleteBtn = new Button("Delete Image");
                deleteBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> {
                    item.getImages().remove(imagePath);
                    dataManager.updateSettings();
                    imageContainer.getChildren().remove(imageBox);
                });
                
                imageBox.getChildren().addAll(imageView, deleteBtn);
                imageContainer.getChildren().add(imageBox);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    imageScrollPane.setContent(imageContainer);
    
    Button addImageBtn = new Button("Add Image");
    addImageBtn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
    addImageBtn.setOnAction(e -> {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(dialog);
        if (selectedFile != null) {
            String savedPath = dataManager.saveImage(selectedFile);
            if (savedPath != null) {
                item.addImage(savedPath);
                dataManager.updateSettings();
                
                // Add new image to display
                Image image = new Image(new File(savedPath).toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(400);
                imageView.setPreserveRatio(true);
                
                VBox imageBox = new VBox(5);
                Button deleteBtn = new Button("Delete Image");
                deleteBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
                deleteBtn.setOnAction(ev -> {
                    item.getImages().remove(savedPath);
                    dataManager.updateSettings();
                    imageContainer.getChildren().remove(imageBox);
                });
                
                imageBox.getChildren().addAll(imageView, deleteBtn);
                imageContainer.getChildren().add(imageBox);
            }
        }
    });
    
    imagesBox.getChildren().addAll(addImageBtn, imageScrollPane);
    VBox.setVgrow(imageScrollPane, Priority.ALWAYS);
    imagesTab.setContent(imagesBox);
    
    // Add tabs to pane
    tabPane.getTabs().addAll(notesTab, imagesTab);
    
    // Close button
    Button closeButton = new Button("Close");
    closeButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
    closeButton.setOnAction(e -> dialog.close());
    
    BorderPane mainPane = new BorderPane();
    mainPane.setCenter(tabPane);
    
    HBox buttonBox = new HBox();
    buttonBox.setPadding(new Insets(10));
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.getChildren().add(closeButton);
    
    mainPane.setBottom(buttonBox);
    mainPane.setStyle("-fx-background-color: #2d2d2d;");
    
    Scene scene = new Scene(mainPane, 600, 500);
    dialog.setScene(scene);
    dialog.showAndWait();
}
    private void manageSymbols() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Manage Symbols");

        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: #2d2d2d;");

        Label titleLabel = new Label("Manage Symbol List");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<String> symbolList = new ListView<>();
        symbolList.getItems().addAll(dataManager.getSettings().getSymbols());
        symbolList.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        HBox inputBox = new HBox(10);
        TextField newSymbolField = new TextField();
        newSymbolField.setPromptText("New symbol");
        newSymbolField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");
        Button addButton = new Button("Add");
        addButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        Button removeButton = new Button("Remove Selected");
        removeButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");

        inputBox.getChildren().addAll(newSymbolField, addButton, removeButton);

        addButton.setOnAction(e -> {
            String newSymbol = newSymbolField.getText().trim().toUpperCase();
            if (!newSymbol.isEmpty() && !symbolList.getItems().contains(newSymbol)) {
                symbolList.getItems().add(newSymbol);
                newSymbolField.clear();
            }
        });

        removeButton.setOnAction(e -> {
            int selectedIndex = symbolList.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                symbolList.getItems().remove(selectedIndex);
            }
        });

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, saveButton);

        mainBox.getChildren().addAll(titleLabel, symbolList, inputBox, buttonBox);
        VBox.setVgrow(symbolList, Priority.ALWAYS);

        saveButton.setOnAction(e -> {
            dataManager.getSettings().setSymbols(new ArrayList<>(symbolList.getItems()));
            dataManager.updateSettings();
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        Scene scene = new Scene(mainBox, 400, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void manageSetups() {
         Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initOwner(primaryStage);
    dialog.setTitle("Manage Setups & Entry Types");
    
    TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.setStyle("-fx-background-color: #2d2d2d;");
    
    // Setups Tab
    Tab setupsTab = new Tab("Setups");
    setupsTab.setStyle("-fx-background-color: #ababab;");
    VBox setupsBox = createEnhancedListManagerBox(
            "Manage Setups",
            dataManager.getSettings().getSetupDetails(),
            true, // isSetup flag to know which type we're handling
            list -> {
                dataManager.getSettings().setSetupDetails(new ArrayList<>(list));
                dataManager.updateSettings();
            }
    );
    setupsTab.setContent(setupsBox);
    
    // Entry Types Tab
    Tab entryTypesTab = new Tab("Entry Types");
    entryTypesTab.setStyle("-fx-background-color: #ababab;");
    VBox entryTypesBox = createEnhancedListManagerBox(
            "Manage Entry Types",
            dataManager.getSettings().getEntryTypeDetails(),
            false, // isSetup flag
            list -> {
                dataManager.getSettings().setEntryTypeDetails(new ArrayList<>(list));
                dataManager.updateSettings();
            }
    );
    entryTypesTab.setContent(entryTypesBox);
    
    tabPane.getTabs().addAll(setupsTab, entryTypesTab);
    
    Scene scene = new Scene(tabPane, 500, 600);
    dialog.setScene(scene);
    dialog.showAndWait();
}
    
    

    private VBox createListManagerBox(String title, List<String> initialItems, java.util.function.Consumer<List<String>> saveAction) {
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: #2d2d2d;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<String> itemList = new ListView<>();
        itemList.getItems().addAll(initialItems);
        itemList.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        HBox inputBox = new HBox(10);
        TextField newItemField = new TextField();
        newItemField.setPromptText("New item");
        newItemField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");
        Button addButton = new Button("Add");
        addButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
        Button removeButton = new Button("Remove");
        removeButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");

        inputBox.getChildren().addAll(newItemField, addButton, removeButton);

        addButton.setOnAction(e -> {
            String newItem = newItemField.getText().trim();
            if (!newItem.isEmpty() && !itemList.getItems().contains(newItem)) {
                itemList.getItems().add(newItem);
                newItemField.clear();
            }
        });

        removeButton.setOnAction(e -> {
            int selectedIndex = itemList.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                itemList.getItems().remove(selectedIndex);
            }
        });

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");

        saveButton.setOnAction(e -> {
            saveAction.accept(new ArrayList<>(itemList.getItems()));
        });

        mainBox.getChildren().addAll(titleLabel, itemList, inputBox, saveButton);
        VBox.setVgrow(itemList, Priority.ALWAYS);

        return mainBox;
    }

    private void showDashboard() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Trading Performance Dashboard");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #2d2d2d;");

        // Overview Tab
        Tab overviewTab = new Tab("Overview");
        VBox overviewBox = createOverviewTab();
        overviewTab.setContent(overviewBox);

        // Chart Tab
        Tab chartsTab = new Tab("Performance Charts");
        VBox chartsBox = createChartsTab();
        chartsTab.setContent(chartsBox);

        // Add tabs to pane
        tabPane.getTabs().addAll(overviewTab, chartsTab);

        Scene scene = new Scene(tabPane, 900, 700);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private VBox createOverviewTab() {
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: #2d2d2d;");

        // Calculate statistics
        int totalTrades = dataManager.getTrades().size();
        long winningTrades = dataManager.getTrades().stream()
                .filter(t -> t.getStatus().equals("WIN"))
                .count();
        long losingTrades = totalTrades - winningTrades;
        double winRate = totalTrades > 0 ? (double) winningTrades / totalTrades * 100 : 0;

        double totalProfit = dataManager.getTrades().stream()
                .mapToDouble(Trade::getReturns)
                .sum();

        double avgWin = dataManager.getTrades().stream()
                .filter(t -> t.getStatus().equals("WIN"))
                .mapToDouble(Trade::getReturns)
                .average()
                .orElse(0);

        double avgLoss = dataManager.getTrades().stream()
                .filter(t -> t.getStatus().equals("LOSS"))
                .mapToDouble(Trade::getReturns)
                .average()
                .orElse(0);

        // Create statistics grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(10);
        statsGrid.setPadding(new Insets(10));
        statsGrid.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        addStatsLabel(statsGrid, 0, 0, "Total Trades", String.valueOf(totalTrades));
        addStatsLabel(statsGrid, 0, 1, "Winning Trades", String.valueOf(winningTrades));
        addStatsLabel(statsGrid, 0, 2, "Losing Trades", String.valueOf(losingTrades));
        addStatsLabel(statsGrid, 1, 0, "Win Rate", String.format("%.2f%%", winRate));
        addStatsLabel(statsGrid, 1, 1, "Total P&L", String.format("$%.2f", totalProfit));
        addStatsLabel(statsGrid, 1, 2, "Avg Win", String.format("$%.2f", avgWin));
        addStatsLabel(statsGrid, 2, 0, "Avg Loss", String.format("$%.2f", avgLoss));
        addStatsLabel(statsGrid, 2, 1, "Risk-Reward Ratio",
                avgWin != 0 && avgLoss != 0 ? String.format("%.2f", Math.abs(avgWin / avgLoss)) : "N/A");

        // Create performance by setup chart
        PieChart setupPieChart = createSetupPieChart();

        // Best/worst trades
        VBox bestWorstBox = new VBox(10);
        bestWorstBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 5; -fx-padding: 10;");

        Label bestTradesLabel = new Label("Best Trades");
        bestTradesLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        TableView<Trade> bestTradesTable = createTopTradesTable(true);
        bestTradesTable.setPrefHeight(150);

        Label worstTradesLabel = new Label("Worst Trades");
        worstTradesLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        TableView<Trade> worstTradesTable = createTopTradesTable(false);
        worstTradesTable.setPrefHeight(150);

        bestWorstBox.getChildren().addAll(bestTradesLabel, bestTradesTable, worstTradesLabel, worstTradesTable);

        mainBox.getChildren().addAll(statsGrid, setupPieChart, bestWorstBox);

        return mainBox;
    }

    private void addStatsLabel(GridPane grid, int col, int row, String title, String value) {
        VBox box = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 12px;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        box.getChildren().addAll(titleLabel, valueLabel);
        grid.add(box, col, row);
    }

    private PieChart createSetupPieChart() {
        // Count trades by setup
        Map<String, Long> setupCounts = dataManager.getTrades().stream()
                .collect(Collectors.groupingBy(Trade::getSetup, Collectors.counting()));

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        setupCounts.forEach((setup, count) -> {
            if (setup != null && !setup.isEmpty()) {
                pieChartData.add(new PieChart.Data(setup + " (" + count + ")", count));
            }
        });

        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Trades by Setup");
        chart.setLabelLineLength(10);
        chart.setLegendSide(Side.RIGHT);
        chart.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        // Customize chart
        chart.setLabelsVisible(true);
        chart.setAnimated(false);

        return chart;
    }

    private TableView<Trade> createTopTradesTable(boolean best) {
        // Sort trades by returns (ascending or descending based on best flag)
        List<Trade> sortedTrades = new ArrayList<>(dataManager.getTrades());
        sortedTrades.sort((t1, t2) -> best
                ? Double.compare(t2.getReturns(), t1.getReturns())
                : Double.compare(t1.getReturns(), t2.getReturns()));

        // Take top 5 or less
        List<Trade> topTrades = sortedTrades.stream()
                .limit(5)
                .collect(Collectors.toList());

        // Create table
        TableView<Trade> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(topTrades));
        table.setStyle("-fx-background-color: #2d2d2d; -fx-control-inner-background: #3a3a3a;");

        // Create columns
        TableColumn<Trade, String> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(new PropertyValueFactory<>("symbol"));

        TableColumn<Trade, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(column -> new TableCell<Trade, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        TableColumn<Trade, String> setupCol = new TableColumn<>("Setup");
        setupCol.setCellValueFactory(new PropertyValueFactory<>("setup"));

        TableColumn<Trade, Double> returnsCol = new TableColumn<>("Returns");
        returnsCol.setCellValueFactory(new PropertyValueFactory<>("returns"));
        returnsCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%.2f", item));
                    if (item > 0) {
                        setStyle("-fx-text-fill: #4caf50;");
                    } else {
                        setStyle("-fx-text-fill: #f44336;");
                    }
                }
            }
        });

        TableColumn<Trade, Double> returnPctCol = new TableColumn<>("Return %");
        returnPctCol.setCellValueFactory(new PropertyValueFactory<>("returnPercentage"));
        returnPctCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f%%", item));
                    if (item > 0) {
                        setStyle("-fx-text-fill: #4caf50;");
                    } else {
                        setStyle("-fx-text-fill: #f44336;");
                    }
                }
            }
        });

        table.getColumns().addAll(symbolCol, dateCol, setupCol, returnsCol, returnPctCol);

        return table;
    }

    private VBox createChartsTab() {
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: #2d2d2d;");

        // Create controls for time period selection
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        Label periodLabel = new Label("Time Period:");
        periodLabel.setStyle("-fx-text-fill: white;");

        ComboBox<String> periodCombo = new ComboBox<>();
        periodCombo.getItems().addAll("All Time", "This Month", "This Week", "Last 30 Days");
        periodCombo.setValue("All Time");
        periodCombo.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        controlsBox.getChildren().addAll(periodLabel, periodCombo);

        // P&L Chart
        LineChart<String, Number> plChart = createProfitLossChart();

        // Win/Loss Ratio Chart
        BarChart<String, Number> winLossChart = createWinLossChart();

        VBox chartContainer = new VBox(20);
        chartContainer.getChildren().addAll(plChart, winLossChart);

        ScrollPane scrollPane = new ScrollPane(chartContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2d2d2d;");

        mainBox.getChildren().addAll(controlsBox, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Update charts based on selected period
        periodCombo.setOnAction(e -> {
            // Implement period filtering logic here
            // This would filter trades based on selected period and update charts
        });

        return mainBox;
    }

    private LineChart<String, Number> createProfitLossChart() {
        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Profit/Loss ($)");

        // Create chart
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Cumulative Profit/Loss");
        lineChart.setStyle("-fx-background-color: #333333;");
        lineChart.setAnimated(false);

        // Prepare data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cumulative P&L");

        // Sort trades by date
        List<Trade> sortedTrades = new ArrayList<>(dataManager.getTrades());
        sortedTrades.sort(Comparator.comparing(Trade::getDate));

        // Calculate cumulative P&L
        double cumulativePL = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Trade trade : sortedTrades) {
            cumulativePL += trade.getReturns();
            series.getData().add(new XYChart.Data<>(formatter.format(trade.getDate()), cumulativePL));
        }

        lineChart.getData().add(series);

        // Apply custom styling
        for (XYChart.Series<String, Number> s : lineChart.getData()) {
            for (XYChart.Data<String, Number> data : s.getData()) {
                Node node = data.getNode();
                double value = data.getYValue().doubleValue();

                // Change line color based on positive or negative value
                if (value >= 0) {
                    node.setStyle("-fx-stroke: #4caf50;");
                } else {
                    node.setStyle("-fx-stroke: #f44336;");
                }
            }
        }

        return lineChart;
    }

    private BarChart<String, Number> createWinLossChart() {
        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Trading Day");
        yAxis.setLabel("Win/Loss Count");

        // Create chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Daily Win/Loss Distribution");
        barChart.setStyle("-fx-background-color: #333333;");
        barChart.setAnimated(false);

        // Group trades by date
        Map<LocalDate, List<Trade>> tradesByDate = dataManager.getTrades().stream()
                .collect(Collectors.groupingBy(Trade::getDate));

        // Prepare data
        XYChart.Series<String, Number> winSeries = new XYChart.Series<>();
        winSeries.setName("Wins");

        XYChart.Series<String, Number> lossSeries = new XYChart.Series<>();
        lossSeries.setName("Losses");

        // Sort dates
        List<LocalDate> dates = new ArrayList<>(tradesByDate.keySet());
        dates.sort(LocalDate::compareTo);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (LocalDate date : dates) {
            List<Trade> dayTrades = tradesByDate.get(date);

            long wins = dayTrades.stream()
                    .filter(t -> t.getStatus().equals("WIN"))
                    .count();

            long losses = dayTrades.stream()
                    .filter(t -> t.getStatus().equals("LOSS"))
                    .count();

            String dateStr = formatter.format(date);
            winSeries.getData().add(new XYChart.Data<>(dateStr, wins));
            lossSeries.getData().add(new XYChart.Data<>(dateStr, losses));
        }

        barChart.getData().addAll(winSeries, lossSeries);

        // Apply custom styling
        for (XYChart.Series<String, Number> series : barChart.getData()) {
            if (series.getName().equals("Wins")) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.getNode().setStyle("-fx-bar-fill: #4caf50;");
                }
            } else {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.getNode().setStyle("-fx-bar-fill: #f44336;");
                }
            }
        }

        return barChart;
    }

    
    private void showCalendarView() {
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initOwner(primaryStage);
    dialog.setTitle("Calendar Performance View");
    
    BorderPane mainPane = new BorderPane();
    mainPane.setStyle("-fx-background-color: #1e1e1e;");
    
    // Month and year selection
    HBox monthYearSelector = new HBox(10);
    monthYearSelector.setPadding(new Insets(15));
    monthYearSelector.setAlignment(Pos.CENTER);
    monthYearSelector.setStyle("-fx-background-color: #252525;");
    
    Button prevMonthBtn = new Button("<");
    prevMonthBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
    
    // Use a DatePicker for month/year selection
    DatePicker datePicker = new DatePicker(LocalDate.now());
    datePicker.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
    // Set to month year view only
    datePicker.setShowWeekNumbers(false);
    
    Button nextMonthBtn = new Button(">");
    nextMonthBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
    
    Label monthYearLabel = new Label(
        YearMonth.from(LocalDate.now()).format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    monthYearLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
    
    monthYearSelector.getChildren().addAll(prevMonthBtn, monthYearLabel, nextMonthBtn);
    
    // Calendar grid
    GridPane calendarGrid = new GridPane();
    calendarGrid.setPadding(new Insets(10));
    calendarGrid.setHgap(1);
    calendarGrid.setVgap(1);
    calendarGrid.setStyle("-fx-background-color: #1e1e1e;");
    
    // Day headers
    String[] days = {"SUN", "MON", "TUE", "WED", "THR", "FRI", "SAT"};
    for (int i = 0; i < days.length; i++) {
        StackPane dayHeader = new StackPane();
        dayHeader.setPrefSize(90, 30);
        dayHeader.setStyle("-fx-background-color: #252525;");
        
        Label dayLabel = new Label(days[i]);
        dayLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        dayHeader.getChildren().add(dayLabel);
        calendarGrid.add(dayHeader, i, 0);
    }
    
    // Function to update calendar
    Runnable updateCalendar = () -> {
        // Clear existing calendar days
        for (Node node : new ArrayList<>(calendarGrid.getChildren())) {
            Integer rowIndex = GridPane.getRowIndex(node);
            if (rowIndex != null && rowIndex > 0) {
                calendarGrid.getChildren().remove(node);
            }
        }
        
        // Get selected month and year
        YearMonth yearMonth = YearMonth.from(datePicker.getValue());
        monthYearLabel.setText(yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        
        // First day of month
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        
        // How many days to leave blank at the start
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // 0 = Sunday, 6 = Saturday
        
        // Group trades by date for display
        Map<LocalDate, List<Trade>> tradesByDate = dataManager.getTrades().stream()
            .filter(t -> YearMonth.from(t.getDate()).equals(yearMonth))
            .collect(Collectors.groupingBy(Trade::getDate));
        
        // Generate calendar days
        int day = 1;
        for (int week = 1; week <= 6; week++) {
            for (int weekday = 0; weekday < 7; weekday++) {
                if ((week == 1 && weekday < dayOfWeek) || day > yearMonth.lengthOfMonth()) {
                    // Empty cell for days outside of month
                    StackPane emptyCell = new StackPane();
                    emptyCell.setPrefSize(90, 90);
                    emptyCell.setStyle("-fx-background-color: #3a3a3a;");
                    calendarGrid.add(emptyCell, weekday, week);
                } else {
                    // Create cell for this day
                    VBox dayCell = new VBox(5);
                    dayCell.setPadding(new Insets(5));
                    dayCell.setPrefSize(90, 90);
                    
                    // Create date
                    LocalDate date = yearMonth.atDay(day);
                    
                    // Check for trades on this day
                    if (tradesByDate.containsKey(date)) {
                        List<Trade> dayTrades = tradesByDate.get(date);
                        double totalReturns = dayTrades.stream().mapToDouble(Trade::getReturns).sum();
                        int tradeCount = dayTrades.size();
                        
                        // Set background color based on returns
                        if (totalReturns > 0) {
                            dayCell.setStyle("-fx-background-color: rgba(76, 175, 80, 0.7);"); // Green for profit
                        } else if (totalReturns < 0) {
                            dayCell.setStyle("-fx-background-color: rgba(244, 67, 54, 0.7);"); // Red for loss
                        } else {
                            dayCell.setStyle("-fx-background-color: #3a3a3a;"); // Gray for break-even
                        }
                        
                        // Date number
                        Label dateLabel = new Label(String.valueOf(day));
                        dateLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        
                        // Returns amount
                        Label returnsLabel = new Label(String.format("$%.2f", totalReturns));
                        returnsLabel.setStyle("-fx-text-fill: white;");
                        
                        // Trade count
                        Label countLabel = new Label(tradeCount + (tradeCount == 1 ? " Trade" : " Trades"));
                        countLabel.setStyle("-fx-text-fill: white;");
                        
                        dayCell.getChildren().addAll(dateLabel, returnsLabel, countLabel);
                    } else {
                        // No trades this day
                        dayCell.setStyle("-fx-background-color: #3a3a3a;"); // Gray for no trades
                        
                        // Just date number
                        Label dateLabel = new Label(String.valueOf(day));
                        dateLabel.setStyle("-fx-text-fill: white;");
                        
                        dayCell.getChildren().add(dateLabel);
                    }
                    
                    // Make day cell clickable to show trades for that day
                    final int finalDay = day;
                    dayCell.setOnMouseClicked(e -> {
                        LocalDate selectedDate = yearMonth.atDay(finalDay);
                        showDayTrades(selectedDate.getYear(), selectedDate.getMonthValue(), selectedDate.getDayOfMonth());
                    });
                    
                    calendarGrid.add(dayCell, weekday, week);
                    day++;
                }
            }
            
            // Break if we've filled all days
            if (day > yearMonth.lengthOfMonth()) {
                break;
            }
        }
    };
    
    // Update buttons behavior
    datePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateCalendar.run());
    
    prevMonthBtn.setOnAction(e -> {
        datePicker.setValue(datePicker.getValue().minusMonths(1));
    });
    
    nextMonthBtn.setOnAction(e -> {
        datePicker.setValue(datePicker.getValue().plusMonths(1));
    });
    
    // Initial calendar display
    updateCalendar.run();
    
    // Add close button at bottom
    Button closeButton = new Button("Close");
    closeButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
    closeButton.setOnAction(e -> dialog.close());
    
    HBox buttonBox = new HBox();
    buttonBox.setPadding(new Insets(10));
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.getChildren().add(closeButton);
    
    // Add everything to main pane
    ScrollPane scrollPane = new ScrollPane(calendarGrid);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle("-fx-background: #1e1e1e; -fx-border-color: transparent;");
    
    mainPane.setTop(monthYearSelector);
    mainPane.setCenter(scrollPane);
    mainPane.setBottom(buttonBox);
    
    Scene scene = new Scene(mainPane, 650, 550);
    dialog.setScene(scene);
    dialog.showAndWait();
}

// Make sure this method exists for showing daily trades
private void showDayTrades(int year, int month, int day) {
    LocalDate date = LocalDate.of(year, month, day);
    
    // Filter trades for selected date
    List<Trade> dayTrades = dataManager.getTrades().stream()
                          .filter(t -> t.getDate().equals(date))
                          .collect(Collectors.toList());
    
    if (dayTrades.isEmpty()) {
        showAlertHere("No Trades", "No trades found for " + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return;
    }
    
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initOwner(primaryStage);
    dialog.setTitle("Trades for " + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    
    VBox mainBox = new VBox(10);
    mainBox.setPadding(new Insets(20));
    mainBox.setStyle("-fx-background-color: #2d2d2d;");
    
    // Summary section
    HBox summaryBox = new HBox(20);
    summaryBox.setPadding(new Insets(10));
    summaryBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");
    
    // Calculate day statistics
    int totalTrades = dayTrades.size();
    long winningTrades = dayTrades.stream()
                        .filter(t -> t.getStatus().equals("WIN"))
                        .count();
    long losingTrades = totalTrades - winningTrades;
    double winRate = (double) winningTrades / totalTrades * 100;
    
    double totalReturns = dayTrades.stream()
                        .mapToDouble(Trade::getReturns)
                        .sum();
    
    VBox statsBox1 = new VBox(5);
    Label tradesLabel = new Label("Total Trades: " + totalTrades);
    tradesLabel.setStyle("-fx-text-fill: white;");
    Label winsLabel = new Label("Winning Trades: " + winningTrades);
    winsLabel.setStyle("-fx-text-fill: white;");
    Label lossesLabel = new Label("Losing Trades: " + losingTrades);
    lossesLabel.setStyle("-fx-text-fill: white;");
    statsBox1.getChildren().addAll(tradesLabel, winsLabel, lossesLabel);
    
    VBox statsBox2 = new VBox(5);
    Label winRateLabel = new Label(String.format("Win Rate: %.2f%%", winRate));
    winRateLabel.setStyle("-fx-text-fill: white;");
    Label returnsLabel = new Label(String.format("Total P&L: $%.2f", totalReturns));
    returnsLabel.setStyle(totalReturns > 0 ? "-fx-text-fill: #4caf50;" : "-fx-text-fill: #f44336;");
    statsBox2.getChildren().addAll(winRateLabel, returnsLabel);
    
    summaryBox.getChildren().addAll(statsBox1, statsBox2);
    
    // Create a table of the day's trades
    TableView<Trade> tradesTable = new TableView<>(FXCollections.observableArrayList(dayTrades));
    tradesTable.setStyle("-fx-background-color: #2d2d2d; -fx-control-inner-background: #3a3a3a;");
    
    // Add your columns here (similar to your main trade table but more compact)
    TableColumn<Trade, String> symbolCol = new TableColumn<>("Symbol");
    symbolCol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
    
    TableColumn<Trade, String> sideCol = new TableColumn<>("Side");
    sideCol.setCellValueFactory(new PropertyValueFactory<>("side"));
    
    TableColumn<Trade, Double> returnsCol = new TableColumn<>("Returns");
    returnsCol.setCellValueFactory(new PropertyValueFactory<>("returns"));
    returnsCol.setCellFactory(column -> new TableCell<Trade, Double>() {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setStyle("");
            } else {
                setText(String.format("$%.2f", item));
                if (item > 0) {
                    setStyle("-fx-text-fill: #4caf50;");
                } else {
                    setStyle("-fx-text-fill: #f44336;");
                }
            }
        }
    });
    
    TableColumn<Trade, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setCellFactory(column -> new TableCell<Trade, String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setStyle("");
            } else {
                setText(item);
                if (item.equals("WIN")) {
                    setStyle("-fx-background-color: #1e5531; -fx-text-fill: white;");
                } else {
                    setStyle("-fx-background-color: #5e1e1e; -fx-text-fill: white;");
                }
            }
        }
    });
    
    tradesTable.getColumns().addAll(symbolCol, sideCol, returnsCol, statusCol);
    
    // Close button
    Button closeButton = new Button("Close");
    closeButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
    closeButton.setOnAction(e -> dialog.close());
    
    HBox buttonBox = new HBox();
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.getChildren().add(closeButton);
    
    mainBox.getChildren().addAll(summaryBox, tradesTable, buttonBox);
    VBox.setVgrow(tradesTable, Priority.ALWAYS);
    
    Scene scene = new Scene(mainBox, 600, 500);
    dialog.setScene(scene);
    dialog.showAndWait();
}
/*
    private void showDayTrades(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);

        // Filter trades for the selected date
        List<Trade> dayTrades = dataManager.getTrades().stream()
                .filter(t -> t.getDate().equals(date))
                .collect(Collectors.toList());

        if (dayTrades.isEmpty()) {
            showAlertHere("No Trades", "No trades found for " + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Trades for " + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: #2d2d2d;");

        // Summary section
        HBox summaryBox = new HBox(20);
        summaryBox.setPadding(new Insets(10));
        summaryBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        // Calculate day statistics
        int totalTrades = dayTrades.size();
        long winningTrades = dayTrades.stream()
                .filter(t -> t.getStatus().equals("WIN"))
                .count();
        long losingTrades = totalTrades - winningTrades;
        double winRate = (double) winningTrades / totalTrades * 100;

        double totalReturns = dayTrades.stream()
                .mapToDouble(Trade::getReturns)
                .sum();

        VBox statsBox1 = new VBox(5);
        Label tradesLabel = new Label("Total Trades: " + totalTrades);
        tradesLabel.setStyle("-fx-text-fill: white;");
        Label winsLabel = new Label("Winning Trades: " + winningTrades);
        winsLabel.setStyle("-fx-text-fill: white;");
        Label lossesLabel = new Label("Losing Trades: " + losingTrades);
        lossesLabel.setStyle("-fx-text-fill: white;");
        statsBox1.getChildren().addAll(tradesLabel, winsLabel, lossesLabel);

        VBox statsBox2 = new VBox(5);
        Label winRateLabel = new Label(String.format("Win Rate: %.2f%%", winRate));
        winRateLabel.setStyle("-fx-text-fill: white;");
        Label returnsLabel = new Label(String.format("Total P&L: $%.2f", totalReturns));
        returnsLabel.setStyle(totalReturns > 0 ? "-fx-text-fill: #4caf50;" : "-fx-text-fill: #f44336;");
        statsBox2.getChildren().addAll(winRateLabel, returnsLabel);

        summaryBox.getChildren().addAll(statsBox1, statsBox2);

        // Trades table
        TableView<Trade> tradesTable = new TableView<>(FXCollections.observableArrayList(dayTrades));
        tradesTable.setStyle("-fx-background-color: #2d2d2d; -fx-control-inner-background: #3a3a3a;");

        // Create columns
        TableColumn<Trade, String> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(new PropertyValueFactory<>("symbol"));

        TableColumn<Trade, String> sideCol = new TableColumn<>("Side");
        sideCol.setCellValueFactory(new PropertyValueFactory<>("side"));

        TableColumn<Trade, Double> entryCol = new TableColumn<>("Entry");
        entryCol.setCellValueFactory(new PropertyValueFactory<>("entry"));
        entryCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        TableColumn<Trade, Double> exitCol = new TableColumn<>("Exit");
        exitCol.setCellValueFactory(new PropertyValueFactory<>("exit"));
        exitCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        TableColumn<Trade, String> setupCol = new TableColumn<>("Setup");
        setupCol.setCellValueFactory(new PropertyValueFactory<>("setup"));

        TableColumn<Trade, Double> returnsCol = new TableColumn<>("Returns");
        returnsCol.setCellValueFactory(new PropertyValueFactory<>("returns"));
        returnsCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%.2f", item));
                    if (item > 0) {
                        setStyle("-fx-text-fill: #4caf50;");
                    } else {
                        setStyle("-fx-text-fill: #f44336;");
                    }
                }
            }
        });

        TableColumn<Trade, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<Trade, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("WIN")) {
                        setStyle("-fx-background-color: #1e5531; -fx-text-fill: white;");
                    } else {
                        setStyle("-fx-background-color: #5e1e1e; -fx-text-fill: white;");
                    }
                }
            }
        });

        TableColumn<Trade, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button detailsBtn = new Button("Details");

            {
                detailsBtn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white;");
                detailsBtn.setOnAction(event -> {
                    Trade trade = getTableView().getItems().get(getIndex());
                    showTradeDetails(trade);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : detailsBtn);
            }
        });

        tradesTable.getColumns().addAll(symbolCol, sideCol, entryCol, exitCol, setupCol, returnsCol, statusCol, actionsCol);

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
        closeButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);

        mainBox.getChildren().addAll(summaryBox, tradesTable, buttonBox);
        VBox.setVgrow(tradesTable, Priority.ALWAYS);

        Scene scene = new Scene(mainBox, 800, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
*/
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Trading Journal");
        alert.setHeaderText("Trading Journal Application");
        alert.setContentText("Version 1.0\n\nA comprehensive trading journal application for traders to track and analyze their trading performance.");

        // Apply custom styling to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2d2d2d;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #333333;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        alert.showAndWait();
    }
    
    public static class SetupEntryDetails implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String notes;
    private List<String> images;
    
    public SetupEntryDetails(String name) {
        this.name = name;
        this.notes = "";
        this.images = new ArrayList<>();
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public List<String> getImages() { return images; }
    public void addImage(String imagePath) { this.images.add(imagePath); }
    
    @Override
    public String toString() {
        return name;
    }
}

    private void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Trade Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write CSV header
                writer.println("DATE,SYMBOL,SIDE,ENTRY,EXIT,SIZE,RETURNS,STATUS,SETUP,ENTRY_TYPE,DURATION,NOTES,BALANCE");

// Update the data writing
for (Trade trade : dataManager.getTrades()) {
    String notes = trade.getNotes() != null ? "\"" + trade.getNotes().replace("\"", "\"\"") + "\"" : "";
    writer.println(String.format("%s,%s,%s,%.5f,%.5f,%.2f,%.2f,%s,%s,%s,%s,%s,%.2f",
        trade.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        trade.getSymbol(),
        trade.getSide(),
        trade.getEntry(),
        trade.getExit(),
        trade.getSize(),
        trade.getReturns(),
        trade.getStatus(),
        trade.getSetup(),
        trade.getEntryType(),
        trade.getDuration(),
        notes,
        trade.getBalance()
    ));
}

                showAlertHere("Export Successful", "Trade data has been exported to: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlertHere("Export Error", "Failed to export data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

private void importData() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Import Trade Data");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
    File file = fileChooser.showOpenDialog(primaryStage);
    
    if (file != null) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header
            String line = reader.readLine();
            
            // Read trade data
            List<Trade> importedTrades = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                
                if (parts.length >= 12) {
                    try {
                        LocalDate date = LocalDate.parse(parts[0], dateFormatter);
                        String symbol = parts[1];
                        String side = parts[2];
                        double entry = Double.parseDouble(parts[3]);
                        double exit = Double.parseDouble(parts[4]);
                        int size = Integer.parseInt(parts[5]);
                        double returns = Double.parseDouble(parts[6]);
                        String status = parts[8];
                        String setup = parts[9];
                        String entryType = parts[10];
                        String duration = parts[11];
                        
                        // Get balance if available (after notes in CSV)
                        double balance = 0.0;
                        if (parts.length > 13) {
                            try {
                                balance = Double.parseDouble(parts[13]);
                            } catch (NumberFormatException e) {
                                // If balance parsing fails, use default 0.0
                            }
                        }
                        
                        // Create the trade
                        Trade trade = new Trade(status, date, duration, symbol, entry, exit, size, 
                                              side, setup, entryType, returns, balance);
                        
                        // Notes (if available)
                        if (parts.length > 12) {
                            String notes = parts[12];
                            if (notes.startsWith("\"") && notes.endsWith("\"")) {
                                notes = notes.substring(1, notes.length() - 1).replace("\"\"", "\"");
                            }
                            trade.setNotes(notes);
                        }
                        
                        importedTrades.add(trade);
                    } catch (DateTimeParseException | NumberFormatException e) {
                        // Skip invalid lines
                        System.err.println("Skipping invalid line: " + line);
                    }
                }
            }
            
            if (!importedTrades.isEmpty()) {
                // Ask user if they want to replace or append
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Import Trades");
                alert.setHeaderText("Import Options");
                alert.setContentText("Do you want to replace existing trades or append imported trades?");
                
                ButtonType replaceButton = new ButtonType("Replace");
                ButtonType appendButton = new ButtonType("Append");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                
                alert.getButtonTypes().setAll(replaceButton, appendButton, cancelButton);
                
                // Apply custom styling to the dialog
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.setStyle("-fx-background-color: #2d2d2d;");
                dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
                dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #333333;");
                dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                
                Optional<ButtonType> result = alert.showAndWait();
                
                if (result.isPresent()) {
                    if (result.get() == replaceButton) {
                        dataManager.getTrades().clear();
                        dataManager.getTrades().addAll(importedTrades);
                        dataManager.saveTrades();
                        this.showAlertHere("Import Successful", "Imported " + importedTrades.size() + " trades, replacing existing trades.");
                    } else if (result.get() == appendButton) {
                        dataManager.getTrades().addAll(importedTrades);
                        dataManager.saveTrades();
                        this.showAlertHere("Import Successful", "Appended " + importedTrades.size() + " imported trades.");
                    }
                }
            } else {
                // Create alert directly if showAlert method isn't accessible
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Import Warning");
                alert.setHeaderText(null);
                alert.setContentText("No valid trades found in the import file.");
                
                // Style the dialog
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.setStyle("-fx-background-color: #2d2d2d;");
                dialogPane.lookupAll(".label").forEach(node -> {
                    ((Label)node).setStyle("-fx-text-fill: white;");
                });
                
                alert.showAndWait();
            }
        } catch (Exception e) {
            // Handle general file reading errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to import data: " + e.getMessage());
            
            // Style the dialog
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #2d2d2d;");
            dialogPane.lookupAll(".label").forEach(node -> {
                ((Label)node).setStyle("-fx-text-fill: white;");
            });
            
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}

   

    @Override
    public void stop() {
        // Save data before closing the application
        if (dataManager != null) {
            dataManager.saveData();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }}

