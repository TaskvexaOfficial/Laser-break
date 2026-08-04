import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        lines = f.readlines()

    new_lines = []
    i = 0
    while i < len(lines):
        line = lines[i]
        new_lines.append(line)
        if 'run: gradle assembleRelease' in line:
            new_lines.append('      - name: Build Debug APK\n')
            new_lines.append('        run: gradle assembleDebug\n')
        if 'path: app/build/outputs/apk/release/**/*.apk' in line:
            new_lines.append('          retention-days: 30\n')
            new_lines.append('      - name: Upload Debug APK\n')
            new_lines.append('        uses: actions/upload-artifact@v4\n')
            new_lines.append('        with:\n')
            new_lines.append('          name: LaserBreak-Debug-Test-APK\n')
            new_lines.append('          path: app/build/outputs/apk/debug/**/*.apk\n')
            new_lines.append('          retention-days: 30\n')
            i += 1 # skip next line retention-days
            while i < len(lines) and 'retention-days' in lines[i]:
                i += 1
            continue
        i += 1

    with open(file_path, 'w') as f:
        f.writelines(new_lines)

if __name__ == '__main__':
    modify('.github/workflows/build-apk.yml')
