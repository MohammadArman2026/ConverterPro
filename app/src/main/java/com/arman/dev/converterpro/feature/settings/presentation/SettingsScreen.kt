package com.arman.dev.converterpro.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arman.dev.converterpro.core.designsystem.color.PrimaryBackground
import com.arman.dev.converterpro.feature.home.domain.model.SettingOption
import com.arman.dev.converterpro.feature.settings.presentation.components.SettingItemCard
import com.arman.dev.converterpro.feature.settings.presentation.components.SettingsScreenTopBar

@Composable
fun SettingsScreenRoute(
    onBackClick: () -> Unit,
    onOptionClick: (SettingOption) -> Unit = {},
) {
    SettingsScreenUi(
        onBackClick = onBackClick,
        onOptionClick = {
            when(it){
                SettingOption.UPGRADE -> {}
                SettingOption.WRITE_A_FEEDBACK -> {}
                SettingOption.SHARE_THIS_APP -> {}
                SettingOption.MANAGE_SUBSCRIPTION -> {}
                SettingOption.CONTACT_US -> {}
            }
        },
    )
}

@Composable
fun SettingsScreenUi(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onOptionClick: (SettingOption) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryBackground),
    ) {
        SettingsScreenTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingOption.entries.forEach { option ->
                SettingItemCard(
                    title = option.title,
                    onClick = { onOptionClick(option) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreenUi(
        onBackClick = {},
        onOptionClick = {},
    )
}
