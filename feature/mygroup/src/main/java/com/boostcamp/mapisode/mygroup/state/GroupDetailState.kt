package com.boostcamp.mapisode.mygroup.state

import androidx.compose.runtime.Immutable
import com.boostcamp.mapisode.model.EpisodeModel
import com.boostcamp.mapisode.model.GroupMemberModel
import com.boostcamp.mapisode.model.GroupModel
import com.boostcamp.mapisode.ui.base.UiState

@Immutable
data class GroupDetailState(
	val isGroupIdCaching: Boolean = true,
	val isGroupLoading: Boolean = false,
	val group: GroupModel? = null,
	val membersInfo: List<GroupMemberModel> = emptyList(),
	val episodes: List<EpisodeModel> = emptyList(),
) : UiState
