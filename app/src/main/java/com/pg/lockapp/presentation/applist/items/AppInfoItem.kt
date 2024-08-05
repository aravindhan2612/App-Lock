package com.pg.lockapp.presentation.applist.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.pg.lockapp.common.CommonHelper
import com.pg.lockapp.domain.models.enitites.AppInformation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun AppInfoItem(appInfo: AppInformation, onCheckedChange: ((Boolean) -> Unit)?) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            appInfo.icon?.let {
                Image(
                    bitmap = CommonHelper.getBitmapFromBase64String(it).asImageBitmap(),
                    contentDescription = appInfo.appName,
                    modifier = Modifier
                        .size(50.dp)
                        .weight(1f)
                )
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .weight(5f)
            ) {
                var showSecondLine by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                appInfo.appName?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                appInfo.packageName?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        maxLines = if (showSecondLine) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            scope.launch {
                                showSecondLine = !showSecondLine
                            }
                        }
                    )
                }

            }
            Switch(
                checked = appInfo.isLocked,
                onCheckedChange = onCheckedChange)
        }
    }
}