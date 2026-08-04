import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    target1 = """      - name: Build Debug APK
        run: gradle assembleDebug"""
    
    replacement1 = """      - name: Generate Debug Keystore
        run: |
          keytool -genkeypair -noprompt \\
            -keystore "$GITHUB_WORKSPACE/debug.keystore" \\
            -storepass android \\
            -alias androiddebugkey \\
            -keypass android \\
            -dname "CN=Android Debug,O=Android,C=US" \\
            -keyalg RSA \\
            -keysize 2048 \\
            -validity 10000
      - name: Build Debug APK
        run: gradle assembleDebug"""
        
    content = content.replace(target1, replacement1)
    
    target2 = """      - name: Cleanup Keystore
        if: always()
        run: rm -f my-upload-key.jks"""
        
    replacement2 = """      - name: Cleanup Keystore
        if: always()
        run: rm -f my-upload-key.jks
      - name: Cleanup Debug Keystore
        if: always()
        run: rm -f "$GITHUB_WORKSPACE/debug.keystore\""""
        
    content = content.replace(target2, replacement2)

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('.github/workflows/build-apk.yml')
