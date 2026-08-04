import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    block_to_remove = """        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="${admob_app_id}"/>
"""
    content = content.replace(block_to_remove, "")
    
    # Also in case there's no trailing newline
    block_to_remove_2 = """        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="${admob_app_id}"/>"""
    content = content.replace(block_to_remove_2, "")

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/AndroidManifest.xml')
