package com.almudier.mygarage.core.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOutCirc
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

object Motion {
    enum class ScaleTransitionDirection {
        INWARDS,
        OUTWARDS
    }

    fun scaleIntoContainer(
        direction: ScaleTransitionDirection = ScaleTransitionDirection.INWARDS,
        initialScale: Float = if (direction == ScaleTransitionDirection.OUTWARDS) 0.9f else 1.1f
    ): EnterTransition {
        return scaleIn(
            animationSpec = tween(150, delayMillis = 90, easing = EaseInOutCirc),
            initialScale = initialScale,
        ) + fadeIn(animationSpec = tween(150, delayMillis = 90, easing = EaseInOutCirc))
    }

    fun scaleOutOfContainer(
        direction: ScaleTransitionDirection = ScaleTransitionDirection.OUTWARDS,
        targetScale: Float = if (direction == ScaleTransitionDirection.INWARDS) 0.9f else 1.1f
    ): ExitTransition {
        return scaleOut(
            animationSpec = tween(150, delayMillis = 90, easing = EaseInOutCirc),
            targetScale = targetScale
        ) + fadeOut(animationSpec = tween(150, delayMillis = 90, easing = EaseInOutCirc))
    }
}
