import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Replace 5.+ with 5.1.0
    content = content.replace('implementation("com.startapp:inapp-sdk:5.+")', 'implementation("com.startapp:inapp-sdk:5.1.0")')

    # Remove the resolutionStrategy block
    block_to_remove = """configurations.all {
    resolutionStrategy {
        force("com.startapp:inapp-sdk:5.1.0")
    }
}
"""
    content = content.replace(block_to_remove, "")
    
    # Also just in case there are no trailing newlines
    block_to_remove_2 = """configurations.all {
    resolutionStrategy {
        force("com.startapp:inapp-sdk:5.1.0")
    }
}"""
    content = content.replace(block_to_remove_2, "")

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/build.gradle.kts')
