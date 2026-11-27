# Run this before 
# powershell -ExecutionPolicy Bypass

function Convert-HeadersToProperties {
    param([string[]]$headers)

    $headers |
        Where-Object { -not ($_ -like ":*") } |   # remove pseudo-headers
        ForEach-Object {
            $name, $value = $_ -split ":", 2
            "$($name.Trim())=$($value.Trim())"
        }
}

$temp_folder = "C:\Ruslan\Dropbox\Code\Portfolio\temp"

Start-Process chrome.exe `
  "--log-net-log=$temp_folder\yf-log.json --net-log-capture-mode=IncludeSensitive --no-first-run --incognito --user-data-dir=$temp_folder\yf\ https://finance.yahoo.com/quote/MSFT/history/" `
  -Wait


Start-Process chrome.exe `
  "--log-net-log=$temp_folder\gf-log.json --net-log-capture-mode=IncludeSensitive --no-first-run --incognito --user-data-dir=$temp_folder\gf\ https://www.gurufocus.com/term/fscore/MSFT" `
  -Wait

Remove-Item "$temp_folder\yf\" -Recurse -Force
Remove-Item "$temp_folder\gf\" -Recurse -Force

$log = Get-Content "$temp_folder\yf-log.json" -Raw | ConvertFrom-Json
$log.events | Where-Object {
        # Convert header list into a single string block for easier matching
        $headers = ($_.params.headers -join "`n")

        $headers -match ":authority:\s*finance\.yahoo\.com" -and
        $headers -match ":path:\s*.*\/MSFT" -and
        $headers -match "cookie:\s*.+"
    } | Sort-Object id -Unique | ForEach-Object {
        Convert-HeadersToProperties $_.params.headers
        ""
    }

$log = Get-Content "$temp_folder\gf-log.json" -Raw | ConvertFrom-Json
$log.events | Where-Object {
        # Convert header list into a single string block for easier matching
        $headers = ($_.params.headers -join "`n")

        $headers -match ":authority:\s*www\.gurufocus\.com" -and
        $headers -match ":path:\s*.*\/MSFT" -and
        $headers -match "cookie:\s*.+"
    } | Sort-Object id -Unique | ForEach-Object {
        Convert-HeadersToProperties $_.params.headers
        ""
    }

Remove-Item "$temp_folder\yf-log.json" -Force
Remove-Item "$temp_folder\gf-log.json" -Force