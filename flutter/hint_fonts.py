import os
import subprocess

fonts_dir = r"D:\Projects\Elvan Niril\flutter\assets\fonts"

for file in os.listdir(fonts_dir):
    if file.endswith(".ttf"):
        input_file = os.path.join(fonts_dir, file)
        temp_file = os.path.join(fonts_dir, "hinted_" + file)
        
        print(f"Hinting {file}...")
        try:
            subprocess.run(["python", "-m", "ttfautohint", input_file, temp_file], check=True)
            os.replace(temp_file, input_file)
            print(f"Successfully hinted {file}")
        except subprocess.CalledProcessError as e:
            print(f"Failed to hint {file}: {e}")
            if os.path.exists(temp_file):
                os.remove(temp_file)
