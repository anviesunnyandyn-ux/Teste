package com.wellington.lenstranslate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wellington.lenstranslate.services.FloatingLensService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen(::requestOverlayPermission, ::startLens) }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun startLens() {
        requestOverlayPermission()
        startForegroundService(Intent(this, FloatingLensService::class.java))
        startActivity(Intent(this, CapturePermissionActivity::class.java))
    }
}

@Composable
fun HomeScreen(onOverlayPermission: () -> Unit, onStart: () -> Unit) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Lens Translate MVP", style = MaterialTheme.typography.headlineMedium)
                Text("Protótipo Android inspirado em PlayTranslate: botão flutuante, OCR na tela e tradução para português.")
                Button(onClick = onOverlayPermission, modifier = Modifier.fillMaxWidth()) { Text("Permitir sobrepor a outros apps") }
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Iniciar lente flutuante") }
                Text("Uso: abra um jogo, toque no botão flutuante TR e veja a tradução. Para produção, ajuste região de captura e idiomas em Settings.")
            }
        }
    }
}
