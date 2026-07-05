$ErrorActionPreference = "Stop"

$ticks = (Get-Date).Ticks
$username = "testuser$ticks"
$email = "test$ticks@example.com"
$password = "password123"

$signupJson = @{
    username = $username
    email = $email
    password = $password
} | ConvertTo-Json

$maxRetries = 30
$serverUp = $false
$token = ""

Write-Host "Waiting for server to start on port 8080..."

for ($i=0; $i -lt $maxRetries; $i++) {
    try {
        $signup = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body $signupJson -ContentType "application/json"
        Write-Host "Signup Success: $($signup.message)"
        
        $loginJson = @{
            email = $email
            password = $password
        } | ConvertTo-Json
        
        $login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginJson -ContentType "application/json"
        Write-Host "Login Success: Token received"
        $token = $login.token
        $serverUp = $true
        break
    } catch {
        Write-Host "Server not ready or error... retrying in 3 seconds. ($i)"
        Start-Sleep -Seconds 3
    }
}

if ($serverUp) {
    try {
        $headers = @{ Authorization = "Bearer $token" }
        
        Write-Host "Testing Dashboard..."
        $dash = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/summary" -Method Get -Headers $headers
        Write-Host "Dashboard Success! AI Suggestion: $($dash.aiSuggestion)"

        Write-Host "Testing AI Friend Chat..."
        $chatJson = @{
            message = "Hello, I am testing the AI friend."
        } | ConvertTo-Json
        $chat = Invoke-RestMethod -Uri "http://localhost:8080/api/chat/send" -Method Post -Body $chatJson -ContentType "application/json" -Headers $headers
        Write-Host "Chat Success! AI Reply: $($chat.aiText)"
        
    } catch {
        Write-Host "Error during authenticated tests: $_"
    }
} else {
    Write-Host "Failed to verify. Server didn't start or tests failed."
}
