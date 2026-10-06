package com.example.tripledger.ui.screens.opening

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val TripLedgerTeal = Color(0xFF087F8C)
private val TripLedgerDarkTeal = Color(0xFF16383C)
private val TripLedgerOrange = Color(0xFFF28C28)
private val TripLedgerSun = Color(0xFFFFB347)
private val TripLedgerSkyTop = Color(0xFFDDF4F4)
private val TripLedgerSkyBottom = Color(0xFFFFF3DF)
private val TripLedgerMountain = Color(0xFF174E55)
private val TripLedgerMountainLight = Color(0xFF2D7474)
private val TripLedgerWhite = Color(0xFFF7FBFA)

@Composable
fun OpeningScreen(
    onFinished: () -> Unit
) {
    val logoAlpha = remember {
        Animatable(0f)
    }

    val logoScale = remember {
        Animatable(0.75f)
    }

    val textAlpha = remember {
        Animatable(0f)
    }

    LaunchedEffect(Unit) {

        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing
            )
        )

        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )

        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600
            )
        )

        delay(1400)

        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TripLedgerSkyTop,
                        TripLedgerSkyBottom
                    )
                )
            )
    ) {

        // Adventure landscape background
        AdventureBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(textAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            TripLedgerLogo(
                modifier = Modifier
                    .size(155.dp)
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value)
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "TripLedger",
                color = TripLedgerDarkTeal,
                fontSize = 36.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Your Journey, Your Story",
                color = TripLedgerTeal,
                fontSize = 15.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
private fun AdventureBackground() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        val width = size.width
        val height = size.height

        // Warm sunrise
        drawCircle(
            color = TripLedgerSun.copy(alpha = 0.55f),
            radius = width * 0.24f,
            center = Offset(
                x = width * 0.50f,
                y = height * 0.30f
            )
        )

        drawCircle(
            color = TripLedgerOrange.copy(alpha = 0.22f),
            radius = width * 0.34f,
            center = Offset(
                x = width * 0.50f,
                y = height * 0.30f
            )
        )

        // Far mountain range
        val farMountains = Path().apply {

            moveTo(0f, height * 0.70f)

            lineTo(
                width * 0.18f,
                height * 0.52f
            )

            lineTo(
                width * 0.31f,
                height * 0.67f
            )

            lineTo(
                width * 0.47f,
                height * 0.48f
            )

            lineTo(
                width * 0.65f,
                height * 0.67f
            )

            lineTo(
                width * 0.82f,
                height * 0.53f
            )

            lineTo(
                width,
                height * 0.70f
            )

            lineTo(
                width,
                height
            )

            lineTo(0f, height)

            close()
        }

        drawPath(
            path = farMountains,
            color = TripLedgerMountainLight.copy(alpha = 0.65f)
        )

        // Main mountain range
        val mainMountains = Path().apply {

            moveTo(0f, height * 0.79f)

            lineTo(
                width * 0.20f,
                height * 0.61f
            )

            lineTo(
                width * 0.35f,
                height * 0.74f
            )

            lineTo(
                width * 0.53f,
                height * 0.55f
            )

            lineTo(
                width * 0.69f,
                height * 0.74f
            )

            lineTo(
                width * 0.86f,
                height * 0.62f
            )

            lineTo(
                width,
                height * 0.78f
            )

            lineTo(
                width,
                height
            )

            lineTo(0f, height)

            close()
        }

        drawPath(
            path = mainMountains,
            color = TripLedgerMountain
        )

        // Dark foreground
        val foreground = Path().apply {

            moveTo(0f, height * 0.88f)

            cubicTo(
                width * 0.20f,
                height * 0.80f,
                width * 0.37f,
                height * 0.92f,
                width * 0.55f,
                height * 0.84f
            )

            cubicTo(
                width * 0.73f,
                height * 0.76f,
                width * 0.86f,
                height * 0.91f,
                width,
                height * 0.82f
            )

            lineTo(width, height)

            lineTo(0f, height)

            close()
        }

        drawPath(
            path = foreground,
            color = TripLedgerDarkTeal
        )

        // Small travel trail
        val trail = Path().apply {

            moveTo(
                width * 0.50f,
                height
            )

            cubicTo(
                width * 0.54f,
                height * 0.92f,
                width * 0.47f,
                height * 0.86f,
                width * 0.52f,
                height * 0.80f
            )
        }

        drawPath(
            path = trail,
            color = TripLedgerWhite.copy(alpha = 0.75f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = width * 0.018f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
private fun TripLedgerLogo(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {

        val width = size.width
        val height = size.height

        // Logo background
        drawCircle(
            color = TripLedgerWhite.copy(alpha = 0.96f),
            radius = width * 0.47f,
            center = Offset(
                width * 0.50f,
                height * 0.50f
            )
        )

        // Mountain
        val mountainPath = Path().apply {

            moveTo(
                width * 0.12f,
                height * 0.68f
            )

            lineTo(
                width * 0.38f,
                height * 0.34f
            )

            lineTo(
                width * 0.52f,
                height * 0.51f
            )

            lineTo(
                width * 0.67f,
                height * 0.28f
            )

            lineTo(
                width * 0.90f,
                height * 0.68f
            )

            close()
        }

        drawPath(
            path = mountainPath,
            color = TripLedgerTeal
        )

        // Mountain highlight
        val highlightPath = Path().apply {

            moveTo(
                width * 0.38f,
                height * 0.34f
            )

            lineTo(
                width * 0.52f,
                height * 0.51f
            )

            lineTo(
                width * 0.45f,
                height * 0.46f
            )

            close()
        }

        drawPath(
            path = highlightPath,
            color = TripLedgerOrange
        )

        // Travel path
        val travelPath = Path().apply {

            moveTo(
                width * 0.18f,
                height * 0.79f
            )

            cubicTo(
                width * 0.38f,
                height * 0.69f,
                width * 0.53f,
                height * 0.83f,
                width * 0.65f,
                height * 0.69f
            )

            cubicTo(
                width * 0.72f,
                height * 0.61f,
                width * 0.78f,
                height * 0.55f,
                width * 0.79f,
                height * 0.45f
            )
        }

        drawPath(
            path = travelPath,
            color = TripLedgerWhite,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = width * 0.035f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Location pin
        val pinCenter = Offset(
            x = width * 0.79f,
            y = height * 0.36f
        )

        drawCircle(
            color = TripLedgerOrange,
            radius = width * 0.10f,
            center = pinCenter
        )

        drawCircle(
            color = TripLedgerWhite,
            radius = width * 0.035f,
            center = pinCenter
        )
    }
}