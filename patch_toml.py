import re

with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

content = content.replace('[versions]', '[versions]\nplayServicesAds = "23.6.0"')
content = content.replace('[libraries]', '[libraries]\nplay-services-ads = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "playServicesAds" }')

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)

