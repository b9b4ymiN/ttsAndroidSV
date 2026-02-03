from PIL import Image, ImageDraw
import os

# Icon sizes for different densities
sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

# Colors
bg_color = (65, 105, 225)  # Royal Blue #4169E1
fg_color = (255, 255, 255)  # White

def create_icon(size):
    """Create a TTS microphone icon"""
    # Create image with blue background
    img = Image.new('RGBA', (size, size), bg_color + (255,))
    draw = ImageDraw.Draw(img)
    
    # Calculate dimensions
    center = size // 2
    mic_width = size // 5
    mic_height = size // 4
    stroke = max(2, size // 25)
    
    # Microphone body (rounded rectangle)
    mic_x = center - mic_width // 2
    mic_y = size // 3 - mic_height // 2
    draw.rounded_rectangle(
        [(mic_x, mic_y), (mic_x + mic_width, mic_y + mic_height)],
        radius=mic_width // 2,
        fill=fg_color
    )
    
    # Microphone arc (bottom curve)
    arc_y = mic_y + mic_height + size // 20
    arc_size = size // 6
    draw.arc(
        [(center - arc_size, arc_y), (center + arc_size, arc_y + arc_size)],
        start=0, end=180,
        fill=fg_color,
        width=stroke
    )
    
    # Microphone stand (vertical line)
    stand_top = arc_y + arc_size // 2
    stand_bottom = stand_top + size // 7
    draw.line([(center, stand_top), (center, stand_bottom)], fill=fg_color, width=stroke)
    
    # Microphone base (horizontal line)
    base_width = size // 10
    draw.line(
        [(center - base_width, stand_bottom), (center + base_width, stand_bottom)],
        fill=fg_color,
        width=stroke
    )
    
    # Sound waves - left
    wave_offset = size // 4
    wave_y_start = mic_y + mic_height // 4
    wave_y_end = mic_y + mic_height * 3 // 4
    
    # Inner left wave
    draw.arc(
        [(center - wave_offset - size // 8, wave_y_start),
         (center - wave_offset, wave_y_end)],
        start=270, end=90,
        fill=fg_color,
        width=stroke
    )
    
    # Inner right wave
    draw.arc(
        [(center + wave_offset, wave_y_start),
         (center + wave_offset + size // 8, wave_y_end)],
        start=90, end=270,
        fill=fg_color,
        width=stroke
    )
    
    # Outer left wave (more transparent)
    draw.arc(
        [(center - wave_offset - size // 5, wave_y_start - size // 15),
         (center - wave_offset - size // 15, wave_y_end + size // 15)],
        start=270, end=90,
        fill=fg_color + (150,),
        width=max(1, stroke - 1)
    )
    
    # Outer right wave (more transparent)
    draw.arc(
        [(center + wave_offset + size // 15, wave_y_start - size // 15),
         (center + wave_offset + size // 5, wave_y_end + size // 15)],
        start=90, end=270,
        fill=fg_color + (150,),
        width=max(1, stroke - 1)
    )
    
    return img

# Generate icons for all densities
base_path = os.path.join('android', 'app', 'src', 'main', 'res')

for density, size in sizes.items():
    print(f"Generating {density} icon ({size}x{size})...")
    
    # Create icon
    icon = create_icon(size)
    
    # Save to mipmap folders
    mipmap_dir = os.path.join(base_path, f'mipmap-{density}')
    os.makedirs(mipmap_dir, exist_ok=True)
    
    # Save both regular and round icons
    icon.save(os.path.join(mipmap_dir, 'ic_launcher.png'), 'PNG')
    icon.save(os.path.join(mipmap_dir, 'ic_launcher_round.png'), 'PNG')
    
    print(f"  ✓ Saved to {mipmap_dir}")

print("\n✅ All icons generated successfully!")
print("Now rebuild your APK: cd android && .\\gradlew.bat assembleDebug")
