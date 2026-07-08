// Default to localhost for local development. 
// You will replace the Railway URL with your actual Railway app URL once deployed!
const API_BASE_URL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
    ? 'http://localhost:8080' 
    : 'https://your-app.up.railway.app';
