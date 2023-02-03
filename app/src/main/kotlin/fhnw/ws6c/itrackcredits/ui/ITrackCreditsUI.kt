import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import fhnw.ws6c.itrackcredits.model.ITrackCreditsModel
import fhnw.ws6c.itrackcredits.model.Screen
import fhnw.ws6c.itrackcredits.ui.screens.*
import fhnw.ws6c.itrackcredits.ui.theme.AppTheme

@Composable
fun ITrackCreditsUI(model: ITrackCreditsModel) {
    with(model) {
        AppTheme(model.isDarkTheme) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing)
            ) { screen: Screen ->
                when (screen) {
                    Screen.HOME -> {
                        HomeScreen(model)
                    }
                    Screen.SEMESTER -> {
                        SemesterScreen(model)
                    }
                    Screen.MODULTEPPICH -> {
                        ModulteppichScreen(model)
                    }
                    Screen.MODUL -> {
                        ModulScreen(model)
                    }
                    Screen.MODULGRUPPE -> {
                        ModulgruppeScreen(model)
                    }
                }
            }
        }
    }
}