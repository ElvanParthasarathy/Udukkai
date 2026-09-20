import sys
from fontTools.ttLib import TTFont

def get_chars(font_path):
    font = TTFont(font_path)
    chars = set()
    for table in font['cmap'].tables:
        for codepoint, name in table.cmap.items():
            chars.add(codepoint)
    return chars

def main():
    elvan_path = 'assets/fonts/ElvanSans-Regular.ttf'
    anand_path = 'assets/fonts/Anand_MuktaMalar.ttf'
    
    try:
        elvan_chars = get_chars(elvan_path)
        anand_chars = get_chars(anand_path)
    except Exception as e:
        print(f"Error reading fonts: {e}")
        return

    common = elvan_chars.intersection(anand_chars)
    only_elvan = elvan_chars - anand_chars
    only_anand = anand_chars - elvan_chars

    print(f"ElvanSans total characters: {len(elvan_chars)}")
    print(f"Anand MuktaMalar total characters: {len(anand_chars)}")
    print(f"Common characters: {len(common)}")
    print(f"Characters ONLY in ElvanSans: {len(only_elvan)}")
    print(f"Characters ONLY in Anand MuktaMalar: {len(only_anand)}")
    
    print("\n--- Important Missing Characters in ElvanSans (Present in Anand MuktaMalar) ---")
    
    # We are specifically looking for Latin Extended and PUA characters
    # that tamil_pdf_shaper maps to! (e.g. U+1D80 to U+1DFF, or PUA U+E000+)
    missing_important = []
    for c in sorted(only_anand):
        # Filter to show only characters outside the standard ASCII range 
        # (mostly focusing on the shaping codes)
        if c > 0x007F:
            missing_important.append(c)
    
    # Print the first 50 to avoid spamming the console
    for i, c in enumerate(missing_important[:50]):
        try:
            char_str = chr(c)
        except:
            char_str = '?'
        print(f"U+{c:04X} ({char_str})", end=", ")
    if len(missing_important) > 50:
        print(f"... and {len(missing_important) - 50} more.")

if __name__ == '__main__':
    main()
