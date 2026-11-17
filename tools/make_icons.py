"""Erzeugt App-Icon (Legacy, Adaptive, Monochrom) und Splash-Logo aus dem Original-Logo.

Aufruf:  python tools/make_icons.py
Ergebnis: app/src/main/res/mipmap-*/*.png, drawable*/ic_launcher_*.png, drawable/splash_logo.png
"""
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
SOURCE = RES / "drawable" / "mcaffen_logo.png"

SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def rounded_mask(size: int, radius_ratio: float) -> Image.Image:
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    radius = int(size * radius_ratio)
    draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=radius, fill=255)
    return mask


def circular_mask(size: int) -> Image.Image:
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size - 1, size - 1), fill=255)
    return mask


def square_crop(img: Image.Image, side_ratio: float) -> Image.Image:
    """Schneidet das Logo so zu, dass das Affen-Gesicht mittig bleibt."""
    w, h = img.size
    side = int(min(w, h) * side_ratio)
    left = (w - side) // 2
    top = int((h - side) * 0.32)
    return img.crop((left, top, left + side, top + side))


def main() -> None:
    logo = Image.open(SOURCE).convert("RGB")

    # --- Legacy-Icon (quadratisch, abgerundet) -----------------------------
    for folder, size in SIZES.items():
        out = RES / folder
        out.mkdir(parents=True, exist_ok=True)
        icon = square_crop(logo, 0.98).resize((size, size), Image.LANCZOS)
        icon = icon.convert("RGBA")
        icon.putalpha(rounded_mask(size, 0.22))
        icon.save(out / "ic_launcher.png")
        round_icon = square_crop(logo, 0.98).resize((size, size), Image.LANCZOS)
        round_icon = round_icon.convert("RGBA")
        round_icon.putalpha(circular_mask(size))
        round_icon.save(out / "ic_launcher_round.png")

    # --- Adaptive Icon: Hintergrund + Vordergrund --------------------------
    for folder, size in SIZES.items():
        out = RES / folder
        adaptive = int(size * 108 / 48)  # 108dp Raster
        # Hintergrund: weicher Verlauf aus dem Logo-Umfeld
        bg = Image.new("RGB", (adaptive, adaptive), (12, 58, 24))
        top_color = logo.getpixel((10, 10))
        bottom_color = logo.getpixel((logo.width // 2, logo.height - 5))
        for y in range(adaptive):
            t = y / max(1, adaptive - 1)
            row = tuple(
                int(top_color[i] * (1 - t) + bottom_color[i] * t) for i in range(3)
            )
            ImageDraw.Draw(bg).line([(0, y), (adaptive, y)], fill=row)
        bg.save(out / "ic_launcher_background.png")

        # Vordergrund: Affe im inneren 66%-Sicherheitsbereich
        inner = int(adaptive * 0.66)
        fg = Image.new("RGBA", (adaptive, adaptive), (0, 0, 0, 0))
        monkey = square_crop(logo, 0.9).resize((inner, inner), Image.LANCZOS)
        monkey = monkey.convert("RGBA")
        monkey.putalpha(circular_mask(inner))
        fg.paste(monkey, ((adaptive - inner) // 2, (adaptive - inner) // 2), monkey)
        fg.save(out / "ic_launcher_foreground.png")

    # --- Monochromes Icon (Themed Icons, Android 13+) ----------------------
    for folder, size in SIZES.items():
        out = RES / folder
        adaptive = int(size * 108 / 48)
        inner = int(adaptive * 0.6)
        mono = Image.new("RGBA", (adaptive, adaptive), (0, 0, 0, 0))
        silhouette = Image.new("L", (inner, inner), 0)
        draw = ImageDraw.Draw(silhouette)
        draw.ellipse((0, 0, inner - 1, inner - 1), fill=255)
        # Gesichts-Silhouette: Augen und Mund als Aussparungen
        draw.ellipse(
            (int(inner * 0.26), int(inner * 0.34), int(inner * 0.40), int(inner * 0.48)),
            fill=0,
        )
        draw.ellipse(
            (int(inner * 0.60), int(inner * 0.34), int(inner * 0.74), int(inner * 0.48)),
            fill=0,
        )
        draw.arc(
            (int(inner * 0.28), int(inner * 0.50), int(inner * 0.72), int(inner * 0.82)),
            start=15,
            end=165,
            fill=0,
            width=max(2, inner // 14),
        )
        layer = Image.new("RGBA", (inner, inner), (255, 255, 255, 0))
        layer.putalpha(silhouette)
        mono.paste(layer, ((adaptive - inner) // 2, (adaptive - inner) // 2), layer)
        mono.save(out / "ic_launcher_monochrome.png")

    # --- Splash-Logo (rund, transparent) ----------------------------------
    for folder, size in {
        "drawable-mdpi": 96,
        "drawable-hdpi": 144,
        "drawable-xhdpi": 192,
        "drawable-xxhdpi": 288,
        "drawable-xxxhdpi": 384,
    }.items():
        out = RES / folder
        out.mkdir(parents=True, exist_ok=True)
        splash = square_crop(logo, 0.96).resize((size, size), Image.LANCZOS).convert("RGBA")
        splash.putalpha(circular_mask(size))
        splash.save(out / "splash_logo.png")

    # --- Kompaktes Logo für die App-Oberfläche (statt 2,1 MB PNG) ---------
    compact = square_crop(logo, 0.92).resize((256, 256), Image.LANCZOS)
    compact.save(RES / "drawable-xxhdpi" / "mcaffen_logo.png", optimize=True)
    compact.resize((128, 128), Image.LANCZOS).save(
        RES / "drawable-mdpi" / "mcaffen_logo.png", optimize=True
    )
    compact.resize((192, 192), Image.LANCZOS).save(
        RES / "drawable-xhdpi" / "mcaffen_logo.png", optimize=True
    )
    compact.resize((384, 384), Image.LANCZOS).save(
        RES / "drawable-xxxhdpi" / "mcaffen_logo.png", optimize=True
    )

    print("Icons erzeugt.")


if __name__ == "__main__":
    main()
