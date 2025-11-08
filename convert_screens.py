from PIL import Image
import os

# Chemins
assets_dir = r"D:\Mods Minecraft\EF\Erinium Faction\informations\designs\assets"
output_dir = r"D:\Mods Minecraft\EF\Erinium Faction\src\main\resources\assets\erinium_faction\textures\gui"

os.makedirs(output_dir, exist_ok=True)

# === TITANIUM COMPRESSOR SCREEN (176x166) ===
print("Creating TitaniumCompressorScreen.png (176x166)...")

# Créer l'image de base
compressor = Image.new('RGBA', (176, 166), (0, 0, 0, 0))

# Background starfield + gradient
bg_star = Image.open(os.path.join(assets_dir, "bg-starfield.png")).convert('RGBA')
bg_grad = Image.open(os.path.join(assets_dir, "bg-gradient-dark.png")).convert('RGBA')

# Tiler le starfield
for y in range(0, 166, bg_star.height):
    for x in range(0, 176, bg_star.width):
        compressor.paste(bg_star, (x, y), bg_star)

# Overlay gradient avec opacité
bg_grad = bg_grad.resize((176, 166))
compressor = Image.alpha_composite(compressor, bg_grad)

# Border glow cyan (simulé avec un rectangle fin)
from PIL import ImageDraw
draw = ImageDraw.Draw(compressor)
draw.rectangle([1, 1, 174, 164], outline=(0, 255, 255, 150), width=1)

# Title section (4, 4, 168, 16)
panel = Image.open(os.path.join(assets_dir, "panel-main.png")).convert('RGBA')
panel_title = panel.resize((168, 16))
compressor.paste(panel_title, (4, 4), panel_title)

# Glow line cyan sous le titre
glow_cyan = Image.open(os.path.join(assets_dir, "glow-line-cyan.png")).convert('RGBA')
glow_cyan_scaled = glow_cyan.resize((168, 1))
compressor.paste(glow_cyan_scaled, (4, 20), glow_cyan_scaled)

# Machine section panel (6, 24, 164, 56)
panel_machine = panel.resize((164, 56))
compressor.paste(panel_machine, (6, 24), panel_machine)

# Energy bar (10, 30, 14, 44)
energy = Image.open(os.path.join(assets_dir, "energy-bar.png")).convert('RGBA')
compressor.paste(energy, (10, 30), energy)

# Input slot (32, 36, 18, 18)
slot_input = Image.open(os.path.join(assets_dir, "slot-input.png")).convert('RGBA')
compressor.paste(slot_input, (32, 36), slot_input)

# Progress arrow (60, 42, 48, 8)
arrow_bg = Image.open(os.path.join(assets_dir, "progress-arrow-bg.png")).convert('RGBA')
compressor.paste(arrow_bg, (60, 42), arrow_bg)
arrow_empty = Image.open(os.path.join(assets_dir, "progress-arrow-empty.png")).convert('RGBA')
compressor.paste(arrow_empty, (60, 42), arrow_empty)

# Output slot (118, 36, 18, 18)
slot_output = Image.open(os.path.join(assets_dir, "slot-output.png")).convert('RGBA')
compressor.paste(slot_output, (118, 36), slot_output)

# Corner accents sur la machine section
corner = Image.open(os.path.join(assets_dir, "corner-accent.png")).convert('RGBA')
corner_small = corner.resize((10, 10))
compressor.paste(corner_small, (6, 24), corner_small)
# Top right corner (flip horizontal)
corner_tr = corner_small.transpose(Image.FLIP_LEFT_RIGHT)
compressor.paste(corner_tr, (160, 24), corner_tr)

# Inventory title glow line (6, 84, 164)
glow_purple = Image.open(os.path.join(assets_dir, "glow-line-purple.png")).convert('RGBA')
glow_purple_scaled = glow_purple.resize((164, 1))
compressor.paste(glow_purple_scaled, (6, 84), glow_purple_scaled)

# Main inventory panel (6, 96, 164, 56)
panel_inv = panel.resize((164, 56))
compressor.paste(panel_inv, (6, 96), panel_inv)

# Inventory slots 3x9 (8, 98)
slot_inv = Image.open(os.path.join(assets_dir, "slot-inventory.png")).convert('RGBA')
for row in range(3):
    for col in range(9):
        x = 8 + col * 18
        y = 98 + row * 18
        compressor.paste(slot_inv, (x, y), slot_inv)

# Hotbar separator line (6, 154)
compressor.paste(glow_cyan_scaled, (6, 154), glow_cyan_scaled)

# Hotbar slots (8, 156)
slot_hotbar = Image.open(os.path.join(assets_dir, "slot-hotbar.png")).convert('RGBA')
for col in range(9):
    x = 8 + col * 18
    compressor.paste(slot_hotbar, (x, 156), slot_hotbar)

# Config button (151, 6, 18, 14)
btn_config = Image.open(os.path.join(assets_dir, "button-config.png")).convert('RGBA')
compressor.paste(btn_config, (151, 6), btn_config)

# Sauvegarder
compressor.save(os.path.join(output_dir, "titanium_compressor.png"))
print(f"[OK] Saved to {output_dir}/titanium_compressor.png")

print("\nDone! PNG screens created successfully.")
