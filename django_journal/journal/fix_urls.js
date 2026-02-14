const fs = require('fs');

// Read the API service file
const filePath = '../react_journal/src/services/apiService.ts';
let content = fs.readFileSync(filePath, 'utf8');

// 1. Fix fetch calls that include API_BASE_URL when using fetchWithAuth
// This causes double URL prefixing like: apihttp://localhost:8000/api/...
const fetchWithAuthApiBaseUrlPattern = /fetchWithAuth\(`\${API_BASE_URL}([^`]+)`/g;
content = content.replace(fetchWithAuthApiBaseUrlPattern, 'fetchWithAuth(`$1`');

// 2. Special fix for dashboard metrics and other complex URL constructions
const dashboardPattern = /const endpoint = accountId \? `\${API_BASE_URL}([^`]+)\${accountId}([^`]*)` : `\${API_BASE_URL}([^`]+)`;/g;
content = content.replace(dashboardPattern, 'const endpoint = accountId ? `$1${accountId}$2` : `$3`;');

// Save the updated file
fs.writeFileSync(filePath, content);
console.log('URLs fixed successfully!');
