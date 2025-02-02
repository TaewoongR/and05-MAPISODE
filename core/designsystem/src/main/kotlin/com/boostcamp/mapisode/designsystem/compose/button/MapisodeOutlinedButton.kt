package com.boostcamp.mapisode.designsystem.compose.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boostcamp.mapisode.designsystem.compose.IconSize
import com.boostcamp.mapisode.designsystem.compose.MapisodeIcon
import com.boostcamp.mapisode.designsystem.compose.MapisodeText
import com.boostcamp.mapisode.designsystem.theme.MapisodeTheme

@Composable
fun MapisodeOutlinedButton(
	modifier: Modifier = Modifier,
	borderColor: Color = MapisodeTheme.colorScheme.outlineButtonStroke,
	contentColor: Color = MapisodeTheme.colorScheme.outlineButtonContent,
	onClick: () -> Unit,
	text: String,
	enabled: Boolean = true,
	@DrawableRes leftIcon: Int? = null,
	@DrawableRes rightIcon: Int? = null,
	showRipple: Boolean = false,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
	MapisodeButton(
		onClick = onClick,
		modifier = Modifier
			.then(modifier)
			.widthIn(320.dp)
			.heightIn(40.dp),
		backgroundColors = MapisodeTheme.colorScheme.outlineButtonBackground,
		contentColor = contentColor,
		enabled = enabled,
		showBorder = true,
		borderColor = borderColor,
		showRipple = showRipple,
		interactionSource = interactionSource,
		rounding = 8.dp,
		contentPadding = PaddingValues(horizontal = 16.dp),
	) {
		leftIcon?.let { icon ->
			MapisodeIcon(
				id = icon,
				iconSize = IconSize.Small,
			)
			Spacer(modifier = Modifier.width(8.dp))
		}

		MapisodeText(
			text = text,
			color = contentColor,
			style = MapisodeTheme.typography.labelLarge,
		)

		leftIcon ?: rightIcon?.let { icon ->
			Spacer(modifier = Modifier.width(8.dp))
			MapisodeIcon(
				id = icon,
				iconSize = IconSize.Small,
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun MapisodeOutlinedButtonPreview() {
	Column(
		modifier = Modifier.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		MapisodeOutlinedButton(
			onClick = { },
			text = "아웃라인 버튼",
		)
	}
}
