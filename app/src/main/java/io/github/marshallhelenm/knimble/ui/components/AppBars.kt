package io.github.marshallhelenm.knimble.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import io.github.marshallhelenm.knimble.R
import io.github.marshallhelenm.knimble.ui.theme.TitleFontFamily

// The app's top-level screens, one per bottom bar item.
enum class AppDestination(@param:StringRes val label: Int, @param:DrawableRes val icon: Int) {
    Timer(R.string.tab_timer, R.drawable.ic_notification),
    Exercises(R.string.tab_exercises, R.drawable.ic_list)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)?, actions: @Composable RowScope.() -> Unit = {}) {
    CenterAlignedTopAppBar(
        // A handwritten face reads small, so it goes well above the default 22sp.
        title = { Text(title, fontFamily = TitleFontFamily, fontSize = 30.sp) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.back))
                }
            }
        },
        actions = actions
    )
}

@Composable
fun AppBottomBar(current: AppDestination, onSelect: (AppDestination) -> Unit) {
    NavigationBar {
        AppDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == current,
                onClick = { onSelect(destination) },
                icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                label = { Text(stringResource(destination.label)) }
            )
        }
    }
}
