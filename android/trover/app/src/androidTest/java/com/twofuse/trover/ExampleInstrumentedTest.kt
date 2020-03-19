package com.twofuse.trover

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.twofuse.trover.utils.KeyManager

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.twofuse.ratpatrol", appContext.packageName)
    }

    @Test
    fun testKeyManager() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        val keyManager = KeyManager.getKeyManager(appContext);

        val keyValue = keyManager.nextKeyValue
        assertNotNull(keyValue)
        assertNotNull(keyValue.key)
        assertNotNull(keyValue.value)
        val comparyKeyValue = keyManager.nextKeyValue
        assertNotEquals(keyValue.key, comparyKeyValue.key)
    }


}
