/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedCompactChip
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.utils.getString

@Composable
fun OpenOnPhoneButton(
    onClick: () -> Unit = {},
) {
    val height = 56.dp * LocalDensity.current.fontScale
    OutlinedCompactChip(
        modifier = Modifier
            .height(height)
            .fillMaxWidth(),
        onClick = onClick,
        icon = {
            Icon(
                modifier = Modifier.padding(4.dp),
                painter = painterResource(R.drawable.wear_open_on_phone),
                contentDescription = null,
            )
        },
        label = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = getString(R.string.wear_open_on_phone),
            )
        },
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Preview() {
    OpenOnPhoneButton()
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun FontScale() {
    OpenOnPhoneButton()
}