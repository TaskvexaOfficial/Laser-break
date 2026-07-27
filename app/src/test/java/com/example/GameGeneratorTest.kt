package com.example

import com.example.viewmodel.GameViewModel
import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class GameGeneratorTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun testGenerator1000() {
        val vm = GameViewModel()
        
        var hasDangerCount = 0
        
        for (i in 0..1000) {
            vm.startNewGame()
            val structure = vm.gameState.value.structure
            assertNotNull(structure)
            
            // Check that type is only SEGMENTED_CIRCLE or SQUARE_LAYERS
            assertTrue(structure!!.type == com.example.model.StructureType.SEGMENTED_CIRCLE || structure.type == com.example.model.StructureType.SQUARE_LAYERS)
            
            val hasDanger = structure.layers.any { layer -> layer.segments.any { it.isDangerous } }
            if (hasDanger) {
                hasDangerCount++
            }
        }
        
        // Every normal playable round must have black danger sections
        assertTrue("Danger sections must appear in almost all rounds", hasDangerCount == 1001)
    }
}
