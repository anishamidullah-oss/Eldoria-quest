package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TouchControls(
    modifier: Modifier = Modifier,
    onMove: (x: Float, y: Float) -> Unit,
    onJump: () -> Unit,
    onJumpRelease: (() -> Unit)? = null,
    onAttack: () -> Unit,
    interactLabel: String? = null,
    onInteract: (() -> Unit)? = null
) {
    var isLeftPressed by remember { mutableStateOf(false) }
    var isRightPressed by remember { mutableStateOf(false) }
    var isUpPressed by remember { mutableStateOf(false) }
    var isDownPressed by remember { mutableStateOf(false) }

    fun updateMovement(
        left: Boolean = isLeftPressed,
        right: Boolean = isRightPressed,
        up: Boolean = isUpPressed,
        down: Boolean = isDownPressed
    ) {
        val mx = when {
            left && !right -> -1f
            right && !left -> 1f
            else -> 0f
        }
        val my = when {
            up && !down -> -1f
            down && !up -> 1f
            else -> 0f
        }
        onMove(mx, my)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Left Side: Translucent Directional Buttons (Left & Right pill buttons + ladder climb)
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Movement Button
            TranslucentFrostedButton(
                testTag = "control_left",
                widthDp = 74,
                heightDp = 54,
                isPressed = isLeftPressed,
                onPressChange = { pressed ->
                    isLeftPressed = pressed
                    updateMovement(left = pressed)
                }
            ) { w, h, pressed ->
                val arrowPath = Path().apply {
                    moveTo(w * 0.72f, h * 0.28f)
                    cubicTo(w * 0.62f, h * 0.42f, w * 0.48f, h * 0.46f, w * 0.38f, h * 0.5f)
                    lineTo(w * 0.48f, h * 0.32f)
                    lineTo(w * 0.22f, h * 0.5f)
                    lineTo(w * 0.48f, h * 0.68f)
                    lineTo(w * 0.38f, h * 0.5f)
                    cubicTo(w * 0.52f, h * 0.54f, w * 0.68f, h * 0.62f, w * 0.72f, h * 0.72f)
                    close()
                }
                val tint = if (pressed) Color(0xFFFFFFFF) else Color(0xCCECEFF1)
                drawPath(arrowPath, color = tint, style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(arrowPath, color = tint.copy(alpha = 0.4f), style = Fill)
            }

            // Up/Down Ladder Assist
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SmallClimbButton(
                    icon = Icons.Default.ArrowUpward,
                    testTag = "control_up",
                    isPressed = isUpPressed,
                    onPressChange = { pressed ->
                        isUpPressed = pressed
                        updateMovement(up = pressed)
                    }
                )
                SmallClimbButton(
                    icon = Icons.Default.ArrowDownward,
                    testTag = "control_down",
                    isPressed = isDownPressed,
                    onPressChange = { pressed ->
                        isDownPressed = pressed
                        updateMovement(down = pressed)
                    }
                )
            }

            // Right Movement Button
            TranslucentFrostedButton(
                testTag = "control_right",
                widthDp = 74,
                heightDp = 54,
                isPressed = isRightPressed,
                onPressChange = { pressed ->
                    isRightPressed = pressed
                    updateMovement(right = pressed)
                }
            ) { w, h, pressed ->
                val arrowPath = Path().apply {
                    moveTo(w * 0.28f, h * 0.28f)
                    cubicTo(w * 0.38f, h * 0.42f, w * 0.52f, h * 0.46f, w * 0.62f, h * 0.5f)
                    lineTo(w * 0.52f, h * 0.32f)
                    lineTo(w * 0.78f, h * 0.5f)
                    lineTo(w * 0.52f, h * 0.68f)
                    lineTo(w * 0.62f, h * 0.5f)
                    cubicTo(w * 0.48f, h * 0.54f, w * 0.32f, h * 0.62f, w * 0.28f, h * 0.72f)
                    close()
                }
                val tint = if (pressed) Color(0xFFFFFFFF) else Color(0xCCECEFF1)
                drawPath(arrowPath, color = tint, style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(arrowPath, color = tint.copy(alpha = 0.4f), style = Fill)
            }
        }

        // Right Side: Action Cluster
        Column(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Contextual Interaction Prompt Button (Talk / Read / Open)
            if (interactLabel != null && onInteract != null) {
                Box(
                    modifier = Modifier
                        .testTag("interact_button")
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xE6FFB300))
                        .border(2.dp, Color(0xFFFFF9C4), RoundedCornerShape(20.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onInteract() })
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Interact",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = interactLabel,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Attack & Jump Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Curved Scimitar / Sword Attack Button
                TranslucentFrostedButton(
                    testTag = "control_attack",
                    widthDp = 82,
                    heightDp = 58,
                    onTap = onAttack
                ) { w, h, pressed ->
                    val tint = if (pressed) Color(0xFFFFD54F) else Color(0xDDFFFFFF)
                    val bladePath = Path().apply {
                        moveTo(w * 0.24f, h * 0.72f)
                        lineTo(w * 0.32f, h * 0.64f)
                        moveTo(w * 0.26f, h * 0.58f)
                        lineTo(w * 0.38f, h * 0.70f)
                        moveTo(w * 0.32f, h * 0.64f)
                        cubicTo(w * 0.46f, h * 0.58f, w * 0.62f, h * 0.48f, w * 0.76f, h * 0.30f)
                        cubicTo(w * 0.66f, h * 0.38f, w * 0.48f, h * 0.44f, w * 0.32f, h * 0.64f)
                        close()
                    }
                    drawPath(bladePath, color = tint, style = Stroke(width = 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(bladePath, color = tint.copy(alpha = 0.35f), style = Fill)
                    drawCircle(color = tint, radius = 2f, center = Offset(w * 0.76f, h * 0.30f))
                }

                // Curved Upward-Hook Jump Button
                TranslucentFrostedButton(
                    testTag = "control_jump",
                    widthDp = 82,
                    heightDp = 58,
                    onPressChange = { pressed ->
                        if (pressed) {
                            onJump()
                        } else {
                            onJumpRelease?.invoke()
                        }
                    }
                ) { w, h, pressed ->
                    val tint = if (pressed) Color(0xFF64B5F6) else Color(0xDDFFFFFF)
                    val jumpPath = Path().apply {
                        moveTo(w * 0.30f, h * 0.70f)
                        cubicTo(w * 0.40f, h * 0.70f, w * 0.58f, h * 0.65f, w * 0.62f, h * 0.42f)
                        lineTo(w * 0.50f, h * 0.44f)
                        lineTo(w * 0.68f, h * 0.26f)
                        lineTo(w * 0.78f, h * 0.48f)
                        lineTo(w * 0.66f, h * 0.46f)
                        cubicTo(w * 0.60f, h * 0.74f, w * 0.42f, h * 0.78f, w * 0.30f, h * 0.78f)
                        close()
                    }
                    drawPath(jumpPath, color = tint, style = Stroke(width = 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(jumpPath, color = tint.copy(alpha = 0.35f), style = Fill)
                }
            }
        }
    }
}

@Composable
private fun TranslucentFrostedButton(
    testTag: String,
    widthDp: Int = 76,
    heightDp: Int = 54,
    isPressed: Boolean = false,
    onPressChange: ((Boolean) -> Unit)? = null,
    onTap: (() -> Unit)? = null,
    drawIcon: androidx.compose.ui.graphics.drawscope.DrawScope.(Float, Float, Boolean) -> Unit
) {
    var internalPressed by remember { mutableStateOf(false) }
    val pressed = isPressed || internalPressed

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .size(widthDp.dp, heightDp.dp)
            .testTag(testTag)
            .clip(shape)
            .background(
                if (pressed) Color(0x66FFFFFF) else Color(0x33000000)
            )
            .border(
                width = 2.dp,
                color = if (pressed) Color(0xDDFFFFFF) else Color(0x66FFFFFF),
                shape = shape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        internalPressed = true
                        onPressChange?.invoke(true)
                        onTap?.invoke()
                        tryAwaitRelease()
                        internalPressed = false
                        onPressChange?.invoke(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIcon(size.width, size.height, pressed)
        }
    }
}

@Composable
private fun SmallClimbButton(
    icon: ImageVector,
    testTag: String,
    isPressed: Boolean,
    onPressChange: (Boolean) -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(36.dp, 28.dp)
            .testTag(testTag)
            .clip(shape)
            .background(if (isPressed) Color(0x66FFFFFF) else Color(0x28000000))
            .border(1.2.dp, if (isPressed) Color.White else Color(0x55FFFFFF), shape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = testTag,
            tint = if (isPressed) Color.White else Color(0xCCFFFFFF),
            modifier = Modifier.size(18.dp)
        )
    }
}
