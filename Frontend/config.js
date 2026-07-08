// Default to localhost for local development. 
// You will replace the Render URL with your actual Render app URL once deployed!
const API_BASE_URL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
    ? 'http://localhost:8080' 
    : 'https://speakmate-backend-fz8i.onrender.com';
