package com.vibe.social.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// AMOLED Dark Theme Palette - Professional Polish
val VibeBlack = Color(0xFF000000)
val VibeDarkSurface = Color(0xFF121212)
val VibeDarkElevated = Color(0xFF18181B)
val VibeDarkCard = Color(0xFF1C1C1E)
val VibeZinc900 = Color(0xFF18181B)
val VibeZinc800 = Color(0xFF27272A)
val VibeZinc700 = Color(0xFF3F3F46)
val VibeZinc600 = Color(0xFF52525B)
val VibeZinc500 = Color(0xFF71717A)
val VibeZinc400 = Color(0xFFA1A1AA)
val VibeZinc300 = Color(0xFFD4D4D8)

// Borders and Dividers
val VibeBorderSubtle = Color(0x0DFFFFFF) // border-white/5
val VibeBorder = Color(0x1AFFFFFF)       // border-white/10
val VibeLightBorder = Color(0x33FFFFFF)  // border-white/20

// Brand & Accent Colors
val VibeAccentBlue = Color(0xFF3797EF)
val VibeSelectionBg = Color(0x4D3797EF)  // selection:bg-[#3797EF]/30
val VibeLikeRed = Color(0xFFED4956)
val VibeUnreadDot = Color(0xFFED4956)
val VibeVerifiedBlue = Color(0xFF3797EF)

// Text Hierarchy
val VibeTextPrimary = Color(0xFFFFFFFF)
val VibeTextSecondary = Color(0xFFA1A1AA) // zinc-400
val VibeTextTertiary = Color(0xFF71717A)  // zinc-500
val VibeTextDisabled = Color(0xFF52525B)  // zinc-600

// Instagram Sunset Gradient Colors
val InstagramGradientOrange = Color(0xFFF58529)
val InstagramGradientPink = Color(0xFFDD2A7B)
val InstagramGradientPurple = Color(0xFF8134AF)
val InstagramGradientBlue = Color(0xFF515BD4)
val InstagramGradientYellow = Color(0xFFFEDA75)

val InstagramStoryGradientBrush = Brush.linearGradient(
    colors = listOf(
        InstagramGradientOrange,
        InstagramGradientPink,
        InstagramGradientPurple
    )
)

val InstagramStoryViewedBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF3F3F46),
        Color(0xFF52525B),
        Color(0xFF3F3F46)
    )
)
