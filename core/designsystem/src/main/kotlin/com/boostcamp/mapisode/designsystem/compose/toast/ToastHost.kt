package com.boostcamp.mapisode.designsystem.compose.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

interface ToastData {
	val message: String
	val duration: ToastDuration

	fun dismiss()
}

enum class ToastResult {
	Dismissed,
}

enum class ToastDuration(val milli: Long) {
	NORMAL(1500L),
}

@Stable
class ToastHostState {
	private val mutex = Mutex()

	var currentToastData by mutableStateOf<ToastData?>(null)
		private set

	suspend fun showToast(
		message: String,
		duration: ToastDuration = ToastDuration.NORMAL,
	): ToastResult = mutex.withLock {
		try {
			return suspendCancellableCoroutine { continuation ->
				currentToastData = ToastDataImpl(message, duration, continuation)
			}
		} finally {
			currentToastData = null
		}
	}
}

@Composable
fun rememberToastHostState(): ToastHostState = remember { ToastHostState() }

@Stable
private class ToastDataImpl(
	override val message: String,
	override val duration: ToastDuration,
	private val continuation: CancellableContinuation<ToastResult>,
) : ToastData {

	override fun dismiss() {
		if (continuation.isActive) {
			continuation.resume(ToastResult.Dismissed)
		}
	}
}

@Composable
fun ToastHost(
	toastHostState: ToastHostState,
	modifier: Modifier = Modifier,
	toast: @Composable (ToastData) -> Unit = { Toast(toastData = it) },
) {
	val currentToastData = toastHostState.currentToastData
	LaunchedEffect(currentToastData) {
		if (currentToastData != null) {
			delay(currentToastData.duration.milli)
			currentToastData.dismiss()
		}
	}
	FadeInFadeOut(
		newToastData = toastHostState.currentToastData,
		modifier = modifier,
		toast = toast,
	)
}
