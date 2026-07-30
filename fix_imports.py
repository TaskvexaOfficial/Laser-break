import sys

with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'r') as f:
    content = f.read()

imports_to_add = """import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
"""

# add imports after the other compose graphics imports
if "import androidx.compose.ui.graphics.Color" in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", "import androidx.compose.ui.graphics.Color\n" + imports_to_add)
    with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'w') as f:
        f.write(content)
    print("Added imports")
