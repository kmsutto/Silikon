package com.silicon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

object AppAnimations {

    private const val ANIM_DURATION = 350

    fun enterTransition(targetIndex: Int, initialIndex: Int): EnterTransition {
        val direction = if (targetIndex > initialIndex) 1 else -1

        return slideInHorizontally(
            initialOffsetX = { fullWidth -> direction * fullWidth },
            animationSpec = tween(ANIM_DURATION)
        ) + fadeIn(animationSpec = tween(ANIM_DURATION))
    }

    fun exitTransition(targetIndex: Int, initialIndex: Int): ExitTransition {
        val direction = if (targetIndex > initialIndex) 1 else -1

        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -direction * fullWidth },
            animationSpec = tween(ANIM_DURATION)
        ) + fadeOut(animationSpec = tween(ANIM_DURATION))
    }
}