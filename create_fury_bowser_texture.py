#!/usr/bin/env python3
"""Generate the 1024x2816 UV atlas used by the redesigned FuryBowserModel.

The model allocates one 256x128 atlas tile to each cuboid (UV tile index 0..84).
Run from anywhere; by default it writes directly into the mod resources folder.
"""
from pathlib import Path
import argparse
import random
from PIL import Image, ImageDraw

parser = argparse.ArgumentParser(description="Generate the Fury Bowser UV texture atlas.")
parser.add_argument("--output", type=Path, default=Path(__file__).resolve().with_name("fury_bowser.png"),
                    help="PNG output path (default: fury_bowser.png beside this script)")
OUT = parser.parse_args().output
TILE_W, TILE_H = 256, 128
COLS, TILE_COUNT = 4, 85
ATLAS = Image.new('RGBA', (COLS * TILE_W, ((TILE_COUNT + COLS - 1) // COLS) * TILE_H), (12, 9, 18, 255))

# Tile order mirrors the texOffs tile numbers in FuryBowserModel.java.
MATERIALS = (
    ['body'] * 9 +                 # 0-8 torso plates
    ['armor'] * 16 +               # 9-24 arms and spikes
    ['shell'] * 4 + ['spike'] * 16 + # 25-44 shell and shell spikes
    ['face'] * 6 + ['teeth'] * 2 + ['socket'] * 2 + ['eye'] * 2 +
    ['horn'] * 8 +                 # 57-64 horns and cheeks
    ['fire_red'] * 4 + ['fire_orange'] * 4 + ['fire_yellow'] * 4 + # 65-76 mane
    ['body'] * 8                   # 77-84 legs
)
assert len(MATERIALS) == TILE_COUNT, len(MATERIALS)

PALETTES = {
    'body': ((10, 9, 19), (34, 18, 43), (78, 20, 40)),
    'armor': ((5, 8, 16), (22, 24, 39), (67, 26, 49)),
    'shell': ((8, 12, 23), (27, 34, 54), (77, 35, 59)),
    'spike': ((65, 70, 88), (151, 157, 174), (230, 213, 185)),
    'face': ((9, 8, 16), (31, 17, 31), (91, 26, 43)),
    'teeth': ((105, 78, 65), (216, 194, 158), (255, 242, 207)),
    'socket': ((1, 2, 8), (9, 8, 18), (32, 10, 20)),
    'eye': ((255, 125, 28), (255, 224, 119), (255, 255, 228)),
    'horn': ((5, 8, 17), (36, 39, 56), (117, 48, 61)),
    'fire_red': ((105, 4, 20), (224, 10, 32), (255, 67, 28)),
    'fire_orange': ((190, 23, 8), (255, 91, 10), (255, 176, 24)),
    'fire_yellow': ((255, 93, 5), (255, 190, 22), (255, 246, 117)),
}

rng = random.Random(42021)

def mix(a, b, t):
    return tuple(int(a[i] * (1-t) + b[i] * t) for i in range(3))

for tile, material in enumerate(MATERIALS):
    ox, oy = (tile % COLS) * TILE_W, (tile // COLS) * TILE_H
    dark, mid, light = PALETTES[material]
    tile_img = Image.new('RGBA', (TILE_W, TILE_H))
    px = tile_img.load()
    for y in range(TILE_H):
        vertical = y / (TILE_H - 1)
        base = mix(dark, mid, min(1.0, vertical * 1.55))
        base = mix(base, light, max(0.0, (vertical - 0.64) * 1.9))
        for x in range(TILE_W):
            grain = rng.randint(-7, 7)
            edge = 1.0 - 0.16 * abs(x - TILE_W/2) / (TILE_W/2)
            color = tuple(max(0, min(255, int(c * edge) + grain)) for c in base)
            px[x, y] = (*color, 255)

    d = ImageDraw.Draw(tile_img, 'RGBA')
    if material in ('body', 'armor', 'face', 'horn'):
        # Fine reptilian scales plus flowing purple/crimson fissures.
        for yy in range(5, TILE_H, 13):
            for xx in range((tile % 2) * 6, TILE_W, 18):
                c = (115, 47, 104, 65) if (xx // 18 + yy // 13) % 3 == 0 else (0, 0, 0, 65)
                d.arc((xx, yy, xx+14, yy+11), 190, 345, fill=c, width=2)
        for k in range(3):
            x0 = rng.randint(8, TILE_W-8)
            points = [(x0, 0), (x0+rng.randint(-12,12), 35),
                      (x0+rng.randint(-18,18), 69), (x0+rng.randint(-12,12), 127)]
            d.line(points, fill=(186, 25, 65, 95), width=2)
    elif material == 'shell':
        for yy in range(12, TILE_H, 18):
            d.arc((5, yy-10, TILE_W-5, yy+17), 190, 350, fill=(119, 137, 166, 95), width=3)
        d.line((8, 12, TILE_W-8, 12), fill=(2, 3, 10, 180), width=5)
    elif material == 'spike':
        d.line((18, 15, TILE_W-18, 15), fill=(255, 245, 215, 90), width=3)
        d.polygon([(128, 5), (145, 63), (128, 120), (111, 63)], fill=(225, 222, 213, 28))
    elif material == 'teeth':
        for x in range(18, TILE_W, 34):
            d.line((x, 8, x+5, TILE_H-8), fill=(255, 248, 220, 150), width=3)
    elif material == 'eye':
        d.ellipse((24, 25, 232, 103), fill=(255, 189, 53, 90), outline=(255, 255, 226, 230), width=5)
        d.ellipse((91, 33, 165, 97), fill=(255, 255, 237, 220))
    elif material.startswith('fire_'):
        # Long flame streaks make the texture read as a luminous mane from every UV face.
        for x in range(12, TILE_W, 29):
            top = rng.randint(2, 32)
            d.polygon([(x, TILE_H), (x+rng.randint(-8,8), top),
                       (x+13, top+25), (x+20, TILE_H)], fill=(255, 220, 82, 80))
        d.line((0, TILE_H-7, TILE_W, TILE_H-7), fill=(255, 246, 130, 150), width=5)

    ATLAS.alpha_composite(tile_img, (ox, oy))

OUT.parent.mkdir(parents=True, exist_ok=True)
ATLAS.save(OUT, optimize=True)
print(f'Created {OUT} ({ATLAS.width}x{ATLAS.height}, {OUT.stat().st_size:,} bytes)')
