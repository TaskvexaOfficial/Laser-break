import re

with open('app/src/main/java/com/example/audio/SoundManager.kt', 'r') as f:
    content = f.read()

# Make sure Toasts use context.applicationContext to avoid crashes if Activity is finishing
content = content.replace('Toast.makeText(context,', 'Toast.makeText(context.applicationContext,')

with open('app/src/main/java/com/example/audio/SoundManager.kt', 'w') as f:
    f.write(content)
