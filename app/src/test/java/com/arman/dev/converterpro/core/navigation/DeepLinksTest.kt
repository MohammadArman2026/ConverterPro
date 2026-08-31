package com.arman.dev.converterpro.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepLinksTest {

    @Test
    fun playerUriUsesCustomSchemeAndPlayerHost() {
        assertEquals("converterpro://player", DeepLinks.PLAYER_URI)
        assertTrue(DeepLinks.PLAYER_URI.startsWith("${DeepLinks.PLAYER_SCHEME}://"))
        assertTrue(DeepLinks.PLAYER_URI.endsWith(DeepLinks.PLAYER_HOST))
    }
}
