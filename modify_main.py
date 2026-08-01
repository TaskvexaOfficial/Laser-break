import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Add imports
    content = content.replace(
        "import com.example.audio.SoundManager",
        "import com.example.audio.SoundManager\nimport com.example.ads.ConsentManager"
    )

    content = content.replace(
        "private lateinit var soundManager: SoundManager",
        "private lateinit var soundManager: SoundManager\n    private lateinit var consentManager: ConsentManager"
    )

    content = content.replace(
        "super.onCreate(savedInstanceState)",
        "super.onCreate(savedInstanceState)\n        consentManager = ConsentManager(this)\n        consentManager.gatherConsent {}"
    )

    # Remove MobileAds.initialize
    content = content.replace(
        "        MobileAds.initialize(this) {}\n",
        ""
    )
    
    # Pass consentManager to AppNavigation
    content = content.replace(
        "AppNavigation(soundManager = soundManager)",
        "AppNavigation(soundManager = soundManager, consentManager = consentManager)"
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/MainActivity.kt')
