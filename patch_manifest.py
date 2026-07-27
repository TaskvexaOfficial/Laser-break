import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

content = content.replace('<uses-permission android:name="android.permission.VIBRATE" />', '<uses-permission android:name="android.permission.VIBRATE" />\n    <uses-permission android:name="android.permission.INTERNET" />\n    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />')

content = content.replace('<application', '<application')
content = content.replace('        android:theme="@style/Theme.MyApplication">', '        android:theme="@style/Theme.MyApplication">\n        <meta-data\n            android:name="com.google.android.gms.ads.APPLICATION_ID"\n            android:value="ca-app-pub-3940256099942544~3347511713"/>')

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)

