package com.silicon.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.ui.graphics.Color

object ToolsManager {

    fun getBurnFixColors(): List<Color> {
        return listOf(
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.White,
            Color.Black
        )
    }

    fun runVibrationTest(context: Context, durationMs: Long = 5000) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator

        if (vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }
}