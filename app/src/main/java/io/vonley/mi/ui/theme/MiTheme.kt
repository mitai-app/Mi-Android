package io.vonley.mi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.vonley.mi.Mi
import io.vonley.mi.R

private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

private val MiColors = lightColorScheme(
    primary = Mi.Color.QUATERNARY,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD7),
    onPrimaryContainer = Color(0xFF410003),
    secondary = Mi.Color.TERTIARY,
    onSecondary = Color.White,
    tertiary = Mi.Color.PURPLE_DARK,
    background = Color.White,
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF2EEEE),
    onSurfaceVariant = Color(0xFF534343),
    error = Color(0xFFB00020),
)

private val MiTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = Montserrat),
        displayMedium = base.displayMedium.copy(fontFamily = Montserrat),
        displaySmall = base.displaySmall.copy(fontFamily = Montserrat),
        headlineLarge = base.headlineLarge.copy(fontFamily = Montserrat),
        headlineMedium = base.headlineMedium.copy(fontFamily = Montserrat),
        headlineSmall = base.headlineSmall.copy(fontFamily = Montserrat),
        titleLarge = base.titleLarge.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily = Montserrat, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = Montserrat, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = Montserrat),
        bodyMedium = base.bodyMedium.copy(fontFamily = Montserrat),
        bodySmall = base.bodySmall.copy(fontFamily = Montserrat),
        labelLarge = base.labelLarge.copy(fontFamily = Montserrat),
        labelMedium = base.labelMedium.copy(fontFamily = Montserrat),
        labelSmall = base.labelSmall.copy(fontFamily = Montserrat),
    )
}

@Composable
fun MiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MiColors, typography = MiTypography, content = content)
}
