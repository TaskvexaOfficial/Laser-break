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

class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var lastTime = 0L

    init {
        // Idle initially
    }

    fun startNewGame() {
        val structure = generateRandomStructure()
        _gameState.update {
            it.copy(
                status = GameStatus.PLAYING,
                structure = structure,
                isFiring = false,
                particles = emptyList()
            )
        }
        startGameLoop()
    }

    private fun generateRandomStructure(): Structure {
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
                val segment = layer.segments.find { !it.isDestroyed && localAngle >= it.startAngle && localAngle <= (it.startAngle + it.sweepAngle) }
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
