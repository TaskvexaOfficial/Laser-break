import re

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "r") as f:
    content = f.read()

# Replace generateRandomStructure and createRandomStructure
old_code = """    private fun createRandomStructure(attempt: Int): Structure {
        val type = if (Random.nextBoolean()) StructureType.SEGMENTED_CIRCLE else StructureType.SQUARE_LAYERS
        val numLayers = Random.nextInt(2, 4) // 2 or 3 layers
        val colorTheme = AllThemes.random()
        
        val layers = mutableListOf<Layer>()
        var currentRadius = 250f
        val layerThickness = 45f
        val gap = 15f
        
        for (i in 0 until numLayers) {
            val numSegments = when (type) {
                StructureType.SEGMENTED_CIRCLE -> Random.nextInt(2, 5) // 2 to 4 segments
                StructureType.SQUARE_LAYERS -> 4
            }
            val segments = mutableListOf<Segment>()
            val segmentAngle = 360f / numSegments
            
            val safeIndex = Random.nextInt(numSegments)
            for (j in 0 until numSegments) {
                val dangerChance = 0.15f // very low danger chance
                val isDangerous = if (j == safeIndex) false else Random.nextFloat() < dangerChance
                
                val visualGap = 12f // large visual gap
                val start = j * segmentAngle + (visualGap / 2f)
                val sweep = segmentAngle - visualGap
                
                segments.add(
                    Segment(
                        id = i * 100 + j,
                        layerIndex = i,
                        startAngle = start,
                        sweepAngle = sweep,
                        isDangerous = isDangerous
                    )
                )
            }
            
            val speed = Random.nextFloat() * 20f + 15f + (i * 2f) // Slower speeds
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
        val safeSegments = listOf(Segment(0, 0, 0f, 180f, false))
        val safeLayer = Layer(0, 250f, 45f, safeSegments, 0f, 30f, true)
        return Structure(StructureType.SEGMENTED_CIRCLE, listOf(safeLayer), AllThemes.random())
    }"""


new_code = """    enum class Difficulty { EASY, MEDIUM, HARD }
    
    private var consecutiveHardRounds = 0

    private fun getNextDifficulty(): Difficulty {
        val roll = Random.nextFloat()
        if (roll < 0.4f) {
            consecutiveHardRounds = 0
            return Difficulty.EASY
        } else if (roll < 0.8f) {
            consecutiveHardRounds = 0
            return Difficulty.MEDIUM
        } else {
            if (consecutiveHardRounds >= 2) {
                consecutiveHardRounds = 0
                return if (Random.nextBoolean()) Difficulty.EASY else Difficulty.MEDIUM
            }
            consecutiveHardRounds++
            return Difficulty.HARD
        }
    }

    private fun createRandomStructure(difficulty: Difficulty): Structure {
        val type = if (Random.nextBoolean()) StructureType.SEGMENTED_CIRCLE else StructureType.SQUARE_LAYERS
        val numLayers = when (difficulty) {
            Difficulty.EASY -> Random.nextInt(2, 4)
            Difficulty.MEDIUM -> Random.nextInt(2, 4)
            Difficulty.HARD -> Random.nextInt(3, 5)
        }
        val colorTheme = AllThemes.random()
        
        val layers = mutableListOf<Layer>()
        var currentRadius = 250f
        val layerThickness = 45f
        
        for (i in 0 until numLayers) {
            val numSegments = when (type) {
                StructureType.SEGMENTED_CIRCLE -> when (difficulty) {
                    Difficulty.EASY -> Random.nextInt(2, 5)
                    Difficulty.MEDIUM -> Random.nextInt(3, 6)
                    Difficulty.HARD -> Random.nextInt(4, 7)
                }
                StructureType.SQUARE_LAYERS -> 4
            }
            val segments = mutableListOf<Segment>()
            val segmentAngle = 360f / numSegments
            
            val safeIndex = Random.nextInt(numSegments)
            
            val visualGap = when (difficulty) {
                Difficulty.EASY -> 16f
                Difficulty.MEDIUM -> 12f
                Difficulty.HARD -> 8f
            }
            val gap = when (difficulty) {
                Difficulty.EASY -> 20f
                Difficulty.MEDIUM -> 15f
                Difficulty.HARD -> 12f
            }
            
            for (j in 0 until numSegments) {
                val dangerChance = when (difficulty) {
                    Difficulty.EASY -> 0.15f
                    Difficulty.MEDIUM -> 0.35f
                    Difficulty.HARD -> 0.50f
                }
                
                val isDangerous = if (j == safeIndex) false else Random.nextFloat() < dangerChance
                
                val start = j * segmentAngle + (visualGap / 2f)
                val sweep = segmentAngle - visualGap
                
                segments.add(
                    Segment(
                        id = i * 100 + j,
                        layerIndex = i,
                        startAngle = start,
                        sweepAngle = sweep,
                        isDangerous = isDangerous
                    )
                )
            }
            
            val speed = when (difficulty) {
                Difficulty.EASY -> Random.nextFloat() * 15f + 15f + (i * 2f)
                Difficulty.MEDIUM -> Random.nextFloat() * 25f + 25f + (i * 4f)
                Difficulty.HARD -> Random.nextFloat() * 40f + 40f + (i * 6f)
            }
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
        
        // Ensure at least one danger section exists
        val hasDanger = layers.any { layer -> layer.segments.any { it.isDangerous } }
        if (!hasDanger && layers.isNotEmpty()) {
            val layer = layers.random()
            if (layer.segments.size > 1) {
                val candidateSegments = layer.segments.filter { !it.isDangerous }
                if (candidateSegments.isNotEmpty()) {
                    candidateSegments.random().isDangerous = true
                }
            }
        }
        
        return Structure(type, layers, colorTheme)
    }

    private fun generateRandomStructure(): Structure {
        val difficulty = getNextDifficulty()
        for (attempt in 1..50) {
            val structure = createRandomStructure(difficulty)
            if (isStructureSolvable(structure)) {
                return structure
            }
        }
        // Fallback: A guaranteed safe level but still has danger to satisfy "Black danger sections must appear in every normal playable round"
        val safeSegments = listOf(
            Segment(0, 0, 10f, 160f, false),
            Segment(1, 0, 190f, 160f, true) // Add one danger section to fallback
        )
        val safeLayer = Layer(0, 250f, 45f, safeSegments, 0f, 30f, true)
        return Structure(StructureType.SEGMENTED_CIRCLE, listOf(safeLayer), AllThemes.random())
    }"""

content = content.replace(old_code, new_code)

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "w") as f:
    f.write(content)

