import sys

with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'r') as f:
    content = f.read()

target = "androidx.compose.ui.graphics.drawscope.withTransform({"
replacement = "withTransform({"

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'w') as f:
        f.write(content)
    print("Fixed withTransform call")
else:
    print("Target not found")
