package id.maripartner.fuel.core.common.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStateTest {

    @Test
    fun uiState_distinguishesStatesCorrectly() {
        val idle: UiState<String> = UiState.Idle
        val loading: UiState<String> = UiState.Loading
        val success: UiState<String> = UiState.Success("Data loaded")
        val error: UiState<String> = UiState.Error("Network failure")
        val empty: UiState<String> = UiState.Empty("No items")

        assertTrue(idle is UiState.Idle)
        assertTrue(loading is UiState.Loading)
        assertTrue(success is UiState.Success)
        assertEquals("Data loaded", (success as UiState.Success).data)
        assertTrue(error is UiState.Error)
        assertEquals("Network failure", (error as UiState.Error).message)
        assertTrue(empty is UiState.Empty)
    }

    @Test
    fun appResult_handlesSuccessAndError() {
        val successResult = AppResult.Success(42)
        val errorResult = AppResult.Error(AppException.Validation("Invalid nominal"))

        assertTrue(successResult.isSuccess)
        assertEquals(42, successResult.getOrNull())

        assertFalse(errorResult.isSuccess)
        assertNull(errorResult.getOrNull())
        assertEquals("Invalid nominal", errorResult.exception.message)
    }
}
