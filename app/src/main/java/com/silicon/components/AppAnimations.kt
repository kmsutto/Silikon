package com.silicon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntSize

object AppAnimations {
    private const val Duration = 300
    private const val CrossfadeDuration = 250

    val crossfadeSpec = tween<Float>(CrossfadeDuration)
    val contentSizeSpec = tween<IntSize>(Duration)

    fun enterTransition(targetIndex: Int, initialIndex: Int): EnterTransition {
        val direction = if (targetIndex > initialIndex) 1 else -1
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> direction * (fullWidth / 8) },
            animationSpec = tween(Duration, easing = FastOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(Duration, easing = FastOutSlowInEasing)
        )
    }

    fun exitTransition(targetIndex: Int, initialIndex: Int): ExitTransition {
        val direction = if (targetIndex > initialIndex) 1 else -1
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -direction * (fullWidth / 8) },
            animationSpec = tween(Duration, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(Duration / 2)
        )
    }

    fun tabTransition(targetIndex: Int, initialIndex: Int): ContentTransform {
        val direction = if (targetIndex > initialIndex) 1 else -1
        return (slideInHorizontally(
            initialOffsetX = { width -> direction * width },
            animationSpec = tween(Duration, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(Duration))) togetherWith (
                slideOutHorizontally(
                    targetOffsetX = { width -> -direction * width },
                    animationSpec = tween(Duration, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(Duration))
                )
    }
}