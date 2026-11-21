package com.aydanailroom.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aydanailroom.app.ui.theme.ChalkColors

@Composable
fun ChalkFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    listOf(ChalkColors.WoodShade, ChalkColors.Wood, ChalkColors.WoodLight, ChalkColors.Wood),
                ),
                RoundedCornerShape(28.dp),
            )
            .padding(8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(ChalkColors.BoardLight, ChalkColors.Board, ChalkColors.BoardDeep),
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val line = 1.dp.toPx()
            drawRoundRect(
                color = ChalkColors.WoodLight.copy(alpha = 0.34f),
                style = Stroke(line),
                topLeft = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 4.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
            )
        }
        content()
    }
}

@Composable
fun ChalkCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val base = Modifier
        .clip(shape)
        .background(ChalkColors.BoardDeep.copy(alpha = 0.94f))
        .drawWithContent {
            drawContent()
            val inset = 1.5.dp.toPx()
            val radius = 17.dp.toPx()
            val canvasWidth = size.width
            val canvasHeight = size.height
            val wobbleA = 0.7.dp.toPx()
            val wobbleB = 0.45.dp.toPx()
            val wobbleC = 0.65.dp.toPx()
            val wobbleD = 0.35.dp.toPx()
            val wobbleE = 0.55.dp.toPx()
            val wobbleF = 0.4.dp.toPx()
            val path = Path().apply {
                val left = inset
                val top = inset
                val right = canvasWidth - inset
                val bottom = canvasHeight - inset
                moveTo(left + radius, top)
                val horizontalSteps = 18
                for (index in 1..horizontalSteps) {
                    val x = left + radius + (right - left - radius * 2f) * index / horizontalSteps
                    val wobble = if (index % 2 == 0) wobbleA else -wobbleB
                    lineTo(x, top + wobble)
                }
                lineTo(right - radius, top)
                quadraticTo(right, top, right, top + radius)
                val verticalSteps = 12
                for (index in 1..verticalSteps) {
                    val y = top + radius + (bottom - top - radius * 2f) * index / verticalSteps
                    val wobble = if (index % 2 == 0) wobbleC else -wobbleD
                    lineTo(right + wobble, y)
                }
                lineTo(right, bottom - radius)
                quadraticTo(right, bottom, right - radius, bottom)
                for (index in 1..horizontalSteps) {
                    val x = right - radius - (right - left - radius * 2f) * index / horizontalSteps
                    val wobble = if (index % 2 == 0) wobbleE else -wobbleF
                    lineTo(x, bottom + wobble)
                }
                lineTo(left + radius, bottom)
                quadraticTo(left, bottom, left, bottom - radius)
                for (index in 1..verticalSteps) {
                    val y = bottom - radius - (bottom - top - radius * 2f) * index / verticalSteps
                    val wobble = if (index % 2 == 0) wobbleE else -wobbleF
                    lineTo(left + wobble, y)
                }
                lineTo(left, top + radius)
                quadraticTo(left, top, left + radius, top)
                close()
            }
            drawPath(path, color = ChalkColors.ChalkDim.copy(alpha = 0.72f), style = Stroke(1.4.dp.toPx()))
        }
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(contentPadding)

    Column(modifier = modifier.then(base), content = content)
}

@Composable
fun ChalkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondary: Boolean = false,
) {
    val background = if (secondary) ChalkColors.BoardLight else ChalkColors.Pink
    val foreground = if (secondary) ChalkColors.Chalk else ChalkColors.BoardDeep
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = 56.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ChalkColors.Chalk.copy(alpha = 0.82f)),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = foreground,
            disabledContainerColor = background.copy(alpha = 0.4f),
            disabledContentColor = foreground.copy(alpha = 0.5f),
        ),
    ) {
        Text(text, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ChalkChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 44.dp),
        label = { Text(text) },
        shape = RoundedCornerShape(14.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = ChalkColors.ChalkDim.copy(alpha = 0.7f),
            selectedBorderColor = ChalkColors.Yellow,
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = ChalkColors.BoardDeep,
            labelColor = ChalkColors.ChalkDim,
            selectedContainerColor = ChalkColors.Pink.copy(alpha = 0.22f),
            selectedLabelColor = ChalkColors.Chalk,
        ),
    )
}

@Composable
fun ChalkField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        textStyle = textStyle,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ChalkColors.Chalk,
            unfocusedTextColor = ChalkColors.Chalk,
            focusedBorderColor = ChalkColors.Pink,
            unfocusedBorderColor = ChalkColors.ChalkDim.copy(alpha = 0.65f),
            focusedLabelColor = ChalkColors.Pink,
            unfocusedLabelColor = ChalkColors.ChalkDim,
            cursorColor = ChalkColors.Yellow,
            focusedContainerColor = ChalkColors.BoardLight.copy(alpha = 0.55f),
            unfocusedContainerColor = ChalkColors.BoardDeep.copy(alpha = 0.35f),
        ),
    )
}

@Composable
fun ChalkDivider(modifier: Modifier = Modifier, colorAlpha: Float = 0.75f) {
    Canvas(modifier.fillMaxWidth().height(8.dp)) {
        val dash = 4.dp.toPx()
        drawLine(
            color = ChalkColors.ChalkDim.copy(alpha = colorAlpha),
            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash * 1.5f)),
        )
    }
}

@Composable
fun ChalkEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    doodle: String = "✿",
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(doodle, color = ChalkColors.Pink, fontSize = 42.sp, textAlign = TextAlign.Center)
        Text(
            text = message,
            color = ChalkColors.ChalkDim,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ChalkIconPill(
    text: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = ChalkColors.BoardLight,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ChalkColors.ChalkDim.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Text(text, color = ChalkColors.Chalk, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        }
    }
}
