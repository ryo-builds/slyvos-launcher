package com.slyvos.launcher.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slyvos.launcher.data.repository.PackageManagerAppRepository
import com.slyvos.launcher.dynamicbar.manager.DynamicBarManager
import com.slyvos.launcher.dynamicbar.model.DynamicBarExpansion
import com.slyvos.launcher.ui.dynamicbar.components.DynamicBarContainer
import com.slyvos.launcher.ui.home.components.AppDrawerSheet
import com.slyvos.launcher.ui.home.components.AppIconItem
import com.slyvos.launcher.ui.home.components.ClockHeader
import com.slyvos.launcher.ui.home.components.DockSurface

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel(
        factory = HomeScreenViewModelFactory(
            PackageManagerAppRepository(LocalContext.current.applicationContext)
        )
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dynamic Bar Manager
    val dynamicBarManager = remember {
        DynamicBarManager(context.applicationContext, scope)
    }

    DisposableEffect(Unit) {
        dynamicBarManager.start()
        onDispose {
            dynamicBarManager.stop()
        }
    }

    val dynamicBarSettings by dynamicBarManager.settings.collectAsState()
    val dynamicBarExpansion by dynamicBarManager.expansion.collectAsState()
    val isDynamicBarExpanded = dynamicBarExpansion == DynamicBarExpansion.EXPANDED
    val isSettingsVisible by dynamicBarManager.isSettingsSheetVisible.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initTimeAndApps(context)
    }

    BackHandler(enabled = isSettingsVisible || isDynamicBarExpanded || uiState.isDrawerVisible) {
        when {
            isSettingsVisible -> dynamicBarManager.toggleSettingsSheet(false)
            isDynamicBarExpanded -> dynamicBarManager.collapse()
            uiState.isDrawerVisible -> viewModel.setDrawerVisible(false)
        }
    }

    // Liquid Minimal Ambient Background Gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F141C),
            Color(0xFF080B10),
            Color(0xFF05070A)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -18f && !uiState.isDrawerVisible && !isDynamicBarExpanded) {
                        viewModel.setDrawerVisible(true)
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Permanent Dynamic Bar rendered at top
            DynamicBarContainer(manager = dynamicBarManager)

            // Prominent minimal clock & date header
            ClockHeader(
                timeString = uiState.timeFormatted,
                amPmString = uiState.amPmFormatted,
                dateString = uiState.dateFormatted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main desktop shortcut grid (installed apps)
            if (uiState.allApps.isNotEmpty()) {
                val gridApps = uiState.allApps.take(12)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(gridApps, key = { "${it.packageName}/${it.className}" }) { app ->
                        AppIconItem(
                            app = app,
                            onClick = { viewModel.launchApp(context, app) },
                            iconSize = 50.dp,
                            showLabel = true
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Bottom floating translucent dock
            DockSurface(
                dockApps = uiState.dockApps,
                onAppClick = { app -> viewModel.launchApp(context, app) },
                onSwipeUpTrigger = { viewModel.setDrawerVisible(true) },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Swipe up App Drawer Sheet overlay
        AppDrawerSheet(
            isVisible = uiState.isDrawerVisible,
            allApps = uiState.filteredApps,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChanged,
            onClearSearch = viewModel::clearSearchQuery,
            onDismiss = { viewModel.setDrawerVisible(false) },
            onAppClick = { app -> viewModel.launchApp(context, app) },
            animationPreference = dynamicBarSettings.animationPreference
        )
    }
}
