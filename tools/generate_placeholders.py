#!/usr/bin/env python3
"""
Generates the synthesized SFX (res/raw/*.wav). With --art it also rebuilds simple fallback
placeholder art (overwrites the imported art pack — normally use tools/import_art.py instead).

    python3 tools/generate_placeholders.py [--art]      (requires Pillow: pip install pillow)

Outputs (fixed names wired through ui/Assets.kt — replace with real art any time):
  res/drawable-nodpi: bg_main.jpg logo.png mascot_a.png mascot_b.png btn_menu.png
                      btn_plate.png plate_round.png icon_back.png icon_pause.png item_01..12.png
  res/mipmap-*dpi:    ic_launcher.png ic_launcher_round.png  (legacy icons, API 24-25)
  res/raw:            sfx_click/correct/wrong/match/win/lose.wav
"""
import math
import os
import random
import struct
import wave

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")
NODPI = os.path.join(RES, "drawable-nodpi")
RAW = os.path.join(RES, "raw")

NIGHT = (20, 6, 33)
PLUM = (42, 14, 61)
VELVET = (75, 26, 107)
MAGENTA = (214, 36, 110)
MAGENTA_DK = (156, 21, 80)
TEAL = (25, 195, 180)
TEAL_LT = (127, 240, 227)
TEAL_DK = (14, 143, 132)
GOLD = (245, 184, 61)
GOLD_DK = (178, 120, 24)
CREAM = (255, 244, 224)
WHITE = (255, 255, 255)

SS = 4  # supersampling factor for smooth edges


def font(size, serif=True):
    candidates = [
        "/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf" if serif else
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        "/Library/Fonts/Georgia Bold.ttf",
        "C:/Windows/Fonts/georgiab.ttf",
    ]
    for c in candidates:
        if os.path.exists(c):
            return ImageFont.truetype(c, size)
    return ImageFont.load_default(size)


class BlendDraw:
    """ImageDraw proxy that alpha-BLENDS every primitive onto an RGBA image
    (plain ImageDraw overwrites pixels, so translucent highlights would punch holes)."""

    def __init__(self, img):
        self.img = img

    def __getattr__(self, name):
        def call(*args, **kwargs):
            layer = Image.new("RGBA", self.img.size, (0, 0, 0, 0))
            result = getattr(ImageDraw.Draw(layer), name)(*args, **kwargs)
            if name != "textlength":
                self.img.alpha_composite(layer)
            return result
        return call


def canvas(w, h, bg=(0, 0, 0, 0)):
    img = Image.new("RGBA", (w * SS, h * SS), bg)
    return img, BlendDraw(img)


def finish(img, w, h):
    return img.resize((w, h), Image.LANCZOS)


def s(v):
    return int(v * SS)


def pts(points):
    return [(s(x), s(y)) for x, y in points]


def save_png(img, name):
    path = os.path.join(NODPI, name + ".png")
    img = img.convert("RGBA")
    # Palette quantization keeps PNGs small while preserving alpha.
    q = img.quantize(colors=256, method=Image.FASTOCTREE, dither=Image.Dither.FLOYDSTEINBERG)
    q.save(path, optimize=True)


def vgrad(w, h, top, bottom):
    g = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(g)
    for y in range(h):
        t = y / max(1, h - 1)
        c = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        d.line([(0, y), (w, y)], fill=c + (255,))
    return g


# ---------------------------------------------------------------- background
def make_bg():
    w, h = 1080, 1920
    img = vgrad(w, h, NIGHT, VELVET).convert("RGB")
    over = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(over)
    # harlequin lattice
    step = 135
    for row in range(-1, h // step + 2):
        for col in range(-1, w // step + 2):
            cx = col * step + (step // 2 if row % 2 else 0)
            cy = row * step
            color = (MAGENTA + (34,)) if (row + col) % 2 else (TEAL + (22,))
            d.polygon([(cx, cy - step // 2), (cx + step // 2, cy), (cx, cy + step // 2), (cx - step // 2, cy)], fill=color)
    # curtain swags along the top
    for i in range(6):
        x0 = i * 180 - 20
        d.ellipse([x0, -140, x0 + 220, 120], fill=MAGENTA_DK + (210,))
        d.ellipse([x0 + 10, -150, x0 + 210, 95], fill=MAGENTA + (230,))
    d.rectangle([0, 0, w, 40], fill=GOLD_DK + (255,))
    # stage glow at the bottom
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse([-200, h - 520, w + 200, h + 300], fill=GOLD + (60,))
    glow = glow.filter(ImageFilter.GaussianBlur(80))
    img = Image.alpha_composite(img.convert("RGBA"), over)
    img = Image.alpha_composite(img, glow)
    # sparkles
    d = ImageDraw.Draw(img, "RGBA")
    rnd = random.Random(7)
    for _ in range(140):
        x, y = rnd.randint(0, w), rnd.randint(60, h)
        r = rnd.choice([1, 1, 2, 2, 3])
        a = rnd.randint(90, 220)
        d.ellipse([x - r, y - r, x + r, y + r], fill=CREAM + (a,))
        if r == 3:
            d.line([(x - 9, y), (x + 9, y)], fill=CREAM + (a,), width=1)
            d.line([(x, y - 9), (x, y + 9)], fill=CREAM + (a,), width=1)
    img.convert("RGB").save(os.path.join(NODPI, "bg_main.jpg"), quality=80, optimize=True, progressive=True)


# ---------------------------------------------------------------- gems
def facet_gem(d, cx, cy, r, base, light, dark):
    """Brilliant-cut style gem seen from the side."""
    top = cy - r * 0.55
    gird = cy - r * 0.1
    bot = cy + r
    d.polygon(pts([(cx - r, gird), (cx - r * 0.55, top), (cx + r * 0.55, top), (cx + r, gird), (cx, bot)]), fill=base)
    d.polygon(pts([(cx - r * 0.55, top), (cx, top), (cx - r * 0.3, gird), (cx - r, gird)]), fill=light)
    d.polygon(pts([(cx + r * 0.2, gird), (cx + r, gird), (cx, bot)]), fill=dark)
    line = CREAM + (200,)
    d.line(pts([(cx - r, gird), (cx + r, gird)]), fill=line, width=s(2))
    d.line(pts([(cx - r * 0.55, top), (cx - r * 0.3, gird), (cx, top), (cx + r * 0.3, gird), (cx + r * 0.55, top)]), fill=line, width=s(2))
    d.line(pts([(cx - r * 0.3, gird), (cx, bot), (cx + r * 0.3, gird)]), fill=line, width=s(2))


def regular_polygon(cx, cy, r, n, rot=0.0):
    return [(cx + r * math.cos(rot + 2 * math.pi * i / n), cy + r * math.sin(rot + 2 * math.pi * i / n)) for i in range(n)]


def star(cx, cy, r1, r2, n=5, rot=-math.pi / 2):
    out = []
    for i in range(n * 2):
        r = r1 if i % 2 == 0 else r2
        a = rot + math.pi * i / n
        out.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return out


def shine(d, x, y, r):
    d.ellipse(pts([(x - r, y - r), (x + r, y + r)]), fill=WHITE + (210,))


def outline_poly(d, p, fill, edge=None, width=4):
    d.polygon(pts(p), fill=fill)
    d.line(pts(p + [p[0]]), fill=(edge or (255, 255, 255, 180)), width=s(width))


def make_items():
    size = 256
    c = size / 2
    makers = []

    def ruby_heart(d):
        red, lt, dk = (232, 56, 79), (255, 140, 150), (150, 20, 40)
        d.ellipse(pts([(c - 92, c - 80), (c + 4, c + 16)]), fill=red)
        d.ellipse(pts([(c - 4, c - 80), (c + 92, c + 16)]), fill=red)
        d.polygon(pts([(c - 90, c - 20), (c + 90, c - 20), (c, c + 100)]), fill=red)
        d.polygon(pts([(c, c - 40), (c + 90, c - 20), (c, c + 100)]), fill=dk)
        shine(d, c - 50, c - 45, 14)
        d.ellipse(pts([(c - 70, c - 66), (c - 40, c - 50)]), fill=lt)

    def sapphire_drop(d):
        blue, lt, dk = (40, 90, 220), (140, 180, 255), (20, 40, 130)
        d.ellipse(pts([(c - 70, c - 10), (c + 70, c + 110)]), fill=blue)
        d.polygon(pts([(c, c - 110), (c - 66, c + 30), (c + 66, c + 30)]), fill=blue)
        d.polygon(pts([(c, c - 110), (c + 66, c + 30), (c + 40, c + 100), (c, c + 50)]), fill=dk)
        d.polygon(pts([(c, c - 80), (c - 30, c + 20), (c, c + 40)]), fill=lt)
        shine(d, c - 28, c + 40, 10)

    def emerald_square(d):
        g, lt, dk = (30, 170, 90), (130, 230, 160), (10, 100, 50)
        outline_poly(d, [(c - 80, c - 100), (c + 80, c - 100), (c + 100, c - 80), (c + 100, c + 80), (c + 80, c + 100),
                         (c - 80, c + 100), (c - 100, c + 80), (c - 100, c - 80)], g)
        d.rectangle(pts([(c - 55, c - 55), (c + 55, c + 55)]), fill=lt)
        d.rectangle(pts([(c - 35, c - 35), (c + 35, c + 35)]), fill=g)
        d.polygon(pts([(c + 55, c - 55), (c + 100, c - 80), (c + 100, c + 80), (c + 55, c + 55)]), fill=dk)
        shine(d, c - 40, c - 40, 10)

    def amethyst_star(d):
        p, lt, dk = (150, 70, 210), (210, 160, 255), (90, 30, 140)
        st = star(c, c + 6, 118, 50)
        outline_poly(d, st, p)
        d.polygon(pts([(c, c + 6), st[0], st[1]]), fill=lt)
        d.polygon(pts([(c, c + 6), st[4], st[5], st[6]]), fill=dk)
        shine(d, c - 14, c - 30, 10)

    def topaz_triangle(d):
        o, lt, dk = (245, 140, 40), (255, 205, 130), (180, 80, 10)
        tri = [(c, c - 110), (c + 112, c + 90), (c - 112, c + 90)]
        outline_poly(d, tri, o)
        d.polygon(pts([(c, c - 110), (c, c + 30), (c - 112, c + 90)]), fill=lt)
        d.polygon(pts([(c, c + 30), (c + 112, c + 90), (c - 112, c + 90)]), fill=dk)
        shine(d, c - 20, c - 30, 9)

    def diamond(d):
        facet_gem(d, c, c - 4, 112, (220, 240, 255), WHITE, (150, 180, 210))
        shine(d, c - 50, c - 60, 9)

    def pearl(d):
        d.ellipse(pts([(c - 100, c - 100), (c + 100, c + 100)]), fill=(235, 228, 240))
        d.ellipse(pts([(c - 80, c - 85), (c + 90, c + 95)]), fill=(250, 245, 252))
        d.ellipse(pts([(c - 10, c), (c + 98, c + 98)]), fill=(225, 216, 236, 170))
        d.ellipse(pts([(c - 70, c - 75), (c - 20, c - 25)]), fill=WHITE)

    def opal_oval(d):
        d.ellipse(pts([(c - 80, c - 110), (c + 80, c + 110)]), fill=(120, 200, 230))
        rnd = random.Random(3)
        for _ in range(22):
            x, y = rnd.uniform(c - 55, c + 55), rnd.uniform(c - 85, c + 85)
            col = rnd.choice([(255, 120, 180), (255, 220, 120), (140, 255, 200), (180, 140, 255)])
            d.ellipse(pts([(x - 14, y - 10), (x + 14, y + 10)]), fill=col + (190,))
        d.ellipse(pts([(c - 80, c - 110), (c + 80, c + 110)]), outline=WHITE + (200,), width=s(5))
        shine(d, c - 30, c - 60, 12)

    def garnet_hexagon(d):
        r, lt, dk = (150, 20, 50), (220, 80, 110), (90, 5, 30)
        hx = regular_polygon(c, c, 112, 6, math.pi / 6)
        outline_poly(d, hx, r)
        inner = regular_polygon(c, c, 60, 6, math.pi / 6)
        d.polygon(pts(inner), fill=lt)
        d.polygon(pts([hx[0], hx[1], inner[1], inner[0]]), fill=dk)
        d.polygon(pts([hx[5], hx[0], inner[0], inner[5]]), fill=dk)
        shine(d, c - 25, c - 25, 10)

    def citrine_sun(d):
        y, lt, dk = (245, 205, 40), (255, 240, 150), (200, 150, 10)
        for i in range(12):
            a = 2 * math.pi * i / 12
            d.polygon(pts([(c + 70 * math.cos(a - 0.2), c + 70 * math.sin(a - 0.2)),
                           (c + 118 * math.cos(a), c + 118 * math.sin(a)),
                           (c + 70 * math.cos(a + 0.2), c + 70 * math.sin(a + 0.2))]), fill=dk)
        d.ellipse(pts([(c - 78, c - 78), (c + 78, c + 78)]), fill=y)
        d.ellipse(pts([(c - 50, c - 55), (c + 30, c + 25)]), fill=lt)
        shine(d, c - 30, c - 35, 10)

    def aquamarine_octagon(d):
        a, lt, dk = (110, 210, 235), (200, 245, 255), (40, 140, 180)
        oc = regular_polygon(c, c, 115, 8, math.pi / 8)
        outline_poly(d, oc, a)
        inner = regular_polygon(c, c, 62, 8, math.pi / 8)
        d.polygon(pts(inner), fill=lt)
        d.polygon(pts([oc[0], oc[1], oc[2], inner[2], inner[1], inner[0]]), fill=dk)
        shine(d, c - 28, c - 28, 10)

    def jade_crown(d):
        g, dk = (40, 160, 110), (20, 100, 70)
        crown = [(c - 110, c + 70), (c - 110, c - 50), (c - 55, c + 5), (c, c - 95), (c + 55, c + 5),
                 (c + 110, c - 50), (c + 110, c + 70)]
        outline_poly(d, crown, g)
        d.rectangle(pts([(c - 110, c + 50), (c + 110, c + 95)]), fill=dk)
        for x in (c - 110, c, c + 110):
            yy = c - 95 if x == c else c - 50
            d.ellipse(pts([(x - 14, yy - 14), (x + 14, yy + 14)]), fill=GOLD)
        for x in (c - 60, c, c + 60):
            d.ellipse(pts([(x - 10, c + 62), (x + 10, c + 82)]), fill=GOLD)

    makers = [ruby_heart, sapphire_drop, emerald_square, amethyst_star, topaz_triangle, diamond,
              pearl, opal_oval, garnet_hexagon, citrine_sun, aquamarine_octagon, jade_crown]
    for i, fn in enumerate(makers, start=1):
        img, d = canvas(size, size)
        # soft drop shadow
        sh, sd = canvas(size, size)
        sd.ellipse(pts([(c - 90, size - 40), (c + 90, size - 14)]), fill=(0, 0, 0, 90))
        img = Image.alpha_composite(img, sh.filter(ImageFilter.GaussianBlur(s(6))))
        d = BlendDraw(img)
        fn(d)
        save_png(finish(img, size, size), "item_%02d" % i)


# ---------------------------------------------------------------- logo
def make_logo():
    w, h = 900, 460
    img, d = canvas(w, h)
    # banner ribbon
    d.polygon(pts([(40, 300), (860, 300), (820, 360), (860, 420), (40, 420), (80, 360)]), fill=MAGENTA_DK)
    d.rounded_rectangle(pts([(90, 290), (810, 410)]), radius=s(30), fill=MAGENTA)
    # jester hat + gem at the top
    cx = w / 2
    d.polygon(pts([(cx - 70, 110), (cx - 120, 20), (cx - 35, 70), (cx, 0 + 6), (cx + 35, 70), (cx + 120, 20), (cx + 70, 110)]), fill=MAGENTA)
    d.polygon(pts([(cx, 6), (cx + 35, 70), (cx, 110)]), fill=MAGENTA_DK)
    d.rectangle(pts([(cx - 72, 100), (cx + 72, 118)]), fill=GOLD)
    for bx, by in ((cx - 120, 20), (cx, 8), (cx + 120, 20)):
        d.ellipse(pts([(bx - 14, by - 14), (bx + 14, by + 14)]), fill=GOLD)
    facet_gem(d, cx, 150, 46, TEAL, TEAL_LT, TEAL_DK)
    # wordmark
    f1 = font(s(118))
    text = "Jokello"
    tw = d.textlength(text, font=f1)
    d.text((s(cx) - tw / 2, s(160)), text, font=f1, fill=GOLD, stroke_width=s(7), stroke_fill=NIGHT)
    f2 = font(s(66))
    text2 = "J E W E L S"
    tw2 = d.textlength(text2, font=f2)
    d.text((s(cx) - tw2 / 2, s(318)), text2, font=f2, fill=WHITE, stroke_width=s(4), stroke_fill=MAGENTA_DK)
    save_png(finish(img, w, h), "logo")


# ---------------------------------------------------------------- mascots
def make_mascot_a():
    """Jojo the jester."""
    w = h = 512
    img, d = canvas(w, h)
    cx = w / 2
    # body: harlequin diamond tunic
    d.polygon(pts([(cx - 150, 500), (cx - 110, 330), (cx + 110, 330), (cx + 150, 500)]), fill=MAGENTA)
    d.polygon(pts([(cx, 330), (cx + 55, 415), (cx, 500), (cx - 55, 415)]), fill=TEAL)
    d.polygon(pts([(cx - 110, 330), (cx - 55, 415), (cx - 130, 470)]), fill=GOLD)
    d.polygon(pts([(cx + 110, 330), (cx + 55, 415), (cx + 130, 470)]), fill=GOLD)
    # ruffled collar
    for i in range(7):
        x = cx - 120 + i * 40
        d.ellipse(pts([(x - 26, 300), (x + 26, 350)]), fill=CREAM)
    # face
    d.ellipse(pts([(cx - 105, 140), (cx + 105, 330)]), fill=(255, 220, 190))
    d.ellipse(pts([(cx - 60, 205), (cx - 25, 245)]), fill=NIGHT)
    d.ellipse(pts([(cx + 25, 205), (cx + 60, 245)]), fill=NIGHT)
    d.ellipse(pts([(cx - 50, 212), (cx - 38, 224)]), fill=WHITE)
    d.ellipse(pts([(cx + 35, 212), (cx + 47, 224)]), fill=WHITE)
    d.ellipse(pts([(cx - 90, 250), (cx - 55, 280)]), fill=(255, 150, 160))
    d.ellipse(pts([(cx + 55, 250), (cx + 90, 280)]), fill=(255, 150, 160))
    d.arc(pts([(cx - 50, 240), (cx + 50, 300)]), start=10, end=170, fill=MAGENTA_DK, width=s(8))
    # diamond face paint
    d.polygon(pts([(cx + 72, 180), (cx + 82, 196), (cx + 72, 212), (cx + 62, 196)]), fill=TEAL)
    # hat
    d.polygon(pts([(cx - 115, 175), (cx - 200, 40), (cx - 55, 120), (cx, 10), (cx + 55, 120), (cx + 200, 40), (cx + 115, 175)]), fill=MAGENTA)
    d.polygon(pts([(cx, 10), (cx + 55, 120), (cx + 200, 40), (cx + 115, 175), (cx, 160)]), fill=VELVET)
    d.rounded_rectangle(pts([(cx - 118, 158), (cx + 118, 190)]), radius=s(14), fill=GOLD)
    for bx, by in ((cx - 200, 40), (cx, 12), (cx + 200, 40)):
        d.ellipse(pts([(bx - 20, by - 20), (bx + 20, by + 20)]), fill=GOLD)
        d.ellipse(pts([(bx - 7, by + 2), (bx + 7, by + 14)]), fill=GOLD_DK)
    facet_gem(d, cx, 172, 18, TEAL, TEAL_LT, TEAL_DK)
    save_png(finish(img, w, h), "mascot_a")


def make_mascot_b():
    """Gemmy the gem with a tiny crown."""
    w = h = 512
    img, d = canvas(w, h)
    cx, cy = w / 2, 290
    facet_gem(d, cx, cy, 190, TEAL, TEAL_LT, TEAL_DK)
    # face
    d.ellipse(pts([(cx - 70, cy - 10), (cx - 30, cy + 35)]), fill=NIGHT)
    d.ellipse(pts([(cx + 30, cy - 10), (cx + 70, cy + 35)]), fill=NIGHT)
    d.ellipse(pts([(cx - 60, cy - 2), (cx - 46, cy + 12)]), fill=WHITE)
    d.ellipse(pts([(cx + 40, cy - 2), (cx + 54, cy + 12)]), fill=WHITE)
    d.arc(pts([(cx - 45, cy + 20), (cx + 45, cy + 80)]), start=15, end=165, fill=NIGHT, width=s(9))
    d.ellipse(pts([(cx - 100, cy + 35), (cx - 68, cy + 60)]), fill=(255, 140, 170, 200))
    d.ellipse(pts([(cx + 68, cy + 35), (cx + 100, cy + 60)]), fill=(255, 140, 170, 200))
    # crown
    crown = [(cx - 80, 140), (cx - 85, 60), (cx - 40, 100), (cx, 40), (cx + 40, 100), (cx + 85, 60), (cx + 80, 140)]
    d.polygon(pts(crown), fill=GOLD)
    d.line(pts(crown), fill=GOLD_DK, width=s(5))
    for bx, by in ((cx - 85, 60), (cx, 40), (cx + 85, 60)):
        d.ellipse(pts([(bx - 12, by - 12), (bx + 12, by + 12)]), fill=MAGENTA)
    # little arms
    d.line(pts([(cx - 180, cy - 30), (cx - 230, cy - 90)]), fill=TEAL_DK, width=s(16))
    d.line(pts([(cx + 180, cy - 30), (cx + 230, cy - 90)]), fill=TEAL_DK, width=s(16))
    d.ellipse(pts([(cx - 252, cy - 112), (cx - 210, cy - 70)]), fill=CREAM)
    d.ellipse(pts([(cx + 210, cy - 112), (cx + 252, cy - 70)]), fill=CREAM)
    save_png(finish(img, w, h), "mascot_b")


# ---------------------------------------------------------------- plates & icons
def make_btn_menu():
    w, h = 720, 240  # aspect 3.0 → Assets.BTN_MENU_ASPECT
    img, d = canvas(w, h)
    d.rounded_rectangle(pts([(6, 12), (w - 6, h - 6)]), radius=s(60), fill=(0, 0, 0, 90))
    d.rounded_rectangle(pts([(4, 4), (w - 4, h - 14)]), radius=s(60), fill=GOLD_DK)
    d.rounded_rectangle(pts([(12, 10), (w - 12, h - 22)]), radius=s(54), fill=GOLD)
    inner = vgrad(s(w - 56), s(h - 68), MAGENTA, MAGENTA_DK)
    mask = Image.new("L", inner.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, inner.size[0] - 1, inner.size[1] - 1], radius=s(40), fill=255)
    img.paste(inner, (s(28), s(24)), mask)
    d = BlendDraw(img)
    d.rounded_rectangle(pts([(28, 24), (w - 28, h - 44)]), radius=s(40), outline=CREAM + (150,), width=s(3))
    d.rounded_rectangle(pts([(60, 34), (w - 60, 70)]), radius=s(18), fill=WHITE + (45,))
    # corner jewels
    for x in (22, w - 22):
        d.polygon(pts([(x, h / 2 - 26 - 6), (x + 18, h / 2 - 6), (x, h / 2 + 26 - 6), (x - 18, h / 2 - 6)]), fill=TEAL)
        d.polygon(pts([(x, h / 2 - 26 - 6), (x + 18, h / 2 - 6), (x, h / 2 - 6)]), fill=TEAL_LT)
    for x in (w * 0.3, w * 0.5, w * 0.7):
        d.ellipse(pts([(x - 9, 2), (x + 9, 20)]), fill=TEAL)
        d.ellipse(pts([(x - 9, h - 32), (x + 9, h - 14)]), fill=TEAL)
    save_png(finish(img, w, h), "btn_menu")


def make_btn_plate():
    w, h = 640, 160
    img, d = canvas(w, h)
    d.rounded_rectangle(pts([(2, 2), (w - 2, h - 2)]), radius=s(44), fill=GOLD)
    inner = vgrad(s(w - 16), s(h - 16), MAGENTA, VELVET)
    mask = Image.new("L", inner.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, inner.size[0] - 1, inner.size[1] - 1], radius=s(38), fill=255)
    img.paste(inner, (s(8), s(8)), mask)
    d = BlendDraw(img)
    d.rounded_rectangle(pts([(40, 16), (w - 40, 50)]), radius=s(16), fill=WHITE + (40,))
    save_png(finish(img, w, h), "btn_plate")


def make_plate_round():
    size = 256
    img, d = canvas(size, size)
    c = size / 2
    d.ellipse(pts([(4, 8), (size - 4, size - 2)]), fill=(0, 0, 0, 90))
    d.ellipse(pts([(4, 4), (size - 4, size - 8)]), fill=GOLD)
    d.ellipse(pts([(16, 16), (size - 16, size - 20)]), fill=MAGENTA)
    # facets
    oc = regular_polygon(c, c - 2, 82, 8, math.pi / 8)
    d.polygon(pts(oc), fill=MAGENTA_DK)
    inner = regular_polygon(c, c - 2, 58, 8, math.pi / 8)
    d.polygon(pts(inner), fill=VELVET)
    for a, b in zip(oc, inner):
        d.line(pts([a, b]), fill=CREAM + (90,), width=s(2))
    d.ellipse(pts([(60, 34), (120, 70)]), fill=WHITE + (70,))
    save_png(finish(img, size, size), "plate_round")


def round_icon_base(d, size):
    d.ellipse(pts([(4, 8), (size - 4, size - 2)]), fill=(0, 0, 0, 100))
    d.ellipse(pts([(4, 4), (size - 4, size - 8)]), fill=GOLD)
    d.ellipse(pts([(14, 14), (size - 14, size - 18)]), fill=VELVET)


def make_icon_back():
    size = 128
    img, d = canvas(size, size)
    round_icon_base(d, size)
    d.polygon(pts([(38, 60), (70, 32), (70, 48), (92, 48), (92, 72), (70, 72), (70, 88)]), fill=CREAM)
    save_png(finish(img, size, size), "icon_back")


def make_icon_pause():
    size = 128
    img, d = canvas(size, size)
    round_icon_base(d, size)
    d.rounded_rectangle(pts([(44, 38), (58, 84)]), radius=s(4), fill=CREAM)
    d.rounded_rectangle(pts([(70, 38), (84, 84)]), radius=s(4), fill=CREAM)
    save_png(finish(img, size, size), "icon_pause")


# ---------------------------------------------------------------- launcher (legacy PNG, API 24-25)
def draw_launcher(size, round_mask):
    base = 108.0
    k = size / base
    img, d = canvas(size, size)
    if round_mask:
        d.ellipse(pts([(0, 0), (size, size)]), fill=PLUM)
    else:
        d.rounded_rectangle(pts([(0, 0), (size, size)]), radius=s(size * 0.18), fill=PLUM)
    # icon art is 108-unit canvas; legacy icons show the full art scaled up from the safe zone
    zoom = 1.45
    off = (size - base * k * zoom) / 2

    def p(x, y):
        return (off + x * k * zoom - (zoom - 1) * 0, off + y * k * zoom)

    def poly(points, fill):
        d.polygon(pts([p(x, y) for x, y in points]), fill=fill)

    poly([(34, 54), (42, 46), (66, 46), (74, 54), (54, 82)], TEAL)
    poly([(42, 46), (54, 46), (48, 54), (34, 54)], TEAL_LT)
    poly([(60, 54), (74, 54), (54, 82)], TEAL_DK)
    poly([(36, 44), (30, 30), (44, 38), (54, 24), (64, 38), (78, 30), (72, 44)], MAGENTA)
    poly([(54, 24), (64, 38), (54, 44)], MAGENTA_DK)
    poly([(36, 42), (72, 42), (72, 46), (36, 46)], GOLD)
    for bx, by in ((30, 30), (54, 24), (78, 30)):
        x, y = p(bx, by)
        r = 3 * k * zoom
        d.ellipse(pts([(x - r, y - r), (x + r, y + r)]), fill=GOLD)
    img = finish(img, size, size)
    # center the art vertically (art spans y 21..82 in the 108 canvas)
    return img


def make_launchers():
    sizes = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    for dpi, px in sizes.items():
        folder = os.path.join(RES, "mipmap-" + dpi)
        os.makedirs(folder, exist_ok=True)
        draw_launcher(px, False).save(os.path.join(folder, "ic_launcher.png"), optimize=True)
        draw_launcher(px, True).save(os.path.join(folder, "ic_launcher_round.png"), optimize=True)


# ---------------------------------------------------------------- SFX
RATE = 22050


def tone(freqs, dur, vol=0.5, wave_kind="sine", attack=0.005, decay=None, slide=0.0):
    n = int(RATE * dur)
    out = []
    for i in range(n):
        t = i / RATE
        f = freqs[min(len(freqs) - 1, int(len(freqs) * i / n))] * (1 + slide * t / dur)
        ph = 2 * math.pi * f * t
        if wave_kind == "square":
            v = 1.0 if math.sin(ph) >= 0 else -1.0
            v *= 0.5
        elif wave_kind == "tri":
            v = 2 / math.pi * math.asin(math.sin(ph))
        else:
            v = math.sin(ph) + 0.25 * math.sin(2 * ph)
        env = min(1.0, t / attack) if attack > 0 else 1.0
        tail = decay if decay is not None else dur
        env *= math.exp(-3.0 * t / tail)
        out.append(v * env * vol)
    return out


def notes(seq, note_dur, **kw):
    out = []
    for f in seq:
        out += tone([f], note_dur, **kw)
    return out


def write_wav(name, samples):
    os.makedirs(RAW, exist_ok=True)
    peak = max(1e-6, max(abs(x) for x in samples))
    scale = min(1.0, 0.9 / peak)
    with wave.open(os.path.join(RAW, name + ".wav"), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, x * scale)) * 32767)) for x in samples))


def make_sfx():
    write_wav("sfx_click", tone([1400], 0.05, vol=0.4, wave_kind="tri", decay=0.02))
    write_wav("sfx_correct", notes([784, 1175], 0.09, vol=0.5, decay=0.12))
    write_wav("sfx_wrong", tone([220, 185], 0.28, vol=0.35, wave_kind="square", decay=0.25, slide=-0.15))
    write_wav("sfx_match", notes([1047, 1319, 1568], 0.06, vol=0.45, wave_kind="tri", decay=0.1))
    write_wav("sfx_win", notes([523, 659, 784, 1047, 1319], 0.12, vol=0.5, decay=0.25))
    write_wav("sfx_lose", notes([392, 330, 262, 196], 0.16, vol=0.45, wave_kind="tri", decay=0.3))


if __name__ == "__main__":
    import sys
    # Default: synthesized SFX only (the art now comes from tools/import_art.py).
    # Pass --art to regenerate the original vector-style placeholder art as well
    # (this OVERWRITES the imported art with simple placeholders).
    if "--art" in sys.argv:
        os.makedirs(NODPI, exist_ok=True)
        make_bg()
        make_items()
        make_logo()
        make_mascot_a()
        make_mascot_b()
        make_btn_menu()
        make_btn_plate()
        make_plate_round()
        make_icon_back()
        make_icon_pause()
        make_launchers()
    make_sfx()
    print("Generated in", RES)
