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
        
        for (i in 0..1000) {
            vm.startNewGame()
            val structure = vm.gameState.value.structure
            assertNotNull(structure)
            
            // Check that type is only SEGMENTED_CIRCLE or SQUARE_LAYERS
            assertTrue(structure!!.type == com.example.model.StructureType.SEGMENTED_CIRCLE || structure.type == com.example.model.StructureType.SQUARE_LAYERS)
        }
    }
}
