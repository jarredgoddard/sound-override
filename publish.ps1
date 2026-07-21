# publish.ps1 - Publish the Sound Override plugin to GitHub and open the
# RuneLite Plugin Hub submission PR.
#
# Run from INSIDE the extracted sound-override folder:
#   PS> .\publish.ps1
#
# Requirements:
#   - git            (winget install Git.Git)
#   - GitHub CLI     (winget install GitHub.cli)  then: gh auth login
#
# What it does:
#   1. Initializes the plugin repo, commits, creates github.com/<user>/sound-override, pushes
#   2. Optionally runs a local gradle build as a pre-flight check
#   3. Forks runelite/plugin-hub, adds plugins/sound-override with your repo+commit, opens the PR

param(
    [string]$GitHubUser = "jarredgoddard",
    [string]$RepoName   = "sound-override",
    [string]$Branch     = "master",
    [switch]$SkipBuild,
    [switch]$SkipHubPR   # push the plugin repo only; do the Hub PR later
)

$ErrorActionPreference = "Stop"

function Assert-Tool($name, $hint) {
    if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
        Write-Host "ERROR: '$name' not found. Install it: $hint" -ForegroundColor Red
        exit 1
    }
}

# Run a native command whose failure/stderr is EXPECTED (probe calls).
# Prevents ErrorActionPreference=Stop from treating stderr as fatal.
function Invoke-Quiet([scriptblock]$cmd) {
    $eap = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try { & $cmd 2>&1 | Out-Null } catch { } finally { $ErrorActionPreference = $eap }
    return $LASTEXITCODE
}

Assert-Tool git "winget install Git.Git"
Assert-Tool gh  "winget install GitHub.cli"

# gh must be authenticated
if ((Invoke-Quiet { gh auth status }) -ne 0) {
    Write-Host "GitHub CLI isn't authenticated. Running 'gh auth login'..." -ForegroundColor Yellow
    gh auth login
    if ($LASTEXITCODE -ne 0) { exit 1 }
}

# Sanity: are we in the plugin folder?
if (-not (Test-Path "runelite-plugin.properties")) {
    Write-Host "ERROR: runelite-plugin.properties not found. Run this from inside the sound-override folder." -ForegroundColor Red
    exit 1
}

# ---------------------------------------------------------------
# Step 1 - optional local build pre-flight
# ---------------------------------------------------------------
if (-not $SkipBuild) {
    if (Test-Path ".\gradlew.bat") {
        Write-Host "`n=== Pre-flight: gradlew build ===" -ForegroundColor Cyan
        .\gradlew.bat build --no-daemon
        if ($LASTEXITCODE -ne 0) { Write-Host "Build failed - fix before publishing." -ForegroundColor Red; exit 1 }
    }
    elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
        Write-Host "`n=== Pre-flight: gradle build ===" -ForegroundColor Cyan
        gradle build --no-daemon
        if ($LASTEXITCODE -ne 0) { Write-Host "Build failed - fix before publishing." -ForegroundColor Red; exit 1 }
    }
    else {
        Write-Host "`nNo gradle wrapper or gradle install found - skipping local build." -ForegroundColor Yellow
        Write-Host "Hub CI will still build your commit; a local pass just catches failures earlier."
        $answer = Read-Host "Continue without building? (y/n)"
        if ($answer -ne "y") { exit 0 }
    }
}

# ---------------------------------------------------------------
# Step 2 - plugin repo: init, commit, create, push
# ---------------------------------------------------------------
Write-Host "`n=== Publishing plugin repo to github.com/$GitHubUser/$RepoName ===" -ForegroundColor Cyan

if (-not (Test-Path ".git")) {
    git init -b $Branch
}

git add -A
# Commit only if there are staged changes
if ((Invoke-Quiet { git diff --cached --quiet }) -ne 0) {
    git commit -m "Sound Override plugin"
} else {
    Write-Host "Nothing new to commit." -ForegroundColor Yellow
}

# Create the GitHub repo if it doesn't exist yet
if ((Invoke-Quiet { gh repo view "$GitHubUser/$RepoName" }) -ne 0) {
    gh repo create "$GitHubUser/$RepoName" --public --source . --remote origin --push
    if ($LASTEXITCODE -ne 0) { Write-Host "Repo creation failed." -ForegroundColor Red; exit 1 }
} else {
    if (-not (git remote | Select-String -Quiet "^origin$")) {
        git remote add origin "https://github.com/$GitHubUser/$RepoName.git"
    }
    git push -u origin $Branch
    if ($LASTEXITCODE -ne 0) { Write-Host "Push failed." -ForegroundColor Red; exit 1 }
}

$sha = (git rev-parse HEAD).Trim()
Write-Host "Pushed. Commit SHA: $sha" -ForegroundColor Green

if ($SkipHubPR) {
    Write-Host "`n-SkipHubPR set - stopping after plugin repo push." -ForegroundColor Yellow
    Write-Host "Descriptor for later:`n  repository=https://github.com/$GitHubUser/$RepoName.git`n  commit=$sha"
    exit 0
}

# ---------------------------------------------------------------
# Step 3 - plugin-hub fork + descriptor + PR
# ---------------------------------------------------------------
Write-Host "`n=== Opening Plugin Hub submission PR ===" -ForegroundColor Cyan

$hubDir = Join-Path ([System.IO.Path]::GetTempPath()) "plugin-hub-$([guid]::NewGuid().ToString('N').Substring(0,8))"

# Fork (no-op if already forked) and clone the fork
Invoke-Quiet { gh repo fork runelite/plugin-hub --clone=false } | Out-Null
gh repo clone "$GitHubUser/plugin-hub" $hubDir -- --depth 1
if ($LASTEXITCODE -ne 0) { Write-Host "Couldn't clone your plugin-hub fork." -ForegroundColor Red; exit 1 }

Push-Location $hubDir
try {
    # Stay in sync with upstream master so the PR is clean
    Invoke-Quiet { git remote add upstream https://github.com/runelite/plugin-hub.git } | Out-Null
    git fetch upstream master --depth 1
    git checkout -B "add-$RepoName" upstream/master

    # Write the two-line descriptor (LF endings, no BOM - CI is picky)
    $descriptor = "repository=https://github.com/$GitHubUser/$RepoName.git`ncommit=$sha`n"
    [System.IO.File]::WriteAllText((Join-Path $hubDir "plugins\$RepoName"), $descriptor.Replace("`r`n","`n"))

    git add "plugins/$RepoName"
    git commit -m "Add $RepoName plugin"
    git push -u origin "add-$RepoName" --force

    gh pr create `
        --repo runelite/plugin-hub `
        --base master `
        --head "${GitHubUser}:add-$RepoName" `
        --title "Add $RepoName plugin" `
        --body "Adds Sound Override: replaces game sound effects and announces game events with user-supplied audio. Supports raw sound effect ID replacement plus 34 event presets. Detection logic ported (with credit) from c-engineer-completed and odablock-sounds, both BSD-2."

    if ($LASTEXITCODE -eq 0) {
        Write-Host "`nPR opened. Watch CI on the PR page - green check = compiled." -ForegroundColor Green
        Write-Host "If review requests changes: push fixes to $RepoName, then re-run this script with the same PR open - it updates the commit hash on the same branch."
    }
}
finally {
    Pop-Location
}
