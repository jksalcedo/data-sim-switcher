package com.jksalcedo.datasimswitcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    // State to hold root status
    var hasRoot by remember { mutableStateOf<Boolean?>(null) }

    // Request root in the background when the UI loads
    LaunchedEffect(Unit) {
        Shell.getShell { shell ->
            hasRoot = shell.isRoot
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Data SIM Switcher",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status Indicator
        when (hasRoot) {
            true -> Text(
                text = "Root Access Granted",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            false -> Text(
                text = "Root Access Denied",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium
            )
            null -> Text(
                text = "Checking Root Access...",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Instructions
        Text(
            text = "How to use:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "1. Pull down your Quick Settings panel.\n" +
                    "2. Tap the Edit (pencil) icon.\n" +
                    "3. Find 'Switch SIM' and drag it to your active tiles.\n" +
                    "4. Tap the tile to instantly swap your mobile data connection.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
        )
    }
}