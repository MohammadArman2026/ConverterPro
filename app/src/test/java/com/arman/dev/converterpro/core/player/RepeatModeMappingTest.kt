package com.arman.dev.converterpro.core.player

import androidx.media3.common.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class RepeatModeMappingTest {

    @Test
    fun mapsPlayerRepeatModesToApp() {
        assertEquals(RepeatMode.OFF, Player.REPEAT_MODE_OFF.toRepeatMode())
        assertEquals(RepeatMode.ONE, Player.REPEAT_MODE_ONE.toRepeatMode())
        assertEquals(RepeatMode.ALL, Player.REPEAT_MODE_ALL.toRepeatMode())
    }

    @Test
    fun mapsAppRepeatModesToPlayer() {
        assertEquals(Player.REPEAT_MODE_OFF, RepeatMode.OFF.toPlayerRepeatMode())
        assertEquals(Player.REPEAT_MODE_ONE, RepeatMode.ONE.toPlayerRepeatMode())
        assertEquals(Player.REPEAT_MODE_ALL, RepeatMode.ALL.toPlayerRepeatMode())
    }
}
