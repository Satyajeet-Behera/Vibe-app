package com.vibe.social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vibe.social.data.auth.AuthRepository
import com.vibe.social.theme.VibeTheme
import com.vibe.social.ui.screens.MainScaffold

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthRepository.instance.initialize(this)
        enableEdgeToEdge()
        setContent {
            VibeTheme {
                MainScaffold()
            }
        }
    }
}
