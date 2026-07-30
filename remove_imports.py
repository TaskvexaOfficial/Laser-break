import sys

with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "import androidx.compose.ui.graphics.drawscope.clipPath" in line:
        continue
    if "import androidx.compose.ui.graphics.drawscope.translate" in line:
        continue
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'w') as f:
    f.writelines(new_lines)
print("Removed bad imports")
