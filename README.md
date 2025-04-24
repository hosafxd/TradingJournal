# 📊 Trading Journal Application

A **comprehensive Java desktop app** for traders to **track, analyze**, and **visualize** their trading performance.

---

## ⚙️ Setup & Installation

### 📋 Requirements
- **Java JDK 17** or later  
- **JavaFX 17.0.9** or compatible version  

### 🚀 Setup Instructions

1. **Clone or Download the Repository**
   ```bash
   git clone https://github.com/yourusername/trading-journal.git
   ```

2. **Download JavaFX SDK**
   - Get it from: [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)
   - Extract to:  
     `C:\javafx-sdk-17.0.9` *(or your preferred location)*

---

## 🖥️ Project Setup in NetBeans

1. **Open NetBeans**
2. `File → Open Project → Select your project folder`
3. Right-click the project → `Properties`
4. Navigate to:
   - **Libraries → Add JAR/Folder** → Add all JARs from `lib` folder of JavaFX SDK
5. Under `Run → VM Options`, add:
   ```
   --add-modules javafx.controls,javafx.base,javafx.graphics,javafx.fxml
   ```

---

## 🔨 Build & Run

- **Build Project**: `F11`
- **Run Project**: `F6`

---

## 🌟 Features & Functionality

### 🧾 Multi-Account Management
- Manage multiple trading accounts
- Switch seamlessly between accounts
- Each account stores data independently

---

### 📈 Trade Logging
- Log:
  - Symbol, Entry/Exit Price, Size, Side (LONG/SHORT)
  - Date, Duration, Returns, Balance
  - Setup Strategy, Entry Type
- Attach screenshots and notes to trades

---

### 📊 Performance Analysis
- Dashboard Metrics:
  - Win/Loss Ratio
  - Total P&L
  - Average Win/Loss
  - Performance by Setup Type
  - Best/Worst Trades
- **Interactive Charts**:
  - Profit/Loss over time
  - Daily/Weekly/Monthly Trends

---

### 📅 Calendar View
- Color-coded monthly performance
- Hover or click to view daily trades
- Daily trade count & P&L summaries

---

### 📓 Trade Journal
- Add rich notes per trade
- Upload screenshots/charts
- Strategy documentation

---

### ⚙️ Setup & Entry Type Management
- Customize and manage trade setups
- Note entry strategies with images
- Analyze setups by performance

---

### 🔄 Import & Export
- **CSV Import/Export**
- **Backup and Restore** functionality

---

### 🎨 Modern UI
- Dark-themed, responsive design
- Searchable, filterable tables

---

### 🔐 Data Storage & Privacy
- Local storage only:  
  `TradingJournalData` directory  
- No cloud sync, complete privacy

---

### ⌨️ Keyboard Shortcuts
| Action              | Shortcut     |
|---------------------|--------------|
| Add Trade           | `Ctrl + N`   |
| Open Dashboard      | `Ctrl + D`   |
| Calendar View       | `Ctrl + C`   |
| Switch Account      | `Ctrl + Tab` |

---

## 📌 Additional Features

- Set custom starting balance & risk parameters
- Write and follow personal **trading rules**
- **Symbol management** for faster entry

---

## 📬 Support & Contact

For support, suggestions, or feature requests:  
📧 **furkanozturk5406@hotmail.com**

---

> **The app is under active development. More features and visuals are coming soon — follow the repository to stay updated!**

