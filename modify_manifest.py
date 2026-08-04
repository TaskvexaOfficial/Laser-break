import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Add uses-permission
    content = content.replace(
        "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />",
        "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />\n    <uses-permission android:name=\"com.google.android.gms.permission.AD_ID\" />"
    )

    # Add meta-data
    content = content.replace(
        "        <meta-data\n            android:name=\"com.google.android.gms.ads.APPLICATION_ID\"",
        """        <meta-data
            android:name="com.startapp.sdk.APPLICATION_ID"
            android:value="207638766" />
        <meta-data
            android:name="com.startapp.sdk.RETURN_ADS_ENABLED"
            android:value="false" />
        <meta-data
            android:name="com.startapp.sdk.SPLASH_ENABLED"
            android:value="false" />
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID\""""
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/AndroidManifest.xml')
