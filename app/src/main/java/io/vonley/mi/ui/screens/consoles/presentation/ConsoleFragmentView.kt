package io.vonley.mi.ui.screens.consoles.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.R
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.domain.model.color


@AndroidEntryPoint
class ConsoleFragmentView: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ConsolesView()
            }
        }
    }
}

@Composable
fun ConsolesView() {
    val vm = hiltViewModel<ConsoleViewModel>()
    val consoleState by vm.consoles.collectAsState()
    ConsoleViewState(
        state = consoleState,
        onConsoleClick = { console: Console ->
            vm.select(console)
        },
        onConsolePin = { console: Console, pin: Boolean ->
            if (pin) {
                vm.pin(console)
            } else {
                vm.unpin(console)
            }
        }
    )
}

@Composable
fun ConsoleViewState(
    state: ConsoleState,
    onConsoleClick: (Console) -> Unit,
    onConsolePin: (Console, Boolean) -> Unit
) {
    Column {
        when (state) {
            is ConsoleState.Error -> {

            }

            is ConsoleState.Success -> {
                LazyColumn {
                    items(state.repos) {
                        ConsoleView(
                            console = it,
                            onConsoleClick = onConsoleClick,
                            onConsolePin = onConsolePin
                        )
                    }
                }
            }

            ConsoleState.Loading -> {

            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewConsoleView() {
    val consoles = arrayOf(
        Console(
            ip = "192.168.1.184",
            name = "PS4",
            type = PlatformType.PS4,
            features = arrayListOf(
                Feature.RPI, Feature.KLOG, Feature.GOLDENHEN, Feature.FTP
            ),
            lastKnownReachable = true,
            wifi = "Gaoooub",
            pinned = true
        ),
        Console(
            ip = "192.168.11.185",
            name = "PS3",
            type = PlatformType.PS3,
            features = arrayListOf(
                Feature.WEBMAN, Feature.PS3MAPI, Feature.CCAPI, Feature.FTP
            ),
            lastKnownReachable = true,
            wifi = "Gaoooub",
            pinned = true
        ),
        Console(
            ip = "192.168.1.1",
            name = "Unknown",
            type = PlatformType.UNKNOWN,
            features = arrayListOf(
                Feature.FTP
            ),
            lastKnownReachable = true,
            wifi = "Gaoooub",
            pinned = true
        )
    )
    Column {
        consoles.forEach {
            ConsoleView(
                console = it,
                onConsoleClick = { console ->
                    //vm.select(console)
                },
                onConsolePin = { console, pinned ->

                })
        }
    }
}

@Composable
fun ConsoleView(
    console: Console,
    onConsoleClick: (Console) -> Unit,
    onConsolePin: (Console, Boolean) -> Unit
) {
    val id = when (console.type) {
        PlatformType.UNKNOWN -> R.drawable.icon_svg_wifi
        PlatformType.PS3 -> R.drawable.icon_svg_monitor
        PlatformType.PS4 -> R.drawable.icon_svg_settings
    }
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onConsoleClick(console)
            }
            .wrapContentHeight()
            .padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = console.color
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = rememberVectorPainter(ImageVector.vectorResource(R.drawable.icon_svg_monitor)),
                modifier = Modifier
                    .width(42.dp)
                    .height(42.dp)
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.Crop, colorFilter = ColorFilter.tint(Color.White),
                contentDescription = null // decorative element
            )

            Column(
                modifier = Modifier
                    .padding(16.dp, 0.dp)
            ) {
                Text(
                    text = console.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                )
                Text(
                    text = console.ip,
                    color = Color.White,
                )
            }
        }
    }

}