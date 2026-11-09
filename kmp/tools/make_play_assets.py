"""
Builds the Google Play image assets in kmp/play-store from the app's own files.

  python -I make_play_assets.py [raw_screenshot_dir]

- icon-512.png: the launcher icon (ic_launcher_background gradient + the white emblem of each app), 512 x 512, rendered
  straight from the vector drawables, so it is always the same logo as the installed app.
- feature-graphic.png: 1024 x 500, the icon, the app name and one line.
- screenshots: raw phone screenshots (names t_*.png for Textiles, f_*.png for Factory) are cropped to 1080 x 2160 (9:18,
  inside Play's 2:1 limit) without the status bar (the bottom bar stays), and numbered in order.
"""
import os, re, sys
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "..", "composeApp", "src")
OUT = os.path.join(HERE, "..", "play-store")
SS = 4  # supersampling

def bezier(p0, p1, p2, p3, n=24):
    return [((1-t)**3*p0[0] + 3*(1-t)**2*t*p1[0] + 3*(1-t)*t*t*p2[0] + t**3*p3[0],
             (1-t)**3*p0[1] + 3*(1-t)**2*t*p1[1] + 3*(1-t)*t*t*p2[1] + t**3*p3[1]) for t in [i/n for i in range(1, n+1)]]

def subpaths(d):
    toks = re.findall(r"[MLCZ]|-?\d+\.?\d*(?:e-?\d+)?", d)
    out, cur, i, cmd = [], [], 0, None
    while i < len(toks):
        if toks[i] in "MLCZ":
            cmd = toks[i]; i += 1
            if cmd == "Z":
                if cur: out.append(cur); cur = []
                continue
        if cmd == "M":
            if cur: out.append(cur)
            cur = [(float(toks[i]), float(toks[i+1]))]; i += 2; cmd = "L"
        elif cmd == "L":
            cur.append((float(toks[i]), float(toks[i+1]))); i += 2
        elif cmd == "C":
            v = [float(x) for x in toks[i:i+6]]; i += 6
            cur += bezier(cur[-1], (v[0], v[1]), (v[2], v[3]), (v[4], v[5]))
    if cur: out.append(cur)
    return out

def area(p):
    return sum(p[i][0]*p[(i+1) % len(p)][1] - p[(i+1) % len(p)][0]*p[i][1] for i in range(len(p))) / 2

def render_icon(flavour, size=512):
    xml = open(os.path.join(SRC, flavour, "res", "drawable", "ic_launcher_foreground.xml"), encoding="utf8").read()
    g = re.search(r'translateX="([\d.]+)"\s+android:translateY="([\d.]+)"\s+android:scaleX="([\d.]+)"', xml, re.S)
    tx, ty, sc = float(g.group(1)), float(g.group(2)), float(g.group(3))
    big = size * SS
    k = big / 108.0
    # background: ic_launcher_background.xml gradient, top to bottom
    bg = open(os.path.join(SRC, "androidMain", "res", "drawable", "ic_launcher_background.xml"), encoding="utf8").read()
    c0, c1, c2 = [re.search(r'%sColor="#FF([0-9A-Fa-f]{6})"' % n, bg).group(1) for n in ("start", "center", "end")]
    rgb = lambda h: tuple(int(h[i:i+2], 16) for i in (0, 2, 4))
    img = Image.new("RGB", (big, big))
    px = ImageDraw.Draw(img)
    for y in range(big):
        t = y / (big - 1)
        a, b, u = (rgb(c0), rgb(c1), t*2) if t < .5 else (rgb(c1), rgb(c2), (t-.5)*2)
        px.line([(0, y), (big, y)], fill=tuple(int(a[j] + (b[j]-a[j])*u) for j in range(3)))
    for d in re.findall(r'pathData="([^"]*)"', xml):
        subs = [[((x*sc + tx) * k, (y*sc + ty) * k) for x, y in s] for s in subpaths(d)]
        outer = max(subs, key=lambda s: abs(area(s)))
        sign = area(outer) > 0
        mask = Image.new("L", (big, big), 0)
        md = ImageDraw.Draw(mask)
        for s in subs:
            if (area(s) > 0) == sign: md.polygon(s, fill=255)
        for s in subs:
            if (area(s) > 0) != sign: md.polygon(s, fill=0)
        img.paste((255, 255, 255), mask=mask)
    return img.resize((size, size), Image.LANCZOS)

def font(bold, size):
    return ImageFont.truetype("C:/Windows/Fonts/%s.ttf" % ("segoeuib" if bold else "segoeui"), size)

def feature_graphic(icon, name, line):
    img = Image.new("RGB", (1024, 500), (17, 17, 17))
    ic = icon.resize((280, 280), Image.LANCZOS)
    m = Image.new("L", (280, 280), 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, 279, 279], 62, fill=255)
    img.paste(ic, (70, 110), m)
    d = ImageDraw.Draw(img)
    d.text((395, 168), name, font=font(True, 62), fill=(255, 255, 255))
    words, lines, cur = line.split(), [], ""
    for w in words:
        t = (cur + " " + w).strip()
        if d.textlength(t, font=font(False, 32)) > 590 and cur: lines.append(cur); cur = w
        else: cur = t
    lines.append(cur)
    for i, l in enumerate(lines):
        d.text((397, 262 + i*44), l, font=font(False, 32), fill=(170, 170, 170))
    return img

APPS = {"textiles": ("Udukkai Textiles", "GST bills, receipts and customers for silk and textile shops."),
        "factory": ("Udukkai Factory", "Job-work bills and receipts for silk twisting and coolie work.")}

def crop_screenshot(src, dst):
    im = Image.open(src).convert("RGB")
    w, h = im.size
    top = 180 * h // 2400
    im = im.crop((0, top, w, top + w * 2))  # width x 2 x width = 1:2
    ImageDraw.Draw(im).rectangle([0, 0, 16 * w // 1080, 260 * w // 1080], fill=(0, 0, 0))  # the Samsung edge-panel handle on the left edge
    im.save(dst, optimize=True)

if __name__ == "__main__":
    for flavour, (name, line) in APPS.items():
        folder = os.path.join(OUT, flavour)
        os.makedirs(os.path.join(folder, "screenshots", "phone"), exist_ok=True)
        icon = render_icon(flavour)
        icon.save(os.path.join(folder, "icon-512.png"))
        feature_graphic(icon, name, line).save(os.path.join(folder, "feature-graphic-1024x500.png"))
        if len(sys.argv) > 1:
            order = ["home", "create", "receipts", "items", "customers", "invoice", "invoice_items", "settings"]
            raw = sorted((f for f in os.listdir(sys.argv[1]) if f.startswith(flavour[0] + "_") and f.endswith(".png")),
                         key=lambda f: order.index(f[2:-4]) if f[2:-4] in order else 99)
            for n, f in enumerate(raw, 1):
                crop_screenshot(os.path.join(sys.argv[1], f), os.path.join(folder, "screenshots", "phone", "%02d_%s" % (n, f[2:])))
    print("done")
