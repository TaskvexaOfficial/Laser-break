import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Replace release config
    content = content.replace(
        "      signingConfig = signingConfigs.getByName(\"release\")",
        """      signingConfig = signingConfigs.getByName("release")
      manifestPlaceholders["admob_app_id"] = "ca-app-pub-4085997939787101~7207491422"
      buildConfigField("String", "REWARDED_AD_UNIT_ID", "\\\"ca-app-pub-4085997939787101/3097121955\\\"")"""
    )

    # Replace debug config
    content = content.replace(
        "    debug { signingConfig = signingConfigs.getByName(\"debugConfig\") }",
        """    debug { 
      signingConfig = signingConfigs.getByName("debugConfig")
      manifestPlaceholders["admob_app_id"] = "ca-app-pub-3940256099942544~3347511713"
      buildConfigField("String", "REWARDED_AD_UNIT_ID", "\\\"ca-app-pub-3940256099942544/5224354917\\\"")
    }"""
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/build.gradle.kts')
