#!/usr/bin/env python3
"""
Imports the Jokello Jewels art pack into the fixed resource names used by ui/Assets.kt.

    python3 tools/import_art.py "/path/to/Jokello Jewels art"      (requires Pillow + numpy)

Every source image is trimmed to its visible (alpha) bounds, resized for phone densities and
compressed. Re-run after replacing any source file; no code change is needed.
"""
import os
import sys

import numpy as np
from PIL import Image, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")
NODPI = os.path.join(RES, "drawable-nodpi")
STORE = os.path.join(ROOT, "store")

# source file (without extension variations) -> (resource name, max long side px)
MAP = {
    "logo.png": ("logo", 900),
    "Приветливый шут в пурпуре и золоте-8.png": ("mascot_a", 720),
    "Роскошный шут с изумрудом-3.png": ("mascot_b", 720),
    "Роскошный фиолетовый шут в поклоне-6.png": ("mascot_c", 720),
    "Пустая фиолетовая кнопка в золотой окантовке-1.png": ("plate_round", 400),
    "Фиолетовая кнопка назад с золотой стрелкой-2.png": ("icon_back", 192),
    "Роскошная фиолетовая кнопка паузы-7.png": ("icon_pause", 192),
    "Роскошная пурпурно-золотая игровая панель-5.png": ("panel_question", 1100),
    # 12 match icons
    "Рубиновый ромб в золотой оправе-4.png": ("item_01", 360),
    "Изумрудный самоцвет в золотой оправе-5.png": ("item_02", 360),
    "Роскошный аметист в золотой оправе-3.png": ("item_03", 360),
    "Пурпурная звёздная брошь с рубином-5.png": ("item_04", 360),
    "Роскошный золотой колокольчик шута-6.png": ("item_05", 360),
    "Роскошный кубок шута с изумрудом-9.png": ("item_06", 360),
    "Роскошный пурпурный подарок с алым бантом-9.png": ("item_07", 360),
    "Роскошный сундук с драгоценными камнями-7.png": ("item_08", 360),
    "Золотая лира с рубиновым сердцем-8.png": ("item_09", 360),
    "Королевский золотой ключ с рубиновым центром-2.png": ("item_10", 360),
    "Пурпурно-золотая колба с изумрудным зельем-4.png": ("item_11", 360),
    "Королевский веер из бархата и золота-6.png": ("item_12", 360),
    # decorations
    "Роскошный шутовской колпак с рубиновым брошем-2.png": ("deco_hat", 480),
    "Роскошная маска арлекина с самоцветами-1.png": ("deco_mask", 360),
    "Роскошная фиолетовая книга с рубином-7.png": ("deco_book", 300),
    "Роскошные песочные часы в пурпурно-золотой оправе-1.png": ("deco_hourglass", 200),
    "Золотой жезл шута с самоцветами-8.png": ("deco_scepter", 300),
    "Пурпурный театральный билет с рубином-3.png": ("deco_ticket", 300),
}
PLATE = "Пурпурная золотая табличка джокера-4.png"
BACKGROUND = "background.jpg"
ICON = "icon.jpg"


def trim(img, threshold=24, pad=4):
    a = np.asarray(img.split()[3])
    ys, xs = np.where(a > threshold)
    if len(xs) == 0:
        return img
    box = (max(0, xs.min() - pad), max(0, ys.min() - pad),
           min(img.width, xs.max() + 1 + pad), min(img.height, ys.max() + 1 + pad))
    return img.crop(box)


def fit(img, long_side):
    w, h = img.size
    k = long_side / max(w, h)
    if k < 1:
        img = img.resize((max(1, round(w * k)), max(1, round(h * k))), Image.LANCZOS)
    return img


def save_png(img, name, folder=NODPI):
    img = img.convert("RGBA")
    # 256-color palette with alpha + dithering: visually lossless for this art, ~4-6x smaller.
    q = img.quantize(colors=256, method=Image.Quantize.FASTOCTREE, dither=Image.Dither.FLOYDSTEINBERG)
    q.save(os.path.join(folder, name + ".png"), optimize=True)
    return img.size


def three_slice(img, cap_w, target_aspect):
    """Stretch only the middle of a horizontal plate so rounded ends keep their shape."""
    w, h = img.size
    target_w = round(h * target_aspect)
    left = img.crop((0, 0, cap_w, h))
    right = img.crop((w - cap_w, 0, w, h))
    mid = img.crop((cap_w, 0, w - cap_w, h)).resize((target_w - 2 * cap_w, h), Image.LANCZOS)
    out = Image.new("RGBA", (target_w, h), (0, 0, 0, 0))
    out.paste(left, (0, 0))
    out.paste(mid, (cap_w, 0))
    out.paste(right, (target_w - cap_w, 0))
    return out


def clean_stage(img, y0=170, y1=870):
    a = np.asarray(img).astype(float)
    top, bot = a[y0].copy(), a[y1].copy()
    for y in range(y0, y1):
        t = (y - y0) / (y1 - y0)
        a[y] = top * (1 - t) + bot * t
    return Image.fromarray(a.astype("uint8")).filter(ImageFilter.GaussianBlur(4))


def pill_plate(img):
    from PIL import ImageDraw
    w, h = img.size
    ss = 4
    mask = Image.new("L", (w * ss, h * ss), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, w * ss - 1, h * ss - 1], radius=h * ss * 0.5, fill=255)
    out = img.copy()
    out.putalpha(mask.resize((w, h), Image.LANCZOS))
    rim = Image.new("RGBA", (w * ss, h * ss), (0, 0, 0, 0))
    d = ImageDraw.Draw(rim)
    t = int(h * ss * 0.075)
    d.rounded_rectangle([t // 2, t // 2, w * ss - t // 2, h * ss - t // 2], radius=h * ss * 0.5 - t // 2,
                        outline=(176, 112, 24, 255), width=t)
    d.rounded_rectangle([t, t, w * ss - t, h * ss - t], radius=h * ss * 0.5 - t,
                        outline=(247, 200, 90, 255), width=int(t * 0.55))
    rim = rim.resize((w, h), Image.LANCZOS)
    out.alpha_composite(rim)
    return out


def main(src):
    os.makedirs(NODPI, exist_ok=True)
    report = {}
    for file, (name, size) in MAP.items():
        img = Image.open(os.path.join(src, file)).convert("RGBA")
        report[name] = save_png(fit(trim(img), size), name)

    # Ornate menu plate (jester-scroll ends) — trimmed, aspect written to Assets.BTN_MENU_ASPECT.
    plate = trim(Image.open(os.path.join(src, PLATE)).convert("RGBA"))
    menu = fit(plate, 1200)
    report["btn_menu"] = save_png(menu, "btn_menu")

    # Simple plate: the straight gold-rimmed middle of the same plate, stretched to aspect 5.2,
    # with pill-shaped ends re-drawn (gold rim) so answer buttons stay slim and clean.
    pw, ph = plate.size
    mid = plate.crop((int(pw * 0.35), int(ph * 0.05), int(pw * 0.65), int(ph * 0.93)))
    mid = trim(mid)
    h = 300
    w = round(h * 5.2)
    mid = mid.resize((w, h), Image.LANCZOS)
    report["btn_plate"] = save_png(pill_plate(mid), "btn_plate")

    # Background: the curtain stage with the baked-in logo removed (each column is interpolated
    # between rows above and below the logo, keeping the vertical curtain folds), stretched to 9:16.
    stage = clean_stage(Image.open(os.path.join(src, BACKGROUND)).convert("RGB"))
    bg = stage.resize((1080, 1920), Image.LANCZOS).filter(ImageFilter.GaussianBlur(3))
    bg.save(os.path.join(NODPI, "bg_main.jpg"), quality=82, optimize=True, progressive=True)
    report["bg_main"] = bg.size
    avg = tuple(int(v) for v in np.asarray(bg.resize((1, 1))).reshape(3))
    print("bg average color:", "#%02X%02X%02X" % avg)

    # Launcher: adaptive background = blurred curtain, foreground = logo inside the safe zone.
    icon = Image.open(os.path.join(src, ICON)).convert("RGB")
    icon_bg = stage.resize((432, 432), Image.LANCZOS)
    icon_bg.save(os.path.join(NODPI, "ic_launcher_bg.jpg"), quality=88, optimize=True)
    logo = trim(Image.open(os.path.join(src, "logo.png")).convert("RGBA"))
    fg = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
    lg = logo.copy()
    lg.thumbnail((288, 288), Image.LANCZOS)  # ≈72dp of 108dp: inside the visible mask area
    fg.paste(lg, ((432 - lg.width) // 2, (432 - lg.height) // 2), lg)
    save_png(fg, "ic_launcher_fg")

    # Splash icon: logo centred in a square, inside the 2/3 circle the splash API shows.
    sp = Image.new("RGBA", (576, 576), (0, 0, 0, 0))
    lg2 = logo.copy()
    lg2.thumbnail((380, 380), Image.LANCZOS)
    sp.paste(lg2, ((576 - lg2.width) // 2, (576 - lg2.height) // 2), lg2)
    save_png(sp, "ic_splash_logo")

    # Legacy launcher PNGs (API 24-25) + Play Store 512 icon, straight from icon.jpg.
    sizes = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    for dpi, px in sizes.items():
        folder = os.path.join(RES, "mipmap-" + dpi)
        os.makedirs(folder, exist_ok=True)
        sq = icon.resize((px, px), Image.LANCZOS).convert("RGBA")
        mask = Image.new("L", (px * 4, px * 4), 0)
        from PIL import ImageDraw
        ImageDraw.Draw(mask).rounded_rectangle([0, 0, px * 4 - 1, px * 4 - 1], radius=px * 4 * 0.18, fill=255)
        sq.putalpha(mask.resize((px, px), Image.LANCZOS))
        sq.save(os.path.join(folder, "ic_launcher.png"), optimize=True)
        rnd = Image.new("RGBA", (px, px), (0, 0, 0, 0))
        composed = icon_bg.resize((px, px), Image.LANCZOS).convert("RGBA")
        lgs = logo.copy()
        lgs.thumbnail((int(px * 0.84), int(px * 0.84)), Image.LANCZOS)
        composed.paste(lgs, ((px - lgs.width) // 2, (px - lgs.height) // 2), lgs)
        cmask = Image.new("L", (px * 4, px * 4), 0)
        ImageDraw.Draw(cmask).ellipse([0, 0, px * 4 - 1, px * 4 - 1], fill=255)
        rnd.paste(composed, (0, 0), cmask.resize((px, px), Image.LANCZOS))
        rnd.save(os.path.join(folder, "ic_launcher_round.png"), optimize=True)
    os.makedirs(STORE, exist_ok=True)
    icon.resize((512, 512), Image.LANCZOS).save(os.path.join(STORE, "play_store_icon_512.png"), optimize=True)

    for k, v in sorted(report.items()):
        print(f"{k:16s} {v[0]}x{v[1]}  aspect {v[0] / v[1]:.3f}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, "art"))
