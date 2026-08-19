package com.kaz229.fittrack

import com.kaz229.fittrack.data.Activities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivitiesTest {

    @Test
    fun `calories follow the MET formula`() {
        val running = Activities.byId("course") // MET 9.8
        // 9.8 * 3.5 * 80 / 200 * 30 = 411.6 -> 412
        assertEquals(412, Activities.caloriesBurned(running, durationMinutes = 30, weightKg = 80.0))
    }

    @Test
    fun `heavier user burns more for the same session`() {
        val football = Activities.byId("football")
        val light = Activities.caloriesBurned(football, 60, 60.0)
        val heavy = Activities.caloriesBurned(football, 60, 90.0)
        assertTrue(heavy > light)
    }

    @Test
    fun `unknown activity falls back to the generic one`() {
        assertEquals("autre", Activities.byId("inconnu").id)
    }
}
