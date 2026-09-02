param (
    [string]$ReleaseNotes = "Slyvos Pre-Alpha Development Build Update"
)

$ErrorActionPreference = "Stop"

$launcherDir = "C:\Users\Marcos Calvin Dudang\Desktop\Slyvos\slyvos-launcher"
$updatesDir = "C:\Users\Marcos Calvin Dudang\Desktop\Slyvos\slyvos-launcher-updates"
$gradleFile = "$launcherDir\app\build.gradle.kts"

$env:JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host " 🚀 SLYVOS AUTOMATED DEVELOPMENT PUBLISHING WORKFLOW" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan

# 1. Run Unit Tests
Write-Host ""
Write-Host "[1/7] Running Unit Tests..." -ForegroundColor Yellow
Set-Location $launcherDir
cmd /c ".\gradlew.bat test"
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Unit tests failed! Aborting publishing." -ForegroundColor Red
    exit 1
}
Write-Host "✓ All unit tests passed successfully." -ForegroundColor Green

# 2. Parse and Increment Build Number in app/build.gradle.kts
Write-Host ""
Write-Host "[2/7] Parsing current build number from app/build.gradle.kts..." -ForegroundColor Yellow
$lines = Get-Content $gradleFile

$currentBuildNum = 2
$quote = [char]34
foreach ($line in $lines) {
    if ($line.Contains("BUILD_NUMBER")) {
        $parts = $line.Split($quote)
        for ($i = 0; $i -lt $parts.Length; $i++) {
            if ($parts[$i] -eq "BUILD_NUMBER" -and ($i + 2) -lt $parts.Length) {
                $currentBuildNum = [int]($parts[$i + 2])
                break
            }
        }
    }
}

$newBuildNum = $currentBuildNum + 1
$newVersionCode = $newBuildNum
$formattedBuildNum = "{0:D3}" -f $newBuildNum
$newVersionName = "Pre-Alpha Build #$formattedBuildNum"

Write-Host "Upgrading Build Number: #$("{0:D3}" -f $currentBuildNum) -> #$formattedBuildNum" -ForegroundColor Cyan

$newLines = @()
foreach ($line in $lines) {
    if ($line.Contains("versionCode =")) {
        $newLines += "        versionCode = $newVersionCode"
    } elseif ($line.Contains("versionName =")) {
        $newLines += "        versionName = ${quote}$newVersionName${quote}"
    } elseif ($line.Contains("BUILD_NUMBER")) {
        $newLines += "        buildConfigField(${quote}int${quote}, ${quote}BUILD_NUMBER${quote}, ${quote}$newBuildNum${quote})"
    } else {
        $newLines += $line
    }
}

$newLines | Set-Content -Path $gradleFile -Encoding UTF8

# 3. Assemble Debug APK
Write-Host ""
Write-Host "[3/7] Assembling Debug APK for Build #$formattedBuildNum..." -ForegroundColor Yellow
cmd /c ".\gradlew.bat assembleDebug"
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ assembleDebug failed! Aborting publishing." -ForegroundColor Red
    exit 1
}
Write-Host "✓ APK assembled successfully." -ForegroundColor Green

# 4. Calculate SHA-256 Checksum
$apkSource = "$launcherDir\app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkSource)) {
    Write-Host "❌ APK output not found at $apkSource" -ForegroundColor Red
    exit 1
}

$apkFilename = "slyvos-prealpha-$formattedBuildNum.apk"
$apkDest = "$updatesDir\$apkFilename"
Copy-Item -Path $apkSource -Destination $apkDest -Force

$sha256 = (Get-FileHash -Algorithm SHA256 $apkDest).Hash.ToLower()
Write-Host "✓ SHA-256 Checksum: $sha256" -ForegroundColor Green

# 5. Update pre-alpha.json manifest in slyvos-launcher-updates
Write-Host ""
Write-Host "[4/7] Updating slyvos-launcher-updates manifest pre-alpha.json..." -ForegroundColor Yellow
$rawApkUrl = "https://raw.githubusercontent.com/ryo-builds/slyvos-launcher-updates/main/$apkFilename"
$manifestFile = "$updatesDir\pre-alpha.json"

$manifestObj = [ordered]@{
    buildNumber = $newBuildNum
    versionCode = $newVersionCode
    versionName = $newVersionName
    releaseStage = "PRE_ALPHA"
    releaseNotes = $ReleaseNotes
    apkUrl = $rawApkUrl
    apkSha256 = $sha256
    publishedAt = [DateTime]::UtcNow.ToString("yyyy-MM-ddTHH:mm:ssZ")
    minimumSupportedBuildNumber = 1
}

$manifestJson = $manifestObj | ConvertTo-Json -Depth 5
Set-Content -Path $manifestFile -Value $manifestJson -Encoding UTF8
Write-Host "✓ Manifest pre-alpha.json updated." -ForegroundColor Green

# 6. Commit and Push slyvos-launcher-updates
Write-Host ""
Write-Host "[5/7] Pushing update release to slyvos-launcher-updates..." -ForegroundColor Yellow
git -C $updatesDir fetch origin
git -C $updatesDir pull origin main --rebase
git -C $updatesDir add .
git -C $updatesDir commit -m "Release Slyvos Pre-Alpha Build #$formattedBuildNum"
git -C $updatesDir push origin main
Write-Host "✓ Pushed update files to GitHub slyvos-launcher-updates." -ForegroundColor Green

# 7. Commit and Push slyvos-launcher Codebase
Write-Host ""
Write-Host "[6/7] Committing and pushing codebase changes to slyvos-launcher..." -ForegroundColor Yellow
git -C $launcherDir add .
git -C $launcherDir commit -m "Build #$formattedBuildNum: $ReleaseNotes"
git -C $launcherDir push origin main
Write-Host "✓ Pushed codebase to GitHub slyvos-launcher." -ForegroundColor Green

# 8. Report Final Verification Summary
Write-Host ""
Write-Host "=====================================================" -ForegroundColor Green
Write-Host " 🎉 BUILD #$formattedBuildNum PUBLISHED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "Identity: Slyvos Pre-Alpha Build #$formattedBuildNum"
Write-Host "Manifest URL: https://raw.githubusercontent.com/ryo-builds/slyvos-launcher-updates/main/pre-alpha.json"
Write-Host "APK URL: $rawApkUrl"
Write-Host "SHA-256: $sha256"
