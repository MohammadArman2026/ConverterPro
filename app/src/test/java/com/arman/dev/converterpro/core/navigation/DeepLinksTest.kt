package com.arman.dev.converterpro.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepLinksTest {

    @Test
    fun playerUriUsesCustomSchemeAndPlayerHost() {
        assertEquals("converterpro://player", DeepLinks.PLAYER_URI)
        assertTrue(DeepLinks.PLAYER_URI.startsWith("${DeepLinks.PLAYER_SCHEME}://"))
        assertTrue(DeepLinks.PLAYER_URI.endsWith(DeepLinks.PLAYER_HOST))
    }

    @Test
    fun playerDeepLinkStartsAtPlayerInsteadOfHome() {
        assertEquals(Routes.PLAYER, DeepLinks.startDestinationForUri(DeepLinks.PLAYER_URI))
    }

    @Test
    fun launcherAndUnknownUrisStartAtHome() {
        assertEquals(Routes.HOME, DeepLinks.startDestinationForUri(null))
        assertEquals(Routes.HOME, DeepLinks.startDestinationForUri("converterpro://files"))
    }

    @Test
    fun matchesPlayerUriIgnoresUnrelatedLinks() {
        assertTrue(DeepLinks.matchesPlayerUri(DeepLinks.PLAYER_URI))
        assertFalse(DeepLinks.matchesPlayerUri(null))
        assertFalse(DeepLinks.matchesPlayerUri("https://example.com"))
    }
}
