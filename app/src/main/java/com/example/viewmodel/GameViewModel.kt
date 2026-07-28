package com.example.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import java.util.UUID

class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var lastTime = 0L

    init {
        // Idle initially
    }


    // Reward Tracking
    private var baseWinRewardCreditedRoundId: String? = null
    private var completed3XAds = 0
    private var activeRoundIdForAds: String? = null
    private var bonus3XCreditedRoundId: String? = null
    private var lossAdRewardCreditedRoundId: String? = null

    fun claimBaseWinReward(roundId: String): Boolean {
        if (baseWinRewardCreditedRoundId == roundId || bonus3XCreditedRoundId == roundId) return false
        baseWinRewardCreditedRoundId = roundId
        return true
    }

    fun getCompleted3XAds(roundId: String): Int {
        if (bonus3XCreditedRoundId == roundId) return 3 // Already fully claimed
        if (activeRoundIdForAds != roundId) return 0
        return completed3XAds
    }

    fun record3XAdCompletion(roundId: String): Int {
        if (activeRoundIdForAds != roundId) {
            activeRoundIdForAds = roundId
            completed3XAds = 0
        }
        completed3XAds++
        return completed3XAds
    }

    fun claim3XBonusReward(roundId: String): Boolean {
        if (bonus3XCreditedRoundId == roundId || baseWinRewardCreditedRoundId == roundId) return false
        bonus3XCreditedRoundId = roundId
        return true
    }

    fun hasClaimedLossReward(roundId: String): Boolean {
        return lossAdRewardCreditedRoundId == roundId
    }

    fun claimLossAdReward(roundId: String): Boolean {
        if (lossAdRewardCreditedRoundId == roundId) return false
        lossAdRewardCreditedRoundId = roundId
        return true
    }
    
    fun resetRewardState() {
        completed3XAds = 0
    }

    fun startNewGame() {
        val structure = generateRandomStructure()
        _gameState.update {
            it.copy(
                status = GameStatus.PLAYING,
                structure = structure,
                isFiring = false,
                particles = emptyList(),
                roundId = java.util.UUID.randomUUID().toString()
            )
        }
        resetRewardState()
        startGameLoop()
    }

    private fun isAngleBetween(angle: Float, start: Float, sweep: Float, padding: Float): Boolean {
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

    enum class Difficulty { EASY, MEDIUM, HARD }
    
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
                Difficulty.EASY -> 24f
                Difficulty.MEDIUM -> 18f
                Difficulty.HARD -> 12f // 8f laser width + 4f safety margin
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
    }

    fun setFiring(isFiring: Boolean) {
        if (_gameState.value.status != GameStatus.PLAYING) return
        _gameState.update { it.copy(isFiring = isFiring) }
    }

    fun pauseGame() {
        if (_gameState.value.status == GameStatus.PLAYING) {
            _gameState.update { it.copy(status = GameStatus.PAUSED, isFiring = false) }
            gameLoopJob?.cancel()
        }
    }

    fun resumeGame() {
        if (_gameState.value.status == GameStatus.PAUSED) {
            _gameState.update { it.copy(status = GameStatus.PLAYING) }
            startGameLoop()
        }
    }

    fun resetToMenu() {
        gameLoopJob?.cancel()
        _gameState.update { GameState(status = GameStatus.IDLE) }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        lastTime = System.currentTimeMillis()
        gameLoopJob = viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val dt = (currentTime - lastTime) / 1000f
                lastTime = currentTime

                updateGame(dt)
                delay(16) // ~60 FPS
            }
        }
    }

    private fun updateGame(dt: Float) {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return

        val structure = state.structure ?: return
        var gameOver = false
        var win = false
        var hitSegment: Segment? = null
        var hitRadius = 0f

        // Update rotations
        val updatedLayers = structure.layers.map { layer ->
            val rotDelta = layer.rotationSpeed * dt * (if (layer.isClockwise) 1 else -1)
            layer.copy(currentRotation = (layer.currentRotation + rotDelta) % 360f)
        }

        // Collision detection if firing
        if (state.isFiring) {
            val laserHitAngle = 90f 
            val laserAngularWidth = 8f
            val padding = laserAngularWidth / 2f
            
            for (layer in updatedLayers) {
                var localAngle = (laserHitAngle - layer.currentRotation) % 360f
                if (localAngle < 0) localAngle += 360f
                
                val segment = layer.segments.find { 
                    !it.isDestroyed && isAngleBetween(localAngle, it.startAngle, it.sweepAngle, padding) 
                }
                if (segment != null) {
                    hitSegment = segment
                    hitRadius = layer.radius
                    
                    if (segment.isDangerous) {
                        gameOver = true
                    } else {
                        // Damage segment
                        segment.health -= 250f * dt // Takes ~0.4s to break
                        if (segment.health <= 0) {
                            segment.isDestroyed = true
                        }
                    }
                    break // Laser blocked by this layer
                }
            }
        }

        // Check win condition (all breakable segments destroyed)
        if (!gameOver) {
            val allSafeDestroyed = updatedLayers.all { layer ->
                layer.segments.filter { !it.isDangerous }.all { it.isDestroyed }
            }
            if (allSafeDestroyed) {
                win = true
            }
        }

        // Update particles
        val newParticles = state.particles.map { it.copy(
            x = it.x + it.vx * dt,
            y = it.y + it.vy * dt,
            life = it.life - dt * 2f
        ) }.filter { it.life > 0 }.toMutableList()

        var newShakeX = 0f
        var newShakeY = 0f

        if (state.isFiring && hitSegment != null && !hitSegment.isDangerous) {
            newShakeX = Random.nextFloat() * 6f - 3f
            newShakeY = Random.nextFloat() * 6f - 3f
            // Spawn particles
            if (Random.nextFloat() < 0.5f) {
                // Calculate hit position
                // Assuming center is (0,0) here, we will translate in UI
                val rad = hitRadius
                val px = 0f
                val py = rad
                newParticles.add(Particle(
                    id = Random.nextInt(),
                    x = px + Random.nextFloat() * 20 - 10,
                    y = py + Random.nextFloat() * 20 - 10,
                    vx = Random.nextFloat() * 100 - 50,
                    vy = Random.nextFloat() * 50 + 50,
                    life = 1f,
                    color = structure.colorTheme.safeColorGlow
                ))
            }
        }

        if (gameOver) {
            _gameState.update {
                it.copy(
                    status = GameStatus.LOST,
                    structure = structure.copy(layers = updatedLayers),
                    particles = newParticles,
                    isFiring = false
                )
            }
            gameLoopJob?.cancel()
        } else if (win) {
             _gameState.update {
                it.copy(
                    status = GameStatus.WON,
                    structure = structure.copy(layers = updatedLayers),
                    particles = newParticles,
                    isFiring = false
                )
            }
            gameLoopJob?.cancel()
        } else {
             _gameState.update {
                it.copy(
                    structure = structure.copy(layers = updatedLayers),
                    particles = newParticles,
                    laserTipY = hitRadius, // if 0, it shoots through to center
                    shakeOffsetX = newShakeX,
                    shakeOffsetY = newShakeY
                )
            }
        }
    }
}
