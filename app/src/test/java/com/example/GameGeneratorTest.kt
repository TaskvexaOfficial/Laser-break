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
    fun testGenerator() {
        // Can't easily test private methods, but we can call startNewGame and check state
        val vm = GameViewModel()
        vm.startNewGame()
        assertNotNull(vm.gameState.value.structure)
        
        // Loop and test many layouts
        for (i in 0..100) {
            vm.startNewGame()
            assertNotNull(vm.gameState.value.structure)
        }
    }
}
