import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import './cyberpunk-theme.css'; // Corrected import path
import App from './App';
import reportWebVitals from './reportWebVitals';
import { AccountProvider } from './contexts/AccountContext'; // Import the AccountProvider
import { AuthProvider } from './contexts/AuthContext'; // Import AuthProvider

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  <React.StrictMode>
    <AuthProvider> {/* Wrap with AuthProvider */}
      <AccountProvider> {/* Wrap App with AccountProvider */}
        <App />
      </AccountProvider>
    </AuthProvider>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
