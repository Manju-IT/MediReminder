package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    val stepTitles = listOf(
        "Manage Reminders",
        "Track Daily Progress",
        "Never Miss Your Dose"
    )

    val stepDescriptions = listOf(
        "Add prescription timings and organize Tablet, Syrup, and Capsule details step by step.",
        "Check pill history logs. Observe weekly logs to stay on top of your medical recovery milestones.",
        "Receive exact high-priority vibrating alarms and notifications on active custom schedules."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicator dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStep) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                        )
                    }
                }

                if (currentStep < 2) {
                    TextButton(onClick = onFinished) {
                        Text(
                            "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Beautiful Canvas Illustration
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (currentStep) {
                    0 -> OnboardingPillIllustration(MaterialTheme.colorScheme.primary)
                    1 -> OnboardingProgressIllustration(MaterialTheme.colorScheme.secondary)
                    else -> OnboardingAlarmIllustration(MaterialTheme.colorScheme.tertiary)
                }
            }

            // Title & Description Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stepTitles[currentStep],
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stepDescriptions[currentStep],
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = {
                    if (currentStep < 2) {
                        currentStep++
                    } else {
                        onFinished()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (currentStep == 2) "GET STARTED" else "NEXT STEP",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Arrow Next"
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPillIllustration(tint: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "step_0")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "illustration_pill"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f + floatAnim

        // Outer ambient glow ring
        drawCircle(
            color = tint.copy(alpha = 0.08f),
            radius = 110.dp.toPx()
        )
        drawCircle(
            color = tint.copy(alpha = 0.15f),
            radius = 85.dp.toPx()
        )

        // Draw geometric medicine pill capsule representation
        drawRoundRect(
            color = tint,
            topLeft = androidx.compose.ui.geometry.Offset(cx - 30.dp.toPx(), cy - 65.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(60.dp.toPx(), 130.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(30.dp.toPx(), 30.dp.toPx())
        )

        // Draw the capsule cap line division
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = androidx.compose.ui.geometry.Offset(cx - 30.dp.toPx(), cy),
            end = androidx.compose.ui.geometry.Offset(cx + 30.dp.toPx(), cy),
            strokeWidth = 4.dp.toPx()
        )

        // Draw cross sign above
        drawCircle(
            color = Color.White,
            radius = 12.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(cx, cy - 30.dp.toPx())
        )
    }
}

@Composable
fun OnboardingProgressIllustration(tint: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "step_1")
    val sweepAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Outer subtle tracks
        drawCircle(
            color = tint.copy(alpha = 0.08f),
            radius = 100.dp.toPx(),
            style = Stroke(width = 16.dp.toPx())
        )

        // Radial progress arc
        drawArc(
            color = tint,
            startAngle = -90f,
            sweepAngle = sweepAnim,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(cx - 100.dp.toPx(), cy - 100.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(200.dp.toPx(), 200.dp.toPx()),
            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
        )

        // Center glowing focal shield
        drawCircle(
            color = tint.copy(alpha = 0.15f),
            radius = 60.dp.toPx()
        )
    }
}

@Composable
fun OnboardingAlarmIllustration(tint: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "step_2")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseOutExpo),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_glow"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Expanding ambient alarm ring
        drawCircle(
            color = tint.copy(alpha = (1.3f - ringScale).coerceIn(0f, 0.4f)),
            radius = 100.dp.toPx() * ringScale,
            style = Stroke(width = 3.dp.toPx())
        )

        // Base solid alert circle
        drawCircle(
            color = tint.copy(alpha = 0.12f),
            radius = 80.dp.toPx()
        )

        // Center visual core bell representation
        drawCircle(
            color = tint,
            radius = 45.dp.toPx()
        )

        // Cute bell top loop representation
        drawRect(
            color = tint,
            topLeft = androidx.compose.ui.geometry.Offset(cx - 12.dp.toPx(), cy - 60.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 20.dp.toPx())
        )
    }
}
