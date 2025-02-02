package com.boostcamp.mapisode.login

import androidx.lifecycle.viewModelScope
import com.boostcamp.mapisode.auth.GoogleOauth
import com.boostcamp.mapisode.auth.LoginState
import com.boostcamp.mapisode.datastore.UserPreferenceDataStore
import com.boostcamp.mapisode.model.GroupModel
import com.boostcamp.mapisode.model.UserModel
import com.boostcamp.mapisode.mygroup.GroupRepository
import com.boostcamp.mapisode.ui.base.BaseViewModel
import com.boostcamp.mapisode.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
	private val userRepository: UserRepository,
	private val groupRepository: GroupRepository,
	private val userDataStore: UserPreferenceDataStore,
) : BaseViewModel<AuthIntent, AuthState, AuthSideEffect>(AuthState()) {

	override fun onIntent(intent: AuthIntent) {
		when (intent) {
			is AuthIntent.Init -> onInit()
			is AuthIntent.OnGoogleSignInClick -> handleGoogleSignIn(intent.googleOauth)
			is AuthIntent.OnNicknameChange -> onNicknameChange(intent.nickname)
			is AuthIntent.OnProfileUrlChange -> onProfileUrlChange(intent.profileUrl)
			is AuthIntent.OnSignUpClick -> handleSignUp()
			is AuthIntent.OnLoginSuccess -> handleLoginSuccess()
			is AuthIntent.OnBackClickedInSignUp -> onBackClickedInSignUp()
			is AuthIntent.OnPhotopickerClick -> handlePhotopickerClick()
		}
	}

	private fun onInit() {
		handleAutoLogin()
	}

	private fun onBackClickedInSignUp() {
		intent {
			copy(
				isLoginSuccess = false,
				nickname = "",
				profileUrl = "",
			)
		}
	}

	private fun handleAutoLogin() {
		viewModelScope.launch {
			if (userDataStore.checkLoggedIn()) {
				onIntent(AuthIntent.OnLoginSuccess)
			} else {
				intent {
					copy(showSplash = false)
				}
			}
		}
	}

	private suspend fun isUserExist(uid: String): Boolean = try {
		userRepository.isUserExist(uid)
	} catch (e: Exception) {
		false
	}

	private suspend fun getRecentSelectedGroup(): String? = try {
		userDataStore.getRecentSelectedGroup().firstOrNull()
	} catch (e: Exception) {
		null
	}

	private fun handleGoogleSignIn(googleOauth: GoogleOauth) {
		viewModelScope.launch {
			try {
				googleOauth.googleSignIn().collect { loginState ->
					when (loginState) {
						is LoginState.Success -> {
							if (isUserExist(loginState.authDataInfo.uid)) {
								val user = getUserInfo(loginState.authDataInfo.uid)
								val recentGroup = getRecentSelectedGroup() ?: user.uid

								storeUserData(
									userModel = user,
									credentialId = loginState.authDataInfo.idToken,
									recentGroup = recentGroup,
								)
								onIntent(AuthIntent.OnLoginSuccess)
								return@collect
							}

							intent {
								copy(
									isLoginSuccess = true,
									authData = loginState.authDataInfo,
								)
							}
						}

						is LoginState.Cancel -> Unit

						is LoginState.Error -> {
							postSideEffect(AuthSideEffect.ShowToast(R.string.login_auth_failed))
						}
					}
				}
			} catch (e: Exception) {
				postSideEffect(AuthSideEffect.ShowToast(R.string.login_auth_failed))
			}
		}
	}

	private fun onNicknameChange(nickname: String) {
		intent {
			copy(
				nickname = nickname,
			)
		}
	}

	private fun onProfileUrlChange(profileUrl: String) {
		intent {
			copy(
				profileUrl = profileUrl,
			)
		}
	}

	private fun handleSignUp() {
		intent {
			copy(isLoading = true)
		}

		viewModelScope.launch {
			try {
				if (currentState.nickname.isBlank()) throw IllegalArgumentException("닉네임을 입력해주세요.")
				if (currentState.profileUrl.isBlank()) throw IllegalArgumentException("프로필 사진을 선택해주세요.")
				if (currentState.authData == null) throw IllegalArgumentException("로그인 정보가 없습니다.")

				val localUri = currentState.profileUrl
				val storageUrl = getStorageUrl()
				onProfileUrlChange(storageUrl)

				val user = UserModel(
					uid = currentState.authData?.uid
						?: throw IllegalArgumentException("UID cannot be empty"),
					email = currentState.authData?.email
						?: throw IllegalArgumentException("Email cannot be empty"),
					name = currentState.nickname,
					profileUrl = currentState.profileUrl,
					joinedAt = Date.from(java.time.Instant.now()),
					groups = emptyList(),
				)

				userRepository.createUser(user)
				createMyEpisodeGroup(
					user.copy(
						profileUrl = localUri,
					),
				)

				storeUserData(
					userModel = user,
					credentialId = currentState.authData?.idToken ?: throw IllegalArgumentException(
						"로그인 정보가 없습니다.",
					),
					recentGroup = user.uid,
				)

				onIntent(AuthIntent.OnLoginSuccess)
			} catch (e: Exception) {
				postSideEffect(AuthSideEffect.ShowToast(R.string.login_signup_failed))
			}
		}
	}

	private suspend fun getStorageUrl(): String {
		try {
			return userRepository.uploadProfileImageToStorage(
				imageUri = currentState.profileUrl,
				uid = currentState.authData?.uid
					?: throw IllegalArgumentException("UID cannot be empty"),
			)
		} catch (e: Exception) {
			throw e
		}
	}

	private suspend fun getUserInfo(uid: String): UserModel = try {
		userRepository.getUserInfo(uid)
	} catch (e: Exception) {
		throw Exception("Failed to get user", e)
	}

	private fun storeUserData(
		userModel: UserModel,
		credentialId: String,
		recentGroup: String,
	) {
		viewModelScope.launch {
			with(userDataStore) {
				updateUserId(userModel.uid)
				updateUsername(userModel.name)
				updateProfileUrl(userModel.profileUrl)
				updateIsFirstLaunch()
				updateCredentialIdToken(credentialId)
				updateRecentSelectedGroup(recentGroup)
			}
		}
	}

	private suspend fun createMyEpisodeGroup(user: UserModel) {
		viewModelScope.launch {
			groupRepository.createGroup(
				GroupModel(
					id = user.uid,
					adminUser = user.uid,
					createdAt = Date.from(java.time.Instant.now()),
					description = "내가 작성한 에피소드",
					imageUrl = user.profileUrl,
					name = "\uD83D\uDC51 나만의 에피소드",
					members = listOf(user.uid),
				),
			)
		}.join()
	}

	private fun handleLoginSuccess() {
		viewModelScope.launch {
			userDataStore.updateIsLoggedIn(true)
		}
		postSideEffect(AuthSideEffect.NavigateToMain)
	}

	private fun handlePhotopickerClick() {
		intent {
			copy(isPhotopickerClicked = !isPhotopickerClicked)
		}
	}
}
