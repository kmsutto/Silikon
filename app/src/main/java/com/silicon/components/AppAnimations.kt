package com.silicon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

object AppAnimations {

    private val springSpec = spring<IntOffset>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = 0.8f
    )

    private val floatSpringSpec = spring<Float>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = 0.8f
    )

    fun enterTransition(targetIndex: Int, initialIndex: Int): EnterTransition {
        val direction = if (targetIndex > initialIndex) 1 else -1

        return slideInHorizontally(
            initialOffsetX = { fullWidth -> direction * fullWidth },
            animationSpec = springSpec
        ) + fadeIn(
            animationSpec = tween(400)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = floatSpringSpec
        )
    }

    fun exitTransition(targetIndex: Int, initialIndex: Int): ExitTransition {
        val direction = if (targetIndex > initialIndex) 1 else -1

        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -direction * (fullWidth / 4) },
            animationSpec = springSpec
        ) + fadeOut(
            animationSpec = tween(400)
        ) + scaleOut(
            targetScale = 0.92f,
            animationSpec = floatSpringSpec
        )
    }
}