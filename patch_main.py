import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace('import android.os.Bundle', 'import android.os.Bundle\nimport com.google.android.gms.ads.MobileAds')
content = content.replace('super.onCreate(savedInstanceState)', 'super.onCreate(savedInstanceState)\n        MobileAds.initialize(this) {}')

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

