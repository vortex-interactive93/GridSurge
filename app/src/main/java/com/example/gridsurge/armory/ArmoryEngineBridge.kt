package com.example.gridsurge.armory

import com.example.gridsurge.armory.model.ArmoryCatalog
import com.example.gridsurge.armory.model.ArmoryUserState
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.audio.VoxAction
import com.example.gridsurge.game.GridSurgeGameView
import com.example.gridsurge.theme.ThemeNormalizer

object ArmoryEngineBridge {

    fun applyEquippedState(userState: ArmoryUserState, gameView: GridSurgeGameView?) {
        // 1. Sync Block Skin Theme with Normalization
        val canonicalSkin = ThemeNormalizer.normalize(userState.equippedBlockSkinId)
        if (gameView != null) {
            gameView.activeThemeKey = canonicalSkin
            gameView.setTheme(canonicalSkin)
        }

        // 2. Sync VOX Announcer Pack
        val voxItem = ArmoryCatalog.findItemById(userState.equippedVoxPackId)
        if (voxItem != null) {
            SfxManager.activeVoxPackKey = voxItem.themeKey
        }
    }

    fun auditionVoxPack(voxItemKey: String) {
        SfxManager.activeVoxPackKey = voxItemKey
        SfxManager.playVox(VoxAction.CATALYST_DETONATED)
        SfxManager.playSfx(SfxType.SNAP_TICK, overridePitch = 1.2f)
    }
}
