package com.silicon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

object AppAnimations {
    private const val Duration = 300

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
}