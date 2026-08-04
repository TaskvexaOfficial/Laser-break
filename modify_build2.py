import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    content = content.replace(
        "implementation(\"com.google.android.ump:user-messaging-platform:3.1.0\")",
        "implementation(\"com.google.android.ump:user-messaging-platform:3.1.0\")\n  implementation(\"com.startapp:inapp-sdk:5.+\")"
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/build.gradle.kts')
