package com.example.gridsurge.game.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ModalType {
    NONE,
    PAUSE,
    SETTINGS,
    GAME_OVER,
    VICTORY,
    AUGMENT_DRAFT,
    CLASH_RESULT,
    REPLAY_THEATER
}

object ModalOrchestrator {
    private val queue = mutableListOf<ModalType>()
    private val _currentModal = MutableStateFlow(ModalType.NONE)
    val currentModal: StateFlow<ModalType> = _currentModal.asStateFlow()

    fun showModal(type: ModalType) {
        if (type == ModalType.NONE) return
        queue.removeAll { it == type || it == ModalType.NONE }
        queue.add(0, type)
        _currentModal.value = type
    }

    fun dismissModal(type: ModalType) {
        queue.removeAll { it == type || it == ModalType.NONE }
        _currentModal.value = queue.firstOrNull() ?: ModalType.NONE
    }

    fun clearAll() {
        queue.clear()
        _currentModal.value = ModalType.NONE
    }
}
