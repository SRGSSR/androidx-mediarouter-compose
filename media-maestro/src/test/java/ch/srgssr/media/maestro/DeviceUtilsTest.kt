/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSensor
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class DeviceUtilsTest {
    private lateinit var context: Context

    @BeforeTest
    fun before() {
        Locale.setDefault(Locale.ENGLISH)

        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `getDialogChooserWifiWarningDescription phone`() {
        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this phone",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }

    @Test
    @Config(qualifiers = "xlarge")
    fun `getDialogChooserWifiWarningDescription tablet`() {
        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this tablet",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `getDialogChooserWifiWarningDescription foldable pre-R`() {
        val sensorManager = context.getSystemService<SensorManager>()
        val shadowSensorManager = shadowOf(sensorManager)
        shadowSensorManager.addSensor(ShadowSensor.newInstance(Sensor.TYPE_HINGE_ANGLE))

        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this phone",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `getDialogChooserWifiWarningDescription foldable`() {
        val sensorManager = context.getSystemService<SensorManager>()
        val shadowSensorManager = shadowOf(sensorManager)
        shadowSensorManager.addSensor(ShadowSensor.newInstance(Sensor.TYPE_HINGE_ANGLE))

        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this phone",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }

    @Test
    @Config(qualifiers = "sw600dp-large")
    fun `getDialogChooserWifiWarningDescription 7 inches tablet`() {
        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this tablet",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }

    @Test
    fun `getDialogChooserWifiWarningDescription wearable`() {
        val packageManger = context.packageManager
        val shadowPackageManager = shadowOf(packageManger)
        shadowPackageManager.setSystemFeature(PackageManager.FEATURE_WATCH, true)

        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this watch",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1])
    fun `getDialogChooserWifiWarningDescription auto pre-O`() {
        val packageManger = context.packageManager
        val shadowPackageManager = shadowOf(packageManger)
        shadowPackageManager.setSystemFeature(PackageManager.FEATURE_AUTOMOTIVE, true)

        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this phone",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `getDialogChooserWifiWarningDescription auto`() {
        val packageManger = context.packageManager
        val shadowPackageManager = shadowOf(packageManger)
        shadowPackageManager.setSystemFeature(PackageManager.FEATURE_AUTOMOTIVE, true)

        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this car",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }

    @Test
    fun `getDialogChooserWifiWarningDescription TV 1`() {
        val packageManger = context.packageManager
        val shadowPackageManager = shadowOf(packageManger)
        shadowPackageManager.setSystemFeature("com.google.android.tv", true)

        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this tv",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }

    @Test
    fun `getDialogChooserWifiWarningDescription TV 2`() {
        val packageManger = context.packageManager
        val shadowPackageManager = shadowOf(packageManger)
        @Suppress("DEPRECATION")
        shadowPackageManager.setSystemFeature(PackageManager.FEATURE_TELEVISION, true)

        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this tv",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }

    @Test
    fun `getDialogChooserWifiWarningDescription TV 3`() {
        val packageManger = context.packageManager
        val shadowPackageManager = shadowOf(packageManger)
        shadowPackageManager.setSystemFeature(PackageManager.FEATURE_LEANBACK, true)

        assertEquals(
            "Make sure the other device is on the same Wi-Fi network as this tv",
            DeviceUtils.getDialogChooserWifiWarningDescription(context)
        )
    }
}
