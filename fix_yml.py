import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    target = """      - name: Build Release APK
        env:
          STORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: gradle assembleRelease
      - name: Upload Release APK
        uses: actions/upload-artifact@v4
        with:
          name: LaserBreak-Signed-Release-APK
          path: app/build/outputs/apk/release/**/*.apk
          retention-days: 30"""

    replacement = """      - name: Build Release APK
        env:
          STORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: gradle assembleRelease
      - name: Build Debug APK
        run: gradle assembleDebug
      - name: Upload Release APK
        uses: actions/upload-artifact@v4
        with:
          name: LaserBreak-Signed-Release-APK
          path: app/build/outputs/apk/release/**/*.apk
          retention-days: 30
      - name: Upload Debug APK
        uses: actions/upload-artifact@v4
        with:
          name: LaserBreak-Debug-Test-APK
          path: app/build/outputs/apk/debug/**/*.apk
          retention-days: 30"""

    content = content.replace(target, replacement)
    
    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('.github/workflows/build-apk.yml')
