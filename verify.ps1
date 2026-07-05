$c = Get-Content 'D:\SpeakmateAI\profile.html' -Raw
Write-Output ("edit-modal present:    " + [bool]($c -match "edit-modal"))
Write-Output ("saveProfile present:   " + [bool]($c -match "saveProfile"))
Write-Output ("handleAvatarChange:    " + [bool]($c -match "handleAvatarChange"))
Write-Output ("old prompt() present:  " + [bool]($c -match "prompt\("))
Write-Output ("Achievements removed:  " + (-not [bool]($c -match "Recent Achievements")))
Write-Output ("Certificates removed:  " + (-not [bool]($c -match "Foundational English")))
