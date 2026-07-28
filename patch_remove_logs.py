import re

with open('app/src/main/java/com/example/audio/SoundManager.kt', 'r') as f:
    content = f.read()

content = content.replace('Log.d("AudioDiagnostic", "Background audio resource loaded")', '')
content = content.replace('Log.d("AudioDiagnostic", "Laser audio resource loaded")', '')
content = content.replace('Log.d("AudioDiagnostic", "Sound preference enabled/disabled: $enabled")', '')
content = content.replace('Log.d("AudioDiagnostic", "Background music started")', '')
content = content.replace('Log.d("AudioDiagnostic", "Laser sound started")', '')
content = content.replace('Log.d("AudioDiagnostic", "Laser sound stopped")', '')

# Clean up empty lines created
content = re.sub(r'\n\s*\n', '\n', content)

with open('app/src/main/java/com/example/audio/SoundManager.kt', 'w') as f:
    f.write(content)
