package ch.srgssr.androidx.mediarouter.compose

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
import android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.Dimension
import androidx.core.content.getSystemService
import androidx.mediarouter.R

// Kotlin port of MediaRouter internal DeviceUtils class.
internal object DeviceUtils {
    private const val FEATURE_TV_1 = "com.google.android.tv"

    @Dimension(unit = Dimension.DP)
    private const val SEVEN_INCH_TABLET_MINIMUM_SCREEN_WIDTH_DP = 600

    fun getDialogChooserWifiWarningDescription(context: Context): String {
        return if (isPhone(context) || isFoldable(context)) {
            context.getString(R.string.mr_chooser_wifi_warning_description_phone)
        } else if (isTablet(context) || isSevenInchTablet(context)) {
            context.getString(R.string.mr_chooser_wifi_warning_description_tablet)
        } else if (isTv(context)) {
            context.getString(R.string.mr_chooser_wifi_warning_description_tv)
        } else if (isWearable(context)) {
            context.getString(R.string.mr_chooser_wifi_warning_description_watch)
        } else if (isAuto(context)) {
            context.getString(R.string.mr_chooser_wifi_warning_description_car)
        } else {
            context.getString(R.string.mr_chooser_wifi_warning_description_unknown)
        }
    }

    private fun isPhone(context: Context): Boolean {
        return !isTablet(context) &&
                !isWearable(context) &&
                !isAuto(context) &&
                !isTv(context)
    }

    private fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        val screenLayoutSize = configuration.screenLayout and SCREENLAYOUT_SIZE_MASK
        val isXLarge = screenLayoutSize > SCREENLAYOUT_SIZE_LARGE

        return isXLarge || isSevenInchTablet(context)
    }

    private fun isFoldable(context: Context): Boolean {
        val sensorManager = context.getSystemService<SensorManager>()

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                sensorManager?.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE) != null
    }

    private fun isSevenInchTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        val screenLayoutSize = configuration.screenLayout and SCREENLAYOUT_SIZE_MASK

        return screenLayoutSize <= SCREENLAYOUT_SIZE_LARGE &&
                configuration.smallestScreenWidthDp >= SEVEN_INCH_TABLET_MINIMUM_SCREEN_WIDTH_DP
    }

    private fun isWearable(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
    }

    private fun isAuto(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
    }

    private fun isTv(context: Context): Boolean {
        val packageManager = context.packageManager

        @Suppress("DEPRECATION")
        return packageManager.hasSystemFeature(FEATURE_TV_1) ||
                packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION) ||
                packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }
}
