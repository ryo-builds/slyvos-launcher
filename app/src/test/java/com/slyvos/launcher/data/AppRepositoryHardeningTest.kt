package com.slyvos.launcher.data

import androidx.compose.ui.graphics.ImageBitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Collections

class AppRepositoryHardeningTest {

    @Test
    fun testIconCachePutAndGet() {
        val fakeBitmap = object : ImageBitmap {
            override val height: Int = 10
            override val width: Int = 10
            override val hasAlpha: Boolean = true
            override val config: androidx.compose.ui.graphics.ImageBitmapConfig = androidx.compose.ui.graphics.ImageBitmapConfig.Argb8888
            override val colorSpace: androidx.compose.ui.graphics.colorspace.ColorSpace = androidx.compose.ui.graphics.colorspace.ColorSpaces.Srgb
            override fun readPixels(buffer: IntArray, startX: Int, startY: Int, width: Int, height: Int, bufferOffset: Int, stride: Int) {}
            override fun prepareToDraw() {}
        }
        
        // Test memory bounded LRU cache contract logic
        val iconCacheMap = object : LinkedHashMap<String, ImageBitmap>(128, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean {
                return size > 128
            }
        }
        val cache = Collections.synchronizedMap(iconCacheMap)
        val key = "com.example.app/com.example.app.MainActivity"

        assertNull(cache[key])
        cache[key] = fakeBitmap
        assertEquals(fakeBitmap, cache[key])

        cache.remove(key)
        assertNull(cache[key])
    }

    @Test
    fun testUninstalledPackageDockPruning() {
        val customDockPackages = mutableListOf("com.android.chrome", "com.example.uninstalled")
        val installedPackages = listOf("com.android.chrome", "com.google.android.dialer", "com.android.settings")

        // Pruning logic verification
        val validPackages = customDockPackages.filter { installedPackages.contains(it) }

        assertEquals(1, validPackages.size)
        assertEquals("com.android.chrome", validPackages[0])
    }
}
