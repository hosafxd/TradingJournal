import React from 'react';
import TradesPage from './pages/TradesPage';
import { useAccount } from './contexts/AccountContext';
import AccountManagementPage from './pages/AccountManagementPage';
import DashboardPage from './pages/DashboardPage'; 
import CalendarPage from './pages/CalendarPage';
import StrategiesPage from './pages/StrategiesPage';
import ImportExportPage from './pages/ImportExportPage';
import LoginPage from './pages/LoginPage'; // Import LoginPage
import RegistrationPage from './pages/RegistrationPage'; // Import RegistrationPage
import { useAuth } from './contexts/AuthContext'; // Import useAuth

type Page = 'dashboard' | 'accounts' | 'trades' | 'calendar' | 'strategies' | 'import-export' | 'login' | 'register';

function App() {
  const { currentAccount } = useAccount();
  const { user, logout, isLoading: authIsLoading } = useAuth(); // Get user and logout from AuthContext
  const [currentPage, setCurrentPage] = React.useState<Page>('dashboard');

  // If still verifying token, show loading
  if (authIsLoading) {
    return <div className="App dashboard-container loading" style={{display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', fontSize: '1.5rem'}}>Loading Application...</div>;
  }

  // If not authenticated, show login or registration page
  if (!user) {
    return (
      <div className="App dashboard-container">
        <nav className="nav-tabs">
          <button className={`nav-tab ${currentPage === 'login' ? 'active' : ''}`} onClick={() => setCurrentPage('login')}>Login</button>
          <button className={`nav-tab ${currentPage === 'register' ? 'active' : ''}`} onClick={() => setCurrentPage('register')}>Register</button>
        </nav>
        {currentPage === 'login' && <LoginPage />}
        {currentPage === 'register' && <RegistrationPage />}
        {/* Default to login if no specific auth page is selected */}
        {currentPage !== 'login' && currentPage !== 'register' && <LoginPage />} 
      </div>
    );
  }

  // Authenticated view
  return (
    <div className="App dashboard-container"> 
      <nav className="nav-tabs"> 
        <button
          className={`nav-tab ${currentPage === 'dashboard' ? 'active' : ''}`}
          onClick={() => setCurrentPage('dashboard')}
        >
          Dashboard
        </button>
        <button 
          className={`nav-tab ${currentPage === 'accounts' ? 'active' : ''}`}
          onClick={() => setCurrentPage('accounts')}
        >
          Accounts
        </button>
        <button 
          className={`nav-tab ${currentPage === 'trades' ? 'active' : ''}`}
          onClick={() => setCurrentPage('trades')}
        >
          Trades
        </button>
        <button 
          className={`nav-tab ${currentPage === 'calendar' ? 'active' : ''}`}
          onClick={() => setCurrentPage('calendar')}
        >
          Calendar
        </button>
        <button 
          className={`nav-tab ${currentPage === 'strategies' ? 'active' : ''}`}
          onClick={() => setCurrentPage('strategies')}
        >
          Setups/Entries
        </button>
        <button 
          className={`nav-tab ${currentPage === 'import-export' ? 'active' : ''}`}
          onClick={() => setCurrentPage('import-export')}
        >
          Import/Export
        </button>
        <div style={{ marginLeft: 'auto', padding: '12px 20px', display: 'flex', alignItems: 'center', gap: '15px' }}>
          {currentAccount && (
            <>
              <span className="metric-label" style={{ textTransform: 'none'}}>ACTIVE ACC:</span>
              <span className="metric-value positive" style={{ fontSize: '1rem' }}>{currentAccount.name}</span>
            </>
          )}
          {user && <span className="text-muted">User: {user.username}</span>}
          <button onClick={() => { logout(); setCurrentPage('login');}} className="btn btn-danger" style={{padding: '8px 15px'}}>Logout</button>
        </div>
      </nav>
      
      {currentPage === 'dashboard' && <DashboardPage />}
      {currentPage === 'accounts' && <AccountManagementPage />}
      {currentPage === 'trades' && <TradesPage />}
      {currentPage === 'calendar' && <CalendarPage />}
      {currentPage === 'strategies' && <StrategiesPage />}
      {currentPage === 'import-export' && <ImportExportPage />}
    </div>
  );
}

export default App;
