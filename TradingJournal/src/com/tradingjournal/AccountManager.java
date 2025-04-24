package com.tradingjournal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AccountManager implements Serializable {
    private static final long serialVersionUID = 1L;
   // In both AccountManager and DataManager classes
private static final String DATA_DIR = "E:\\netbeansprojects\\TradingJournal\\TradingJournalData";    private static final String ACCOUNTS_FILE = "accounts.dat";
    
    private List<Account> accounts;
    private Account currentAccount;
    
    public AccountManager() {
        accounts = new ArrayList<>();
        loadAccounts();
        
        if (accounts.isEmpty()) {
            // Create default account if none exists
            Account defaultAccount = new Account("Default Account");
            accounts.add(defaultAccount);
            currentAccount = defaultAccount;
            saveAccounts();
        } else {
            currentAccount = accounts.get(0);
        }
    }
    
    public List<Account> getAccounts() {
        return accounts;
    }
    
    public Account getCurrentAccount() {
        return currentAccount;
    }
    
    public void setCurrentAccount(Account account) {
        this.currentAccount = account;
    }
    
    public void addAccount(String name) {
        Account account = new Account(name);
        accounts.add(account);
        saveAccounts();
    }
    
    // In the AccountManager class, fix the removeAccount method:
public void removeAccount(Account account) {
    // Store current account before changes
    Account previousCurrent = currentAccount;
    
    // Check if we're deleting the current account
    boolean isCurrentAccount = currentAccount.getId().equals(account.getId());
    
    // Remove from list but don't save yet
    accounts.remove(account);
    
    // Handle empty accounts list or current account deleted
    if (accounts.isEmpty()) {
        // Create default account if all are removed
        Account defaultAccount = new Account("Default Account");
        accounts.add(defaultAccount);
        currentAccount = defaultAccount;
    } else if (isCurrentAccount) {
        // Reset current account to first in list if it was deleted
        currentAccount = accounts.get(0);
    } else {
        // Keep the same current account
        currentAccount = previousCurrent;
    }
    
    // Save the updated account list
    saveAccounts();
    
    // Only after account list is updated, delete the account's data directory
    File accountDir = new File(DATA_DIR + File.separator + account.getId());
    if (accountDir.exists()) {
        deleteDirectory(accountDir);
    }
}
    
    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return directory.delete();
    }
    
    @SuppressWarnings("unchecked")
    private void loadAccounts() {
        File accountsFile = new File(DATA_DIR + File.separator + ACCOUNTS_FILE);
        if (accountsFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(accountsFile))) {
                accounts = (List<Account>) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
                accounts = new ArrayList<>();
            }
        } else {
            accounts = new ArrayList<>();
        }
    }
    
    private void saveAccounts() {
        // Create data directory if it doesn't exist
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        
        File accountsFile = new File(DATA_DIR + File.separator + ACCOUNTS_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(accountsFile))) {
            oos.writeObject(accounts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}