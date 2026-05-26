package br.com.contaemdia

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import br.com.contaemdia.presentation.ads.ConsentManager
import br.com.contaemdia.presentation.navigation.ContaEmDiaNavHost
import br.com.contaemdia.presentation.theme.ContaEmDiaTheme
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var adsEnabled by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                ConsentManager.requestConsent(this@MainActivity) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        MobileAds.initialize(this@MainActivity) {}
                        withContext(Dispatchers.Main) {
                            adsEnabled = true
                        }
                    }
                }
            }

            ContaEmDiaTheme {
                RequestNotificationPermission()
                ContaEmDiaNavHost(
                    navController = rememberNavController(),
                    adsEnabled = adsEnabled,
                )
            }
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {},
    )
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
