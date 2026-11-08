package com.jksalcedo.datasimswitcher

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDark = isSystemInDarkTheme()
            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isDark -> dynamicDarkColorScheme(this)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(this)
                isDark -> darkColorScheme()
                else -> lightColorScheme()
            }
            MaterialTheme(colorScheme = colorScheme) {
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

private sealed interface RootStatus {
    data object Checking : RootStatus
    data object Granted : RootStatus
    data object Denied : RootStatus
}

@Composable
fun MainScreen() {
    var rootStatus by remember { mutableStateOf<RootStatus>(RootStatus.Checking) }

    LaunchedEffect(Unit) {
        val isRoot = withContext(Dispatchers.IO) {
            Shell.getShell().isRoot
        }
        rootStatus = if (isRoot) RootStatus.Granted else RootStatus.Denied
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Data SIM Switcher",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        RootStatusBadge(status = rootStatus)

        Spacer(modifier = Modifier.height(40.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))

        InstructionsCard()
    }
}

@Composable
private fun RootStatusBadge(status: RootStatus) {
    AnimatedContent(
        targetState = status,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "root-status"
    ) { current ->
        val (icon, text, color) = when (current) {
            RootStatus.Checking -> Triple(
                Icons.Rounded.Info,
                "Checking root access…",
                MaterialTheme.colorScheme.onSurfaceVariant
            )
            RootStatus.Granted -> Triple(
                Icons.Rounded.CheckCircle,
                "Root access granted",
                MaterialTheme.colorScheme.primary
            )
            RootStatus.Denied -> Triple(
                Icons.Rounded.Warning,
                "Root access denied",
                MaterialTheme.colorScheme.error
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (current == RootStatus.Checking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(text = text, color = color, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun InstructionsCard() {
    val steps = listOf(
        "Pull down your Quick Settings panel.",
        "Tap the Edit (pencil) icon.",
        "Find 'Switch SIM' and drag it to your active tiles.",
        "Tap the tile to instantly swap your mobile data SIM."
    )

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "How to use",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            steps.forEachIndexed { index, step ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = step, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}