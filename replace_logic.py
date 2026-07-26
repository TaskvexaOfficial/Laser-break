import re

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "r") as f:
    content = f.read()

# Replace generateRandomStructure
old_generate = """    private fun generateRandomStructure(): Structure {
        val type = StructureType.values().random()
        val numLayers = Random.nextInt(2, 5)
        val colorTheme = AllThemes.random()
        
        val layers = mutableListOf<Layer>()
        var currentRadius = 250f // Outer radius
        val layerThickness = 45f
        val gap = 15f
        
        for (i in 0 until numLayers) {
            val numSegments = when (type) {
                StructureType.SEGMENTED_CIRCLE -> Random.nextInt(2, 9)
                StructureType.ROTATING_HEXAGON -> 6
                StructureType.TRIANGULAR_LAYERS -> 3
                StructureType.SQUARE_LAYERS -> 4
            }
            val segments = mutableListOf<Segment>()
            val segmentAngle = 360f / numSegments
            
            // Ensure at least one safe segment per layer
            val safeIndex = Random.nextInt(numSegments)
            for (j in 0 until numSegments) {
                val isDangerous = if (j == safeIndex) false else Random.nextFloat() < 0.25f // ~25% dangerous
                
                val start = j * segmentAngle
                // add a small visual gap between segments by reducing sweep slightly
                val sweep = segmentAngle - 4f
                
                segments.add(
                    Segment(
                        id = i * 100 + j,
                        layerIndex = i,
                        startAngle = start + 2f,
                        sweepAngle = sweep,
                        isDangerous = isDangerous
                    )
                )
            }
            
            layers.add(
                Layer(
                    index = i,
                    radius = currentRadius,
                    thickness = layerThickness,
                    segments = segments,
                    currentRotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 40f + 30f,
                    isClockwise = Random.nextBoolean()
                )
            )
            
            currentRadius -= (layerThickness + gap)
        }
        
        return Structure(
            type = type,
            layers = layers,
            colorTheme = colorTheme
        )
    }"""

new_generate = """    private fun isAngleBetween(angle: Float, start: Float, sweep: Float, padding: Float): Boolean {
        var a = angle % 360f
        if (a < 0) a += 360f
        val s = (start - padding) % 360f
        val sNorm = if (s < 0) s + 360f else s
        val e = (start + sweep + padding) % 360f
        val eNorm = if (e < 0) e + 360f else e
        
        return if (sNorm <= eNorm) {
            a in sNorm..eNorm
        } else {
            a >= sNorm || a <= eNorm
        }
    }

    private fun getOutermostHit(
        layers: List<Layer>,
        time: Float,
        laserAngularWidth: Float,
        brokenSegments: Set<Int>
    ): Segment? {
        val laserHitAngle = 90f
        val padding = laserAngularWidth / 2f
        
        for (layer in layers) {
            val rotDelta = layer.rotationSpeed * time * (if (layer.isClockwise) 1 else -1)
            var currentRot = (layer.currentRotation + rotDelta) % 360f
            if (currentRot < 0) currentRot += 360f
            
            var localAngle = (laserHitAngle - currentRot) % 360f
            if (localAngle < 0) localAngle += 360f
            
            val hit = layer.segments.find { 
                !brokenSegments.contains(it.id) && isAngleBetween(localAngle, it.startAngle, it.sweepAngle, padding) 
            }
            if (hit != null) return hit
        }
        return null
    }

    private fun isStructureSolvable(structure: Structure): Boolean {
        val allSafeSegments = structure.layers.flatMap { it.segments }.filter { !it.isDangerous }.map { it.id }.toSet()
        val brokenSegments = mutableSetOf<Int>()
        
        val maxTime = 45f
        val dt = 0.05f
        val breakTime = 0.4f
        val reactionTime = 0.25f
        val laserAngularWidth = 8f
        
        var time = 0f
        while (time < maxTime) {
            if (brokenSegments.size == allSafeSegments.size) return true
            
            val hitStart = getOutermostHit(structure.layers, time, laserAngularWidth, brokenSegments)
            if (hitStart != null && !hitStart.isDangerous) {
                var safeToHold = true
                var segmentRemains = true
                
                var tHold = time + dt
                while (tHold <= time + breakTime) {
                    val hit = getOutermostHit(structure.layers, tHold, laserAngularWidth, brokenSegments)
                    if (hit?.id != hitStart.id) {
                        segmentRemains = false
                        break
                    }
                    tHold += dt
                }
                
                if (segmentRemains) {
                    val testBroken = brokenSegments.toMutableSet().apply { add(hitStart.id) }
                    var tReaction = time + breakTime + dt
                    while (tReaction <= time + breakTime + reactionTime) {
                        val hitAfter = getOutermostHit(structure.layers, tReaction, laserAngularWidth, testBroken)
                        if (hitAfter != null && hitAfter.isDangerous) {
                            safeToHold = false
                            break
                        }
                        tReaction += dt
                    }
                    
                    if (safeToHold) {
                        brokenSegments.add(hitStart.id)
                        time += breakTime
                    }
                }
            }
            time += dt
        }
        
        return brokenSegments.size == allSafeSegments.size
    }

    private fun createRandomStructure(attempt: Int): Structure {
        val type = StructureType.values().random()
        val numLayers = Random.nextInt(2, 5)
        val colorTheme = AllThemes.random()
        
        val layers = mutableListOf<Layer>()
        var currentRadius = 250f
        val layerThickness = 45f
        val gap = 15f
        
        for (i in 0 until numLayers) {
            val numSegments = when (type) {
                StructureType.SEGMENTED_CIRCLE -> Random.nextInt(2, 9)
                StructureType.ROTATING_HEXAGON -> 6
                StructureType.TRIANGULAR_LAYERS -> 3
                StructureType.SQUARE_LAYERS -> 4
            }
            val segments = mutableListOf<Segment>()
            val segmentAngle = 360f / numSegments
            
            val safeIndex = Random.nextInt(numSegments)
            for (j in 0 until numSegments) {
                val dangerChance = if (attempt > 25) 0.15f else 0.25f
                val isDangerous = if (j == safeIndex) false else Random.nextFloat() < dangerChance
                
                val start = j * segmentAngle
                val sweep = segmentAngle - 4f
                
                segments.add(
                    Segment(
                        id = i * 100 + j,
                        layerIndex = i,
                        startAngle = start + 2f,
                        sweepAngle = sweep,
                        isDangerous = isDangerous
                    )
                )
            }
            
            // Random rotation speed but avoid exactly matching speeds to ensure changing alignments
            val speed = Random.nextFloat() * 40f + 30f + (i * 5f) 
            layers.add(
                Layer(
                    index = i,
                    radius = currentRadius,
                    thickness = layerThickness,
                    segments = segments,
                    currentRotation = Random.nextFloat() * 360f,
                    rotationSpeed = speed,
                    isClockwise = Random.nextBoolean()
                )
            )
            currentRadius -= (layerThickness + gap)
        }
        return Structure(type, layers, colorTheme)
    }

    private fun generateRandomStructure(): Structure {
        for (attempt in 1..50) {
            val structure = createRandomStructure(attempt)
            if (isStructureSolvable(structure)) {
                return structure
            }
        }
        // Fallback: A guaranteed safe level (1 layer, 1 safe segment)
        val safeSegments = listOf(Segment(0, 0, 0f, 180f, false))
        val safeLayer = Layer(0, 250f, 45f, safeSegments, 0f, 30f, true)
        return Structure(StructureType.SEGMENTED_CIRCLE, listOf(safeLayer), AllThemes.random())
    }"""

content = content.replace(old_generate, new_generate)


old_hit = """        // Collision detection if firing
        if (state.isFiring) {
            // Laser hits at angle 90 (straight down)
            val laserHitAngle = 90f 
            
            // Find the outermost layer that has an active segment at angle 90
            for (layer in updatedLayers) {
                // Find effective angle on this layer
                // If layer is rotated by currentRotation, the segment at 90 on screen 
                // is at (90 - currentRotation) in layer's local coordinates.
                var localAngle = (laserHitAngle - layer.currentRotation) % 360f
                if (localAngle < 0) localAngle += 360f
                
                // Find segment
                val segment = layer.segments.find { !it.isDestroyed && localAngle >= it.startAngle && localAngle <= (it.startAngle + it.sweepAngle) }"""

new_hit = """        // Collision detection if firing
        if (state.isFiring) {
            val laserHitAngle = 90f 
            val laserAngularWidth = 8f
            val padding = laserAngularWidth / 2f
            
            // Find the outermost layer that has an active segment at angle 90
            for (layer in updatedLayers) {
                var localAngle = (laserHitAngle - layer.currentRotation) % 360f
                if (localAngle < 0) localAngle += 360f
                
                // Find segment with laser width padding
                val segment = layer.segments.find { !it.isDestroyed && isAngleBetween(localAngle, it.startAngle, it.sweepAngle, padding) }"""

content = content.replace(old_hit, new_hit)

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "w") as f:
    f.write(content)

