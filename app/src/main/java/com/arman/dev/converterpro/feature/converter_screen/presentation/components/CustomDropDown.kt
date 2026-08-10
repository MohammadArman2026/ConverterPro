package com.arman.dev.converterpro.feature.converter_screen.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.AppSurface
import com.arman.dev.converterpro.core.designsystem.color.DropDownBackground
import com.arman.dev.converterpro.core.designsystem.color.DropDownStroke
import com.arman.dev.converterpro.core.designsystem.color.PrimaryBackground
import com.arman.dev.converterpro.feature.converter_screen.domain.model.DropDown
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Encoder
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Extension
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Map
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T: DropDown>CustomDropDown(
    modifier: Modifier = Modifier,
    selectedDropDown: T,
    dropDownList: List<T>,
    onSelectDropDown: (T) -> Unit
) {
    var isDropDownExpanded by remember {
        mutableStateOf(false)
    }


    ExposedDropdownMenuBox(
        expanded = isDropDownExpanded,
        onExpandedChange = {
            isDropDownExpanded = it
        },
        modifier = modifier
    ) {
        Card(
            onClick = {
                isDropDownExpanded = !isDropDownExpanded
            },
            modifier = Modifier,
            colors = CardDefaults.cardColors(
                containerColor = DropDownBackground,
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp , DropDownStroke)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReusableText(
                    text = selectedDropDown.dropDown,
                    modifier = Modifier,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.width(8.dp))
                val dropDownIcon =
                    if (isDropDownExpanded) R.drawable.baseline_arrow_drop_down_24 else R.drawable.outline_arrow_drop_up_24
                Icon(
                    painter = painterResource(dropDownIcon),
                    contentDescription = "drop down icon",
                    modifier = Modifier,
                    tint = Color.White
                )
            }
        }


        ExposedDropdownMenu(
            expanded = isDropDownExpanded,
            onDismissRequest = {
                isDropDownExpanded = false
            },
            modifier = Modifier,
            shape = MaterialTheme.shapes.medium,
            containerColor = PrimaryBackground,
        ) {
            dropDownList.forEach { dropDown ->
                DropdownMenuItem(
                    text = {
                        ReusableText(
                            text = dropDown.dropDown,
                            style = TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        )
                    },
                    onClick = {
                        onSelectDropDown(dropDown)
                        isDropDownExpanded = false
                    },
                    modifier = Modifier,
                )
            }
        }
    }

}


@Composable
@Preview(showBackground = true)
private fun Preview() {
    CustomDropDown(
        modifier = Modifier,
        selectedDropDown = Encoder.WAV_PACK,
        dropDownList = Map.extensionToEncoding[Extension.WAV_PACK] ?: emptyList(),
        onSelectDropDown = {}
    )
}