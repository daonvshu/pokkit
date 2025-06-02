import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.daonvshu.shared.generated.resources.*
import com.daonvshu.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun ChillRoundGothic() = FontFamily(
    Font(Res.font.ChillRoundGothic_ExtraLight, weight = FontWeight.ExtraLight, style = FontStyle.Normal),
    Font(Res.font.ChillRoundGothic_Light,      weight = FontWeight.Light,      style = FontStyle.Normal),
    Font(Res.font.ChillRoundGothic_Regular,    weight = FontWeight.Normal,     style = FontStyle.Normal),
    Font(Res.font.ChillRoundGothic_Medium,     weight = FontWeight.Medium,     style = FontStyle.Normal),
    Font(Res.font.ChillRoundGothic_Bold,       weight = FontWeight.Bold,       style = FontStyle.Normal),
    Font(Res.font.ChillRoundGothic_Heavy,      weight = FontWeight.Black,      style = FontStyle.Normal),
)

@Composable
fun Calista() = FontFamily(
    Font(Res.font.Calista, weight = FontWeight.Normal, style = FontStyle.Normal),
)