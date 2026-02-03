# Script to generate Android icons from SVG
# Requires: npm install -g svgexport or use online tool

Write-Host "=== Android Icon Generator ===" -ForegroundColor Cyan
Write-Host ""

$svgPath = "assets\icon.svg"
$outputDir = "android\app\src\main\res"

# Check if SVG exists
if (-not (Test-Path $svgPath)) {
    Write-Host "Error: icon.svg not found in assets folder!" -ForegroundColor Red
    exit 1
}

Write-Host "SVG Icon created at: $svgPath" -ForegroundColor Green
Write-Host ""
Write-Host "To generate PNG icons for Android, you have 3 options:" -ForegroundColor Yellow
Write-Host ""
Write-Host "Option 1 - Online Tool (Easiest):" -ForegroundColor Cyan
Write-Host "  1. Visit: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html"
Write-Host "  2. Upload: $svgPath"
Write-Host "  3. Download and extract to: $outputDir"
Write-Host ""
Write-Host "Option 2 - VSCode Extension:" -ForegroundColor Cyan
Write-Host "  1. Install 'SVG' extension by jock.feng"
Write-Host "  2. Right-click icon.svg > SVG: Export PNG"
Write-Host "  3. Export these sizes manually:"
Write-Host "     - mipmap-mdpi: 48x48"
Write-Host "     - mipmap-hdpi: 72x72"
Write-Host "     - mipmap-xhdpi: 96x96"
Write-Host "     - mipmap-xxhdpi: 144x144"
Write-Host "     - mipmap-xxxhdpi: 192x192"
Write-Host ""
Write-Host "Option 3 - Command Line (if you have svgexport):" -ForegroundColor Cyan
Write-Host "  npm install -g svgexport"
Write-Host "  Then run this script again"
Write-Host ""

# Check if svgexport is available
$svgexport = Get-Command svgexport -ErrorAction SilentlyContinue

if ($svgexport) {
    Write-Host "svgexport found! Generating icons..." -ForegroundColor Green
    
    $sizes = @(
        @{name="mdpi"; size=48},
        @{name="hdpi"; size=72},
        @{name="xhdpi"; size=96},
        @{name="xxhdpi"; size=144},
        @{name="xxxhdpi"; size=192}
    )
    
    foreach ($s in $sizes) {
        $dir = Join-Path $outputDir "mipmap-$($s.name)"
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Force -Path $dir | Out-Null
        }
        
        $output = Join-Path $dir "ic_launcher.png"
        Write-Host "  Generating $($s.size)x$($s.size) -> $output"
        
        & svgexport $svgPath $output "$($s.size):$($s.size)"
        
        # Also create round icon
        $outputRound = Join-Path $dir "ic_launcher_round.png"
        & svgexport $svgPath $outputRound "$($s.size):$($s.size)"
    }
    
    Write-Host ""
    Write-Host "Icons generated successfully!" -ForegroundColor Green
    Write-Host "Now rebuild your APK: .\gradlew.bat assembleDebug" -ForegroundColor Yellow
} else {
    Write-Host "For now, using Android Vector Drawable (already included)" -ForegroundColor Green
    Write-Host "The app will use the XML vector icon automatically." -ForegroundColor Green
}

Write-Host ""
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
