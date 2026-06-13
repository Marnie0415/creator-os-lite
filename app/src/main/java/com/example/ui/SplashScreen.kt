package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.WarningRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Pulse animation for radar icon
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha"
    )

    // Text fade-in sequence
    var showBrand by remember { mutableStateOf(false) }
    val brandAlpha by animateFloatAsState(
        targetValue = if (showBrand) 1f else 0f,
        animationSpec = tween(600),
        label = "brand_fade"
    )

    var showTagline by remember { mutableStateOf(false) }
    val taglineAlpha by animateFloatAsState(
        targetValue = if (showTagline) 1f else 0f,
        animationSpec = tween(500, delayMillis = 100),
        label = "tagline_fade"
    )

    // Loading dots animation
    var dots by remember { mutableStateOf("") }

    // Auto-play the animation sequence
    LaunchedEffect(Unit) {
        showBrand = true
        delay(400)
        showTagline = true
        delay(2000)
        onSplashFinished()
    }

    // Loading dots ticker
    LaunchedEffect(Unit) {
        while (true) {
            dots = when (dots.length) { 0 -> "."; 1 -> ".."; else -> "..." }
            delay(400)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated radar icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha),
                contentAlignment = Alignment.Center
            ) {
                Text("◉", fontSize = 64.sp, color = MoneyGreen,
                    fontWeight = FontWeight.Bold, modifier = Modifier.alpha(0.3f))
                Text("◉", fontSize = 48.sp, color = MoneyGreen,
                    fontWeight = FontWeight.Bold, modifier = Modifier.alpha(0.5f))
                Text("⬟", fontSize = 28.sp, color = WarningRed,
                    fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand: CREATOR OS // LITE
            Text(
                text = "CREATOR OS // LITE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MoneyGreen,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(brandAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Risk Control Radar for Freelancers",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.sp,
                modifier = Modifier.alpha(taglineAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading indicator
            Text(
                text = "initializing$dots",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        }
    }
}
