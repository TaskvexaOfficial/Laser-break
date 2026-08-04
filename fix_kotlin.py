import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Revert compileSdk to 36
    content = content.replace("compileSdk = 37", "compileSdk { version = release(36) { minorApiLevel = 1 } }")

    # Add resolution strategy to force kotlin-stdlib
    strategy = """
configurations.all {
    resolutionStrategy {
        force("com.startapp:inapp-sdk:5.1.0")
    }
}
"""
    if "resolutionStrategy" not in content:
        content += strategy

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/build.gradle.kts')
