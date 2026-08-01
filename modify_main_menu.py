import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Change signature
    content = content.replace(
        "    onPlayClick: () -> Unit\n) {",
        "    onPlayClick: () -> Unit,\n    isPrivacyOptionsRequired: Boolean = false,\n    onPrivacyOptionsClick: () -> Unit = {}\n) {"
    )

    # Add Privacy Options button before "A TaskVexa Game"
    content = content.replace(
        """            Text(
                text = "A TaskVexa Game",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )""",
        """            if (isPrivacyOptionsRequired) {
                TextButton(onClick = onPrivacyOptionsClick) {
                    Text("Privacy Options", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = "A TaskVexa Game",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )"""
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/ui/MainMenuScreen.kt')
