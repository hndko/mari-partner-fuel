package id.maripartner.fuel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.maripartner.fuel.core.designsystem.theme.MariPartnerFuelTheme
import id.maripartner.fuel.core.navigation.AppNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MariPartnerFuelTheme {
                AppNavHost()
            }
        }
    }
}
