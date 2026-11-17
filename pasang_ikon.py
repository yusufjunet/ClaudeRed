import os
from PIL import Image, ImageDraw

src = Image.open("../icon-only.png").convert("RGB")
sizes = {"mdpi": (48, 108), "hdpi": (72, 162), "xhdpi": (96, 216),
         "xxhdpi": (144, 324), "xxxhdpi": (192, 432)}
for d, (leg, fg) in sizes.items():
    p = f"android/app/src/main/res/mipmap-{d}"
    os.makedirs(p, exist_ok=True)
    sq = src.resize((leg, leg), Image.LANCZOS)
    sq.save(f"{p}/ic_launcher.png")
    m = Image.new("L", (leg * 4, leg * 4), 0)
    ImageDraw.Draw(m).ellipse((0, 0, leg * 4 - 1, leg * 4 - 1), fill=255)
    m = m.resize((leg, leg), Image.LANCZOS)
    r = Image.new("RGBA", (leg, leg), (0, 0, 0, 0))
    r.paste(sq, (0, 0), m)
    r.save(f"{p}/ic_launcher_round.png")
    src.resize((fg, fg), Image.LANCZOS).save(f"{p}/ic_launcher_foreground.png")
print("ikon terpasang")
