import re

file_path = 'd:/Projects/Elvan Niril/flutter/lib/src/localization/language_keys/tg.dart'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

def replacer(match):
    prefix = match.group(1)
    val = match.group(2)
    suffix = match.group(3)
    
    words = val.split(' ')
    new_words = []
    for w in words:
        if not w:
            new_words.append(w)
            continue
        
        chars = list(w)
        for i, c in enumerate(chars):
            if c.isalpha():
                chars[i] = c.upper()
                break
        new_words.append(''.join(chars))
        
    return prefix + ' '.join(new_words) + suffix

new_content = re.sub(r"(: ')([^']*)(')", replacer, content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)
print('Done')
