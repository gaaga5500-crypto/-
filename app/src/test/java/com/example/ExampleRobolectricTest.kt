package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DeviceType
import com.example.data.model.SmartDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Assistant AI", appName)
  }

  @Test
  fun `smart device state test`() {
    val device = SmartDevice(
      id = "test_lamp",
      name = "Desk Lamp",
      type = DeviceType.LIGHT,
      room = "Office",
      isOn = true,
      value = 75
    )
    assertTrue(device.isOn)
    assertEquals(75, device.value)
    assertEquals("Office", device.room)
  }
}
