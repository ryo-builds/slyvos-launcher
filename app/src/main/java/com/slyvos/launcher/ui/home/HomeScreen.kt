package com.slyvos.launcher.ui.home

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slyvos.launcher.data.model.BlurIntensity
import com.slyvos.launcher.data.model.DoubleTapAction
import com.slyvos.launcher.data.model.LongPressAction
import com.slyvos.launcher.data.model.SurfaceAppearance
import com.slyvos.launcher.data.model.SwipeUpAction
import com.slyvos.launcher.data.repository.PackageManagerAppRepository
import com.slyvos.launcher.data.repository.PreferencesPersonalizationRepository
import com.slyvos.launcher.data.repository.PreferencesWidgetRepository
import com.slyvos.launcher.data.repository.SystemWidgetDiscoveryRepository
import com.slyvos.launcher.dynamicbar.manager.DynamicBarManager
import com.slyvos.launcher.dynamicbar.model.DynamicBarExpansion
import com.slyvos.launcher.ui.dynamicbar.components.DynamicBarContainer
import com.slyvos.launcher.ui.home.components.AppDrawerSheet
import com.slyvos.launcher.ui.home.components.AppIconItem
import com.slyvos.launcher.ui.home.components.ClockHeader
import com.slyvos.launcher.ui.home.components.DockSurface
import com.slyvos.launcher.ui.personalization.DockAppPickerSheet
import com.slyvos.launcher.ui.personalization.PersonalizationSheet
import com.slyvos.launcher.ui.widget.WidgetGridCanvas
import com.slyvos.launcher.ui.widget.WidgetPickerSheet
import com.slyvos.launcher.widget.SlyvosAppWidgetHost

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel(
        factory = HomeScreenViewModelFactory(
            PackageManagerAppRepository(
                LocalContext.current.applicationContext,
                PreferencesPersonalizationRepository(LocalContext.current.applicationContext)
            ),
            PreferencesWidgetRepository(LocalContext.current.applicationContext),
            SystemWidgetDiscoveryRepository(LocalContext.current.applicationContext),
            PreferencesPersonalizationRepository(LocalContext.current.applicationContext)
        )
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // AppWidgetHost Lifecycle
    val appWidgetHost = remember {
        SlyvosAppWidgetHost(context.applicationContext)
    }

    DisposableEffect(Unit) {
        appWidgetHost.startListening()
        onDispose {
            try {
                appWidgetHost.stopListening()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Widget Configuration Activity Result Launcher
    val configLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onWidgetConfigureResult(
            context = context,
            resultCode = result.resultCode,
            appWidgetHost = appWidgetHost,
            onLaunchConfigure = { intent, _ -> }
        )
    }

    // Dynamic Bar Manager synchronized with PersonalizationRepository
    val dynamicBarManager = remember {
        DynamicBarManager(
            context.applicationContext,
            scope,
            PreferencesPersonalizationRepository(context.applicationContext)
        )
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
    val isDynamicBarSettingsSheetVisible by dynamicBarManager.isSettingsSheetVisible.collectAsState()

    // Open personalization sheet if dynamic bar gear settings icon is tapped
    LaunchedEffect(isDynamicBarSettingsSheetVisible) {
        if (isDynamicBarSettingsSheetVisible) {
            dynamicBarManager.toggleSettingsSheet(false)
            viewModel.setPersonalizationSheetVisible(true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initTimeAndApps(context)
    }

    var showHomeContextMenu by remember { mutableStateOf(false) }

    BackHandler(
        enabled = showHomeContextMenu || uiState.isPersonalizationSheetVisible || uiState.isDockAppPickerVisible || isDynamicBarExpanded || uiState.isDrawerVisible || uiState.isWidgetPickerVisible
    ) {
        when {
            showHomeContextMenu -> showHomeContextMenu = false
            uiState.isDockAppPickerVisible -> viewModel.setDockAppPickerVisible(false)
            uiState.isPersonalizationSheetVisible -> viewModel.setPersonalizationSheetVisible(false)
            isDynamicBarExpanded -> dynamicBarManager.collapse()
            uiState.isWidgetPickerVisible -> viewModel.closeWidgetPicker()
            uiState.isDrawerVisible -> viewModel.setDrawerVisible(false)
        }
    }

    // Apply Phase 6 Appearance Settings
    val personalization = uiState.personalization
    val appearance = personalization.appearance
    val homeLayout = personalization.homeLayout
    val gestures = personalization.gestures
    val cornerRadius = appearance.cornerGeometry.cornerDp.dp
    val translucencyAlpha = appearance.transparencyLevel
    val gridSpacing = homeLayout.density.spacingDp.dp

    val backgroundModifier = when (appearance.surfaceAppearance) {
        SurfaceAppearance.SOLID_MINIMAL -> Modifier.background(Color(0xFF06080C))
        SurfaceAppearance.AMBIENT_GRADIENT -> Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F141C),
                    Color(0xFF1A2332),
                    Color(0xFF0A0F18)
                )
            )
        )
        SurfaceAppearance.LIQUID_TRANSLUCENT -> Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F141C).copy(alpha = 1f - translucencyAlpha * 0.4f),
                    Color(0xFF080B10).copy(alpha = 1f - translucencyAlpha * 0.4f),
                    Color(0xFF05070A)
                )
            )
        )
    }

    // Isolated background blur render effect for background surface layer only
    val blurRadius = if (appearance.surfaceAppearance == SurfaceAppearance.SOLID_MINIMAL) 0f else appearance.blurIntensity.blurRadiusDp
    val backgroundBlurGraphicsLayer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f) {
        Modifier.graphicsLayer {
            renderEffect = RenderEffect.createBlurEffect(
                blurRadius, blurRadius, Shader.TileMode.CLAMP
            ).asComposeRenderEffect()
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (gestures.doubleTapAction == DoubleTapAction.EXPAND_DYNAMIC_BAR) {
                            dynamicBarManager.handleTap()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -18f && !uiState.isDrawerVisible && !isDynamicBarExpanded && !uiState.isWidgetPickerVisible && !uiState.isPersonalizationSheetVisible) {
                        if (gestures.swipeUpAction == SwipeUpAction.PERSONALIZATION_SHEET) {
                            viewModel.setPersonalizationSheetVisible(true)
                        } else {
                            viewModel.setDrawerVisible(true)
                        }
                    }
                }
            }
    ) {
        // LAYER 1: Dedicated Background Surface Layer (Receives RenderEffect Blur)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
                .then(backgroundBlurGraphicsLayer)
        )

        // LAYER 2: Foreground UI Elements (Sharp, Readable, Crisp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Permanent Dynamic Bar rendered at top
            DynamicBarContainer(manager = dynamicBarManager)

            // Prominent minimal clock & date header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            if (gestures.longPressAction == LongPressAction.WIDGET_PICKER) {
                                viewModel.openWidgetPicker(context)
                            } else {
                                viewModel.setPersonalizationSheetVisible(true)
                            }
                        }
                    )
            ) {
                ClockHeader(
                    timeString = uiState.timeFormatted,
                    amPmString = uiState.amPmFormatted,
                    dateString = uiState.dateFormatted
                )
            }

            // Spatial Widgets Canvas
            if (uiState.placedWidgets.isNotEmpty()) {
                WidgetGridCanvas(
                    placedWidgets = uiState.placedWidgets,
                    appWidgetHost = appWidgetHost,
                    onRemoveWidget = { id -> viewModel.removeWidget(id, appWidgetHost) },
                    onResizeWidget = viewModel::resizeWidget,
                    modifier = Modifier.weight(0.55f)
                )
            } else {
                // Add Widget / Personalization Canvas Prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(Color.White.copy(alpha = translucencyAlpha * 0.3f))
                        .combinedClickable(
                            onClick = { showHomeContextMenu = true },
                            onLongClick = { showHomeContextMenu = true }
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "+ Add Widgets or Customize Slyvos",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main desktop shortcut grid (installed apps)
            if (uiState.allApps.isNotEmpty()) {
                val gridApps = uiState.allApps.take(8)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(gridSpacing),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxWidth()
                ) {
                    items(gridApps, key = { "${it.packageName}/${it.className}" }) { app ->
                        AppIconItem(
                            app = app,
                            onClick = { viewModel.launchApp(context, app) },
                            iconSize = homeLayout.iconSize.dpValue.dp,
                            showLabel = true,
                            iconPresentation = appearance.iconPresentation
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(0.45f))
            }

            // Floating dock
            if (homeLayout.isDockVisible) {
                DockSurface(
                    dockApps = uiState.dockApps,
                    onAppClick = { app -> viewModel.launchApp(context, app) },
                    onSwipeUpTrigger = {
                        if (gestures.swipeUpAction == SwipeUpAction.PERSONALIZATION_SHEET) {
                            viewModel.setPersonalizationSheetVisible(true)
                        } else {
                            viewModel.setDrawerVisible(true)
                        }
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        // Context Menu Overlay for Home Long-Press / Canvas Tap
        if (showHomeContextMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showHomeContextMenu = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161B26))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Slyvos Canvas Options",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF263238))
                            .clickable {
                                showHomeContextMenu = false
                                viewModel.openWidgetPicker(context)
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                            .semantics { contentDescription = "Add Widget option" }
                    ) {
                        Text(text = "🧩 Add Widgets", color = Color.White, fontSize = 14.sp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E88E5))
                            .clickable {
                                showHomeContextMenu = false
                                viewModel.setPersonalizationSheetVisible(true)
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                            .semantics { contentDescription = "Personalization option" }
                    ) {
                        Text(text = "🎨 Personalization", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
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

        // Phase 5 Widget Picker Sheet Overlay
        WidgetPickerSheet(
            isVisible = uiState.isWidgetPickerVisible,
            availableWidgets = uiState.filteredWidgets,
            searchQuery = uiState.widgetSearchQuery,
            onSearchQueryChange = viewModel::onWidgetSearchQueryChanged,
            onClearSearch = viewModel::clearWidgetSearchQuery,
            onDismiss = viewModel::closeWidgetPicker,
            onWidgetSelect = { providerInfo ->
                viewModel.selectAndAddWidget(
                    context = context,
                    providerInfo = providerInfo,
                    appWidgetHost = appWidgetHost,
                    onLaunchConfigure = { intent, _ -> configLauncher.launch(intent) }
                )
            },
            animationPreference = dynamicBarSettings.animationPreference
        )

        // Phase 6 Personalization Sheet Overlay
        PersonalizationSheet(
            isVisible = uiState.isPersonalizationSheetVisible,
            personalization = uiState.personalization,
            dynamicBarSettings = dynamicBarSettings,
            onUpdateHomeLayout = viewModel::updateHomeLayout,
            onUpdateAppearance = viewModel::updateAppearance,
            onUpdateGestures = viewModel::updateGestures,
            onUpdateDynamicBarSettings = { transform ->
                dynamicBarManager.updateSettings(transform)
            },
            onOpenDockPicker = { viewModel.setDockAppPickerVisible(true) },
            onDismiss = { viewModel.setPersonalizationSheetVisible(false) }
        )

        // Phase 6 Dock App Picker Sheet Overlay
        DockAppPickerSheet(
            isVisible = uiState.isDockAppPickerVisible,
            allApps = uiState.allApps,
            selectedPackages = uiState.personalization.dock.customDockPackages,
            onSaveDockApps = viewModel::saveDockPackages,
            onDismiss = { viewModel.setDockAppPickerVisible(false) },
            animationPreference = dynamicBarSettings.animationPreference
        )
    }
}
