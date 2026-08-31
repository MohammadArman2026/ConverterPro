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
    fun playerDeepLinkBackStackIsHomeThenFilesThenPlayer() {
        assertEquals(
            listOf(Routes.HOME, Routes.FILES, Routes.PLAYER),
            DeepLinks.PLAYER_BACK_STACK
        )
    }

    @Test
    fun graphAlwaysStartsAtHome() {
        assertEquals(Routes.HOME, DeepLinks.startDestinationForUri(DeepLinks.PLAYER_URI))
        assertEquals(Routes.HOME, DeepLinks.startDestinationForUri(null))
    }

    @Test
    fun matchesPlayerUriIgnoresUnrelatedLinks() {
        assertTrue(DeepLinks.matchesPlayerUri(DeepLinks.PLAYER_URI))
        assertFalse(DeepLinks.matchesPlayerUri(null))
        assertFalse(DeepLinks.matchesPlayerUri("https://example.com"))
    }
}
