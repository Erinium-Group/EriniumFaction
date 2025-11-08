from PIL import Image, ImageDraw, ImageFont
import os

assets_dir = r"D:\Mods Minecraft\EF\Erinium Faction\informations\designs\assets"
output_dir = r"D:\Mods Minecraft\EF\Erinium Faction\src\main\resources\assets\erinium_faction\textures\gui"

os.makedirs(output_dir, exist_ok=True)

# === TITANIUM COMPRESSOR SCREEN (176x166) ===
print("Creating TitaniumCompressorScreen.png (176x166)...")

# Créer l'image de base avec alpha
img = Image.new('RGBA', (176, 166), (10, 14, 39, 255))  # Background bleu nuit foncé

draw = ImageDraw.Draw(img)

# Main panel background avec gradient simulé
for y in range(166):
    # Gradient vertical subtil
    intensity = int(26 + (21 - 26) * (y / 166))  # De #1a1535 à #0f1123
    draw.rectangle([0, y, 176, y+1], fill=(intensity, intensity-5, intensity+18, 255))

# Ajouter des étoiles (starfield)
import random
random.seed(42)
for _ in range(80):
    x = random.randint(0, 175)
    y = random.randint(0, 165)
    size = random.choice([1, 1, 1, 2])
    opacity = random.randint(100, 255)
    color = random.choice([(255, 255, 255), (0, 255, 255), (157, 78, 221)])
    if size == 1:
        draw.point((x, y), fill=(*color, opacity))
    else:
        draw.ellipse([x, y, x+size, y+size], fill=(*color, opacity))

# Border cyan glow
draw.rectangle([0, 0, 175, 165], outline=(0, 255, 255, 150), width=1)
draw.rectangle([1, 1, 174, 164], outline=(0, 255, 255, 80), width=1)

# === TITLE SECTION (4, 4, 168x16) ===
draw.rectangle([4, 4, 172, 20], fill=(26, 21, 53, 240))
# Ligne de séparation cyan
for x in range(4, 172):
    opacity = int(128 * (1 - abs((x - 88) / 84)))  # Gradient centré
    draw.point((x, 20), fill=(0, 255, 255, opacity))

# Coins holographiques titre
draw.line([4, 4, 14, 4], fill=(0, 255, 255, 150), width=1)
draw.line([4, 4, 4, 14], fill=(0, 255, 255, 150), width=1)
draw.line([172, 4, 162, 4], fill=(0, 255, 255, 150), width=1)
draw.line([172, 4, 172, 14], fill=(0, 255, 255, 150), width=1)

# === MACHINE SECTION (6, 24, 164x56) ===
draw.rectangle([6, 24, 170, 80], fill=(26, 21, 53, 230))
draw.rectangle([6, 24, 170, 80], outline=(157, 78, 221, 100), width=1)

# Coins holographiques machine
draw.line([6, 24, 12, 24], fill=(157, 78, 221, 120), width=1)
draw.line([6, 24, 6, 30], fill=(157, 78, 221, 120), width=1)
draw.line([170, 24, 164, 24], fill=(157, 78, 221, 120), width=1)
draw.line([170, 24, 170, 30], fill=(157, 78, 221, 120), width=1)

# === ENERGY BAR (10, 30, 14x44) ===
# Background
draw.rectangle([10, 30, 24, 74], fill=(15, 17, 35, 255))
draw.rectangle([10, 30, 24, 74], outline=(0, 255, 255, 150), width=1)

# Ticks
draw.line([11, 41, 23, 41], fill=(255, 255, 255, 80), width=1)
draw.line([11, 52, 23, 52], fill=(255, 255, 255, 80), width=1)
draw.line([11, 63, 23, 63], fill=(255, 255, 255, 80), width=1)

# Energy fill (66% example) avec gradient cyan -> purple
fill_height = 28
for i in range(fill_height):
    progress = i / fill_height
    # Cyan -> Purple -> Magenta
    if progress < 0.5:
        r = int(0 + 91 * (progress * 2))
        g = int(255 - 222 * (progress * 2))
        b = int(255 - 73 * (progress * 2))
    else:
        p = (progress - 0.5) * 2
        r = int(91 + 66 * p)
        g = int(33 - 33 * p)
        b = int(182 + 39 * p)
    draw.line([11, 74-i, 23, 74-i], fill=(r, g, b, 255), width=1)

# === INPUT SLOT (32, 36, 18x18) ===
draw.rectangle([32, 36, 50, 54], fill=(15, 17, 35, 255))
draw.rectangle([32, 36, 50, 54], outline=(0, 255, 255, 200), width=1)
draw.rectangle([34, 38, 48, 52], outline=(0, 255, 255, 80), width=1)

# === PROGRESS ARROW (60, 42, 48x8) ===
# Background
draw.rounded_rectangle([60, 42, 108, 50], radius=4, fill=(15, 17, 35, 255))
draw.rounded_rectangle([60, 42, 108, 50], radius=4, outline=(157, 78, 221, 100), width=1)

# Empty arrow outline
draw.line([64, 46, 96, 46], fill=(91, 33, 182, 100), width=2)
draw.line([92, 43, 100, 46], fill=(91, 33, 182, 100), width=2)
draw.line([92, 49, 100, 46], fill=(91, 33, 182, 100), width=2)

# Progress fill (50% example)
draw.line([64, 46, 84, 46], fill=(0, 255, 255, 255), width=2)
draw.ellipse([82, 44, 86, 48], fill=(0, 255, 255, 255))

# === OUTPUT SLOT (118, 36, 18x18) ===
draw.rectangle([118, 36, 136, 54], fill=(15, 17, 35, 255))
draw.rectangle([118, 36, 136, 54], outline=(157, 78, 221, 200), width=1)
draw.rectangle([120, 38, 134, 52], outline=(157, 78, 221, 80), width=1)

# === INVENTORY TITLE LINE (6, 84) ===
for x in range(6, 170):
    opacity = int(100 * (1 - abs((x - 88) / 82)))
    draw.point((x, 84), fill=(157, 78, 221, opacity))

# === MAIN INVENTORY (6, 96, 164x56) ===
draw.rectangle([6, 96, 170, 152], fill=(26, 21, 53, 220))
draw.rectangle([6, 96, 170, 152], outline=(91, 33, 182, 80), width=1)

# Inventory slots 3x9
for row in range(3):
    for col in range(9):
        x = 8 + col * 18
        y = 98 + row * 18
        draw.rectangle([x, y, x+16, y+16], fill=(15, 17, 35, 255))
        draw.rectangle([x, y, x+16, y+16], outline=(91, 33, 182, 150), width=1)

# === HOTBAR LINE (6, 154) ===
for x in range(6, 170):
    opacity = int(80 * (1 - abs((x - 88) / 82)))
    draw.point((x, 154), fill=(0, 255, 255, opacity))

# Hotbar slots
for col in range(9):
    x = 8 + col * 18
    y = 156
    draw.rectangle([x, y, x+16, y+16], fill=(15, 17, 35, 255))
    draw.rectangle([x, y, x+16, y+16], outline=(0, 255, 255, 180), width=1)

# === CONFIG BUTTON (151, 6, 18x14) ===
draw.rounded_rectangle([151, 6, 169, 20], radius=2, fill=(26, 21, 53, 240))
draw.rounded_rectangle([151, 6, 169, 20], radius=2, outline=(0, 255, 255, 200), width=1)

# Gear icon
cx, cy = 160, 13
draw.ellipse([cx-3, cy-3, cx+3, cy+3], outline=(0, 255, 255, 255), width=1)
draw.ellipse([cx-1, cy-1, cx+1, cy+1], fill=(0, 255, 255, 255))
# Teeth
for angle in [0, 90, 180, 270]:
    import math
    rad = math.radians(angle)
    tx = cx + int(3 * math.cos(rad))
    ty = cy + int(3 * math.sin(rad))
    draw.ellipse([tx-0.6, ty-0.6, tx+0.6, ty+0.6], fill=(0, 255, 255, 255))

# Save
img.save(os.path.join(output_dir, "titanium_compressor.png"))
print(f"[OK] Saved titanium_compressor.png")

# === FACE CONFIG SCREEN BACKGROUND (500x400) ===
print("\nCreating FaceConfigScreen background (500x400)...")

face_bg = Image.new('RGBA', (500, 400), (10, 14, 39, 220))
draw_face = ImageDraw.Draw(face_bg)

# Gradient radial
for y in range(400):
    for x in range(500):
        dist = ((x - 250)**2 + (y - 200)**2) ** 0.5
        max_dist = ((250)**2 + (200)**2) ** 0.5
        intensity_factor = 1 - (dist / max_dist) * 0.3
        r = int(26 * intensity_factor)
        g = int(21 * intensity_factor)
        b = int(53 * intensity_factor)
        draw_face.point((x, y), fill=(r, g, b, int(220 + 20 * (1 - dist/max_dist))))

# Starfield
random.seed(123)
for _ in range(150):
    x = random.randint(0, 499)
    y = random.randint(0, 399)
    size = random.choice([1, 1, 1, 2])
    opacity = random.randint(100, 255)
    color = random.choice([(255, 255, 255), (0, 255, 255), (157, 78, 221)])
    if size == 1:
        draw_face.point((x, y), fill=(*color, opacity))
    else:
        draw_face.ellipse([x, y, x+size, y+size], fill=(*color, opacity))

# Corners holographiques
draw_face.line([10, 10, 30, 10], fill=(0, 255, 255, 80), width=1)
draw_face.line([10, 10, 10, 30], fill=(0, 255, 255, 80), width=1)
draw_face.line([490, 10, 470, 10], fill=(0, 255, 255, 80), width=1)
draw_face.line([490, 10, 490, 30], fill=(0, 255, 255, 80), width=1)
draw_face.line([10, 390, 30, 390], fill=(65, 105, 225, 80), width=1)
draw_face.line([10, 390, 10, 370], fill=(65, 105, 225, 80), width=1)
draw_face.line([490, 390, 470, 390], fill=(65, 105, 225, 80), width=1)
draw_face.line([490, 390, 490, 370], fill=(65, 105, 225, 80), width=1)

# Scan lines
draw_face.line([0, 100, 500, 100], fill=(0, 255, 255, 25), width=1)
draw_face.line([0, 200, 500, 200], fill=(65, 105, 225, 25), width=1)
draw_face.line([0, 280, 500, 280], fill=(0, 255, 255, 25), width=1)

face_bg.save(os.path.join(output_dir, "face_config_bg.png"))
print(f"[OK] Saved face_config_bg.png")

print("\n=== All textures created successfully! ===")
