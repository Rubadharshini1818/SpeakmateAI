$file = 'D:\SpeakmateAI\profile.html'
$content = Get-Content $file -Raw -Encoding UTF8

# 1. Remove bio placeholder line
$content = $content -replace '<p class="text-body-lg text-on-surface max-w-2xl">Aspiring communicator \| Learning English one day at a time [^\<]*</p>\r?\n', ''

# 2. Remove entire Achievements Row section
$content = $content -replace '(?s)<!-- Achievements Row -->.*?</section>\r?\n', ''

# 3. Remove Certificates section only (stops before Account Activity Stats)
$content = $content -replace '(?s)<!-- Certificates -->.*?</section>\r?\n<!-- Account Activity Stats -->', '<!-- Account Activity Stats -->'

Set-Content $file -Value $content -Encoding UTF8 -NoNewline
Write-Output "Steps 1-3 complete"
