package com.jaemin.fitzam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jaemin.fitzam.ui.FitzamApp
import com.jaemin.fitzam.ui.theme.FitzamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitzamTheme {
                FitzamApp()
            }
        }
    }
}