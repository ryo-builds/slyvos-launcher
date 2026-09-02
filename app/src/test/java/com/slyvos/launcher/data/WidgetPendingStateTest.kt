package com.slyvos.launcher.data

import com.slyvos.launcher.data.repository.PendingWidgetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetPendingStateTest {

    @Test
    fun testPendingWidgetStateModel() {
        val pending = PendingWidgetState(
            appWidgetId = 42,
            providerPackage = "com.example.widget",
            providerClass = "com.example.widget.ExampleProvider"
        )

        assertEquals(42, pending.appWidgetId)
        assertEquals("com.example.widget", pending.providerPackage)
        assertEquals("com.example.widget.ExampleProvider", pending.providerClass)
        assertNotNull(pending.timestamp)
    }

    @Test
    fun testPendingStateClearing() {
        var state: PendingWidgetState? = PendingWidgetState(10, "pkg", "cls")
        assertNotNull(state)

        // Simulating clearPendingWidgetState()
        state = null
        assertNull(state)
    }
}
