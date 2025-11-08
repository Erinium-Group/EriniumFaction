from PIL import Image, ImageDraw
import os

output_dir = r"D:\Mods Minecraft\EF\Erinium Faction\src\main\resources\assets\erinium_faction\textures\gui"

# === TITANIUM COMPRESSOR - Background simple et propre ===
print("Creating simple TitaniumCompressor background (176x166)...")

img = Image.new('RGBA', (176, 166), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Background uni bleu nuit foncé
draw.rectangle([0, 0, 176, 166], fill=(10, 14, 39, 255))

# Quelques étoiles subtiles seulement
import random
random.seed(42)
for _ in range(30):  # Beaucoup moins d'étoiles
    x = random.randint(0, 175)
    y = random.randint(0, 165)
    opacity = random.randint(100, 200)
    draw.point((x, y), fill=(255, 255, 255, opacity))

# Border principale cyan
draw.rectangle([0, 0, 175, 165], outline=(0, 255, 255, 100), width=1)

# === TITLE BAR (clean) ===
draw.rectangle([3, 3, 173, 21], fill=(26, 21, 53, 200), outline=(0, 255, 255, 50))

# === MACHINE PANEL ===
draw.rectangle([5, 23, 171, 81], fill=(26, 21, 53, 180), outline=(157, 78, 221, 80))

# Energy bar background
draw.rectangle([9, 29, 25, 75], fill=(15, 17, 35, 255), outline=(0, 255, 255, 120))

# Input slot
draw.rectangle([31, 35, 51, 55], fill=(15, 17, 35, 255), outline=(0, 255, 255, 150))

# Progress bar background
draw.rounded_rectangle([59, 41, 109, 51], radius=4, fill=(15, 17, 35, 255), outline=(157, 78, 221, 80))

# Output slot
draw.rectangle([117, 35, 137, 55], fill=(15, 17, 35, 255), outline=(157, 78, 221, 150))

# === INVENTORY PANEL ===
draw.rectangle([5, 95, 171, 153], fill=(26, 21, 53, 170), outline=(91, 33, 182, 60))

# Inventory slots (3x9)
for row in range(3):
    for col in range(9):
        x = 7 + col * 18
        y = 97 + row * 18
        draw.rectangle([x, y, x+17, y+17], fill=(15, 17, 35, 255), outline=(91, 33, 182, 100))

# === HOTBAR ===
# Hotbar slots
for col in range(9):
    x = 7 + col * 18
    y = 155
    draw.rectangle([x, y, x+17, y+17], fill=(15, 17, 35, 255), outline=(0, 255, 255, 120))

# === CONFIG BUTTON ===
draw.rounded_rectangle([150, 5, 170, 21], radius=2, fill=(26, 21, 53, 220), outline=(0, 255, 255, 150))

# Gear icon simplifié
cx, cy = 160, 13
draw.ellipse([cx-3, cy-3, cx+3, cy+3], outline=(0, 255, 255, 200))
draw.ellipse([cx-1, cy-1, cx+1, cy+1], fill=(0, 255, 255, 255))

img.save(os.path.join(output_dir, "titanium_compressor.png"))
print(f"[OK] Fixed titanium_compressor.png - Simple clean background")
