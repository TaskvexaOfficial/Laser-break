import sys

with open('app/src/main/java/com/example/viewmodel/GameViewModel.kt', 'r') as f:
    lines = f.readlines()

# Find newParticles initialization
new_particles_idx = -1
for i, line in enumerate(lines):
    if "val newParticles = state.particles.map" in line:
        new_particles_idx = i
        break

if new_particles_idx != -1:
    # Extract the block
    block = lines[new_particles_idx : new_particles_idx + 8]
    # Remove it from original position
    del lines[new_particles_idx : new_particles_idx + 8]
    
    # Find where to insert it (before 'if (state.isFiring) {')
    insert_idx = -1
    for i, line in enumerate(lines):
        if "if (state.isFiring) {" in line:
            insert_idx = i
            break
            
    if insert_idx != -1:
        # Insert a comment and the block
        lines.insert(insert_idx, "        // Update particles first so collision can add to it\n")
        for line in reversed(block):
            lines.insert(insert_idx + 1, line)
            
        with open('app/src/main/java/com/example/viewmodel/GameViewModel.kt', 'w') as f:
            f.writelines(lines)
        print("Moved newParticles initialization")
else:
    print("newParticles not found")
