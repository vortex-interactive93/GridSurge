package com.example.gridsurge.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gridsurge.features.adventure.data.AdventureRepository
import com.example.gridsurge.features.adventure.model.LevelNodeSpec
import com.example.gridsurge.features.adventure.ui.AdventureViewModel
import com.example.gridsurge.features.adventure.ui.SectorMapScreen
import com.example.gridsurge.armory.data.ArmoryDataStoreRepository
import com.example.gridsurge.armory.ui.ArmoryViewModel
import com.example.gridsurge.armory.ui.CyberArmoryScreen
import com.example.gridsurge.leaderboard.data.LeaderboardRepository
import com.example.gridsurge.leaderboard.ui.CyberLeaderboardScreen
import com.example.gridsurge.leaderboard.ui.LeaderboardViewModel
import com.example.gridsurge.meta.PlayerProfileManager
import com.example.gridsurge.meta.data.DailyLoginRepository
import com.example.gridsurge.meta.data.DailyMissionsRepository
import com.example.gridsurge.ui.auth.CyberAuthScreen
import com.example.gridsurge.ui.career.CareerProgressScreen
import com.example.gridsurge.ui.dialogs.CyberProfileSetupDialog
import com.example.gridsurge.ui.quests.DailyMissionsScreen
import com.example.gridsurge.ui.quests.DailyMissionsViewModel
import com.example.gridsurge.ui.screens.TierAchievementsScreen
import com.example.gridsurge.ui.screens.StudioSplashScreen
import com.example.gridsurge.ui.store.CyberStoreScreen
import kotlinx.coroutines.launch

enum class Screen { STUDIO_SPLASH, MAIN_MENU, GAME_CLASSIC, GAME_ADVENTURE, ARMORY, CAREER, DAILY_GLITCH, TIME_BLITZ, BLITZ_CLASH, LEADERBOARD, QUESTS, SETTINGS, ADVENTURE_MAP, STORE, ACHIEVEMENTS, AUTH }

@Composable
fun NavigationRoot() {
    val context = LocalContext.current
    val profileManager = remember { PlayerProfileManager(context) }
    val armoryRepository = remember { ArmoryDataStoreRepository(context) }
    val adventureRepository = remember { AdventureRepository(context) }
    val dailyLoginRepository = remember { DailyLoginRepository(context, profileManager) }
    val dailyMissionsRepository = remember { DailyMissionsRepository(context) }
    val leaderboardRepository = remember { LeaderboardRepository() }
    
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.STUDIO_SPLASH) }
    var selectedLevelSpec by remember { mutableStateOf<LevelNodeSpec?>(null) }
    var pendingRelicMatrix by remember { mutableStateOf(false) }
    
    val stars by profileManager.starCurrency.collectAsState()

    BackHandler(enabled = currentScreen != Screen.MAIN_MENU) {
        currentScreen = Screen.MAIN_MENU
    }

    Crossfade(
        targetState = currentScreen,
        animationSpec = tween(durationMillis = 300),
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            Screen.STUDIO_SPLASH -> StudioSplashScreen(
                onSplashFinished = {
                    currentScreen = Screen.MAIN_MENU
                }
            )
            Screen.MAIN_MENU -> MainMenuScreen(
                profileManager = profileManager,
                armoryRepository = armoryRepository,
                dailyLoginRepository = dailyLoginRepository,
                onNavigate = { nextScreen ->
                    if (nextScreen == Screen.GAME_ADVENTURE) {
                        currentScreen = Screen.ADVENTURE_MAP
                    } else {
                        currentScreen = nextScreen
                    }
                }
            )
            Screen.GAME_CLASSIC -> {
                val advVm: AdventureViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return AdventureViewModel(adventureRepository, profileManager) as T
                        }
                    }
                )
                GameScreen(
                    profileManager = profileManager,
                    armoryRepository = armoryRepository,
                    dailyMissionsRepository = dailyMissionsRepository,
                    adventureViewModel = advVm,
                    gameMode = Screen.GAME_CLASSIC,
                    onNavigateBack = { _ -> currentScreen = Screen.MAIN_MENU }
                )
            }
            Screen.GAME_ADVENTURE -> {
                val advVm: AdventureViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return AdventureViewModel(adventureRepository, profileManager) as T
                        }
                    }
                )
                GameScreen(
                    profileManager = profileManager,
                    armoryRepository = armoryRepository,
                    dailyMissionsRepository = dailyMissionsRepository,
                    adventureViewModel = advVm,
                    gameMode = Screen.GAME_ADVENTURE,
                    adventureLevel = selectedLevelSpec,
                    onNavigateBack = { showRelic ->
                        pendingRelicMatrix = showRelic
                        currentScreen = Screen.ADVENTURE_MAP 
                    }
                )
            }
            Screen.ADVENTURE_MAP -> {
                val adventureViewModel: AdventureViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return AdventureViewModel(adventureRepository, profileManager) as T
                        }
                    }
                )
                val uiState by adventureViewModel.uiState.collectAsState()
                val activeAugmentIds by profileManager.activeSectorAugmentIds.collectAsState()
                val equippedRelicName by profileManager.equippedRelicAbilityName.collectAsState()
                
                SectorMapScreen(
                    currentSector = uiState.baseMapState.activeSector,
                    allSectors = uiState.baseMapState.allSectors,
                    progressMap = uiState.baseMapState.progressMap,
                    totalStarsCollected = uiState.baseMapState.totalStarsCollected,
                    claimedRelicReward = uiState.claimedRelicReward,
                    isRelicClaimed = uiState.sectorRecords[uiState.baseMapState.activeSector.sectorId]?.isRelicClaimed ?: false,
                    equippedRelicName = equippedRelicName,
                    activeAugmentIds = activeAugmentIds,
                    showMatrixOnArrival = pendingRelicMatrix,
                    onResetSectorRun = {
                        profileManager.clearActiveSectorAugments()
                    },
                    onLevelSelected = { node ->
                        if (node.levelInSector == 1) {
                            profileManager.clearActiveSectorAugments()
                        }
                        selectedLevelSpec = node
                        pendingRelicMatrix = false
                        currentScreen = Screen.GAME_ADVENTURE
                    },
                    onClaimRelic = { relic ->
                        adventureViewModel.claimSectorRelic(relic)
                    },
                    onSectorChanged = { sectorId ->
                        profileManager.clearActiveSectorAugments()
                        adventureViewModel.selectSector(sectorId)
                    },
                    onDismissReward = {
                        adventureViewModel.dismissRewardClaimDialog()
                    },
                    onEquipRelic = { relicAbilityName ->
                        profileManager.equipRelicAbility(relicAbilityName)
                    },
                    onNavigateBack = { currentScreen = Screen.MAIN_MENU }
                )
            }
            Screen.ARMORY -> {
                val armoryViewModel: ArmoryViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return ArmoryViewModel(profileManager) as T
                        }
                    }
                )
                CyberArmoryScreen(
                    viewModel = armoryViewModel,
                    onNavigateBack = { currentScreen = Screen.MAIN_MENU }
                )
            }
            Screen.STORE -> CyberStoreScreen(
                profileManager = profileManager,
                onBack = { currentScreen = Screen.MAIN_MENU }
            )
            Screen.QUESTS -> {
                val missionsVm: DailyMissionsViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return DailyMissionsViewModel(dailyMissionsRepository, profileManager) as T
                        }
                    }
                )
                DailyMissionsScreen(
                    viewModel = missionsVm,
                    profileManager = profileManager,
                    onNavigateToStore = { currentScreen = Screen.STORE },
                    onNavigateBack = { currentScreen = Screen.MAIN_MENU }
                )
            }
            Screen.SETTINGS -> {
                // Placeholder for Settings
                currentScreen = Screen.MAIN_MENU
            }
            Screen.CAREER -> CareerProgressScreen(
                onBack = { currentScreen = Screen.MAIN_MENU }
            )
            Screen.DAILY_GLITCH -> {
                val advVm: AdventureViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return AdventureViewModel(adventureRepository, profileManager) as T
                        }
                    }
                )
                GameScreen(
                    profileManager = profileManager,
                    armoryRepository = armoryRepository,
                    dailyMissionsRepository = dailyMissionsRepository,
                    adventureViewModel = advVm,
                    gameMode = Screen.DAILY_GLITCH,
                    onNavigateBack = { _ -> currentScreen = Screen.MAIN_MENU }
                )
            }
            Screen.TIME_BLITZ -> {
                val advVm: AdventureViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return AdventureViewModel(adventureRepository, profileManager) as T
                        }
                    }
                )
                GameScreen(
                    profileManager = profileManager,
                    armoryRepository = armoryRepository,
                    dailyMissionsRepository = dailyMissionsRepository,
                    adventureViewModel = advVm,
                    gameMode = Screen.TIME_BLITZ,
                    onNavigateBack = { _ -> currentScreen = Screen.MAIN_MENU }
                )
            }
            Screen.BLITZ_CLASH -> {
                val hasConfiguredProfile by profileManager.hasConfiguredProfile.collectAsState()
                var showProfileSetupModal by remember { mutableStateOf(!hasConfiguredProfile) }

                Box(modifier = Modifier.fillMaxSize()) {
                    GameScreen(
                        profileManager = profileManager,
                        armoryRepository = armoryRepository,
                        dailyMissionsRepository = dailyMissionsRepository,
                        adventureViewModel = viewModel(),
                        gameMode = Screen.BLITZ_CLASH,
                        onNavigateBack = { _ -> currentScreen = Screen.MAIN_MENU }
                    )

                    if (showProfileSetupModal) {
                        CyberProfileSetupDialog(
                            profileManager = profileManager,
                            isPvpRequiredNotice = true,
                            onProfileInitialized = {
                                showProfileSetupModal = false
                            },
                            onDismiss = {
                                currentScreen = Screen.MAIN_MENU
                            }
                        )
                    }
                }
            }
            Screen.ACHIEVEMENTS -> {
                TierAchievementsScreen(
                    profileManager = profileManager,
                    onBackToHub = { currentScreen = Screen.MAIN_MENU }
                )
            }
            Screen.AUTH -> {
                CyberAuthScreen(
                    profileManager = profileManager,
                    onNavigateBack = { currentScreen = Screen.MAIN_MENU }
                )
            }
            Screen.LEADERBOARD -> {
                val leaderboardVm: LeaderboardViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return LeaderboardViewModel(leaderboardRepository, profileManager, profileManager.callsign.value) as T
                        }
                    }
                )
                CyberLeaderboardScreen(
                    viewModel = leaderboardVm,
                    onNavigateBack = { currentScreen = Screen.MAIN_MENU }
                )
            }
        }
    }
}
