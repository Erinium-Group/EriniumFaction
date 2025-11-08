import os
import shutil

src = r"D:\Mods Minecraft\EF\Erinium Faction\informations\designs\assets"
dst = r"D:\Mods Minecraft\EF\Erinium Faction\src\main\resources\assets\erinium_faction\textures\gui"

os.makedirs(dst, exist_ok=True)

files = [f for f in os.listdir(src) if f.endswith('.png')]

print(f"Copying {len(files)} PNG files...")

for file in files:
    # Remplacer les - par des _
    new_name = file.replace('-', '_')
    src_path = os.path.join(src, file)
    dst_path = os.path.join(dst, new_name)

    shutil.copy2(src_path, dst_path)
    print(f"  {file} -> {new_name}")

print(f"\n[OK] All {len(files)} PNG assets copied successfully!")
