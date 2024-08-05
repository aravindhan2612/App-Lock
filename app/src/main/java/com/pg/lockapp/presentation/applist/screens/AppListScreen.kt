package com.pg.lockapp.presentation.applist.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pg.lockapp.domain.models.enitites.AppInformation
import com.pg.lockapp.domain.models.interfaces.AppListUiState
import com.pg.lockapp.presentation.applist.items.AppInfoItem
import com.pg.lockapp.presentation.applist.viewmodels.AppListViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListScreen(appListViewModel: AppListViewModel = hiltViewModel()) {
    val applicationContext = LocalContext.current.applicationContext

    when (appListViewModel.appListUiState) {
        is AppListUiState.Success -> {
            RenderAppList()
        }

        is AppListUiState.Error -> {

        }

        is AppListUiState.Loading -> {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun RenderAppList(appListViewModel: AppListViewModel = hiltViewModel()) {
    val appInfos by appListViewModel.appInfos.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(appInfos) { appInfo: AppInformation ->
                AppInfoItem(appInfo = appInfo, onCheckedChange = { checked ->
                    scope.launch {
                        appInfo.packageName?.let { appListViewModel.updateLockForApp(it, checked) }
                    }
                })
            }
        }
    }
}