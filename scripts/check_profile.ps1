$c = Get-Content 'D:\SpeakmateAI\profile.html' -Raw
Write-Output ("Achievements present: " + [bool]($c -match "Recent Achievements"))
Write-Output ("Certificates present: " + [bool]($c -match "Certificates"))
Write-Output ("Bio line present: "     + [bool]($c -match "Aspiring communicator"))
Write-Output ("Account Activity present: " + [bool]($c -match "Account Activity"))
