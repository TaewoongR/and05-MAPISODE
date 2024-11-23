package com.boostcamp.mapisode.mygroup.intent

import androidx.compose.runtime.Immutable
import com.boostcamp.mapisode.model.GroupItem
import com.boostcamp.mapisode.ui.base.UiState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class GroupState(
	val areGroupsLoading: Boolean = false,
	val groups: PersistentList<GroupItem> = persistentListOf(),
) : UiState
