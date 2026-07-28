import re

with open('app/src/main/java/com/example/ui/AppNavigation.kt', 'r') as f:
    content = f.read()

# Make sure onSoundToggle updates SoundManager immediately
old_toggle = """                onSoundToggle = { 
                     scope.launch { gemDataStore.setSoundEnabled(it) }
                },"""
new_toggle = """                onSoundToggle = { 
                    soundManager.setSoundEnabled(it)
                    scope.launch { gemDataStore.setSoundEnabled(it) }
                },"""
content = content.replace(old_toggle, new_toggle)

with open('app/src/main/java/com/example/ui/AppNavigation.kt', 'w') as f:
    f.write(content)
