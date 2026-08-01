import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Add UMP dependency
    content = content.replace(
        "  implementation(libs.play.services.ads)",
        "  implementation(libs.play.services.ads)\n  implementation(\"com.google.android.ump:user-messaging-platform:3.1.0\")"
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/build.gradle.kts')
