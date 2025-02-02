package com.boostcamp.mapisode.mygroup.viewmodel

import androidx.lifecycle.viewModelScope
import com.boostcamp.mapisode.datastore.UserPreferenceDataStore
import com.boostcamp.mapisode.mygroup.GroupRepository
import com.boostcamp.mapisode.mygroup.R
import com.boostcamp.mapisode.mygroup.intent.GroupJoinIntent
import com.boostcamp.mapisode.mygroup.model.toGroupCreationModel
import com.boostcamp.mapisode.mygroup.sideeffect.GroupJoinSideEffect
import com.boostcamp.mapisode.mygroup.state.GroupJoinState
import com.boostcamp.mapisode.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupJoinViewModel @Inject constructor(
	private val groupRepository: GroupRepository,
	private val userPreferenceDataStore: UserPreferenceDataStore,
) : BaseViewModel<GroupJoinIntent, GroupJoinState, GroupJoinSideEffect>(GroupJoinState()) {
	private val myId: MutableStateFlow<String> = MutableStateFlow("")

	init {
		observeUserId()
	}

	private fun observeUserId() {
		viewModelScope.launch {
			userPreferenceDataStore.getUserId().collect { userId ->
				myId.value = userId ?: ""
			}
		}
	}

	override fun onIntent(intent: GroupJoinIntent) {
		when (intent) {
			is GroupJoinIntent.TryGetGroup -> {
				tryGetGroupByGroupId(intent.inviteCode)
			}

			is GroupJoinIntent.OnJoinClick -> {
				joinGroup()
			}

			is GroupJoinIntent.OnBackClick -> {
				postSideEffect(GroupJoinSideEffect.NavigateToGroupScreen)
			}
		}
	}

	private fun tryGetGroupByGroupId(inviteCodes: String) {
		viewModelScope.launch {
			intent { copy(isGroupLoading = true) }
			try {
				val group = groupRepository.getGroupByInviteCodes(inviteCodes)
				intent { copy(isGroupExist = true, group = group.toGroupCreationModel()) }
			} catch (e: Exception) {
				intent { copy(isGroupExist = false) }
				postSideEffect(GroupJoinSideEffect.ShowToast(R.string.group_join_not_exist))
			}
		}
	}

	private fun joinGroup() {
		viewModelScope.launch {
			val userId = myId.value
			val group = currentState.group ?: return@launch
			intent { copy(isGroupLoading = true) }
			try {
				groupRepository.joinGroup(userId, group.id)
				intent { copy(isJoinedSuccess = true) }
				postSideEffect(GroupJoinSideEffect.ShowToast(R.string.group_join_success))
			} catch (e: NullPointerException) {
				intent { copy(isGroupLoading = false, isJoinedSuccess = false, group = null) }
				postSideEffect(GroupJoinSideEffect.ShowToast(R.string.message_already_joined))
			} catch (e: Exception) {
				intent { copy(isGroupLoading = false, isJoinedSuccess = false, group = null) }
				postSideEffect(GroupJoinSideEffect.ShowToast(R.string.group_join_failure))
			}
			delay(100)
			postSideEffect(GroupJoinSideEffect.NavigateToGroupScreen)
		}
	}
}
