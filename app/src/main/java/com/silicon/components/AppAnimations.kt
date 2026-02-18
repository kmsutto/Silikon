package com.silicon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

object AppAnimations {

    private val motionSpec = spring<IntOffset>(
        stiffness = 380f,
        dampingRatio = 0.75f
    )

    private val scaleSpec = spring<Float>(
        stiffness = 380f,
        dampingRatio = 0.75f
    )

    fun enterTransition(targetIndex: Int, initialIndex: Int): EnterTransition {
        val direction = if (targetIndex > initialIndex) 1 else -1
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> direction * fullWidth },
            animationSpec = motionSpec
        ) + fadeIn(
            animationSpec = tween(durationMillis = 400)
        ) + scaleIn(
            initialScale = 0.96f,
            animationSpec = scaleSpec
        )
    }

    fun exitTransition(targetIndex: Int, initialIndex: Int): ExitTransition {
        val direction = if (targetIndex > initialIndex) 1 else -1
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -direction * (fullWidth / 4) },
            animationSpec = motionSpec
        ) + fadeOut(
            animationSpec = tween(durationMillis = 400)
        ) + scaleOut(
            targetScale = 0.96f,
            animationSpec = scaleSpec
        )
    }
}