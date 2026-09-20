import json
with open(r'C:\Users\jaipr\Downloads\elvan-niril-default-rtdb-export.json', encoding='utf-8') as f:
    d=json.load(f)
p=d.get('profile',{})
p.pop('logo',None)
p.pop('wideLogo',None)
p.pop('signature',None)
cp=d.get('coolie_thannilai',{})
for k in cp:
    cp[k].pop('logo',None)
    cp[k].pop('signature',None)
with open('profile_dump_final.txt', 'w', encoding='utf-8') as out:
    out.write('SILK PROFILE:\n')
    out.write(json.dumps(p, indent=2, ensure_ascii=False))
    out.write('\n\nCOOLIE THANNILAI:\n')
    out.write(json.dumps(cp, indent=2, ensure_ascii=False))

