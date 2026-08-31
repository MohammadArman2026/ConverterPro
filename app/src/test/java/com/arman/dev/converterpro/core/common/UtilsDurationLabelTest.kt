package com.arman.dev.converterpro.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsDurationLabelTest {

    @Test
    fun formatsShortClipsInSeconds() {
        assertEquals("0 sec", Utils.formatDurationLabel(0L))
        assertEquals("45 sec", Utils.formatDurationLabel(45_000L))
    }

    @Test
    fun formatsMinutesAndRemainingSeconds() {
        assertEquals("2 min", Utils.formatDurationLabel(120_000L))
        assertEquals("2 min 5 sec", Utils.formatDurationLabel(125_000L))
    }

    @Test
    fun formatsHoursMinutesAndSeconds() {
        assertEquals("1 hr", Utils.formatDurationLabel(3_600_000L))
        assertEquals("1 hr 2 min", Utils.formatDurationLabel(3_720_000L))
        assertEquals("1 hr 2 min 5 sec", Utils.formatDurationLabel(3_725_000L))
    }
}
