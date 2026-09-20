package com.elvan.udukkai.ui.screens.onboarding.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Handles the staggered entrance animation from Flutter/React:
 * opacity 0 -> 1 (easeOut)
 * translateY 20dp -> 0dp (easeOutBack with spring overshoot bounce)
 */
@Composable
fun AuthAnimatedElement(
    delayIndex: Int = 1,
    durationMillis: Int = 800,
    content: @Composable () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val translateYAnim = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(delayIndex * 100L)
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            // CubicBezierEasing matching Flutter's Curves.easeOutBack (overshoot bounce)
            translateYAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = CubicBezierEasing(0.175f, 0.885f, 0.32f, 1.275f)
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .alpha(alphaAnim.value)
            .offset(y = translateYAnim.value.dp)
            .fillMaxWidth()
    ) {
        content()
    }
}

/**
 * The main layout matching AuthLayout in Flutter 1:1.
 * Renders the 4 animated floating and rotating background geometric shapes
 * with responsive screen-percentage sizes and positions.
 */
@Composable
fun AuthLayout(
    showBranding: Boolean = false,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = ThemeManager.isDark()
    val bgColor = if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFAFA)
    val shapeColor = if (isDark) Color.White.copy(alpha = 0.03f) else Color(0xFFEAEAEA)

    val infiniteTransition = rememberInfiniteTransition(label = "auth_background_anim")

    // Shape 1 Rotate: 60s linear infinite
    val rotate1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate1"
    )

    // Shape 3 Rotate: 40s linear infinite reverse
    val rotate2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate2"
    )

    // Shape 2 Float: 4s ease-in-out alternate (0 -> 30px)
    val float1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )

    // Shape 4 Float: 5s ease-in-out alternate (30px -> 0px)
    val float2 by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // Shape 1: Top Right, Large Rounded Square (50% screen width, radius 80dp, rotate 60s)
        val s1Size = screenW * 0.5f
        Box(
            modifier = Modifier
                .offset(
                    x = screenW * 0.6f,
                    y = screenH * 0.02f
                )
                .size(s1Size)
                .rotate(rotate1)
                .background(shapeColor, RoundedCornerShape(80.dp))
        )

        // Shape 2: Bottom Left, Circle (70% screen width, float 4s)
        val s2Size = screenW * 0.7f
        Box(
            modifier = Modifier
                .offset(
                    x = -screenW * 0.1f,
                    y = screenH * 0.65f + float1.dp
                )
                .size(s2Size)
                .background(shapeColor, CircleShape)
        )

        // Shape 3: Top Left, Small Rounded Square (20% screen width, radius 30dp, reverse rotate 40s)
        val s3Size = screenW * 0.2f
        Box(
            modifier = Modifier
                .offset(
                    x = screenW * 0.05f,
                    y = screenH * 0.15f
                )
                .size(s3Size)
                .rotate(rotate2)
                .background(shapeColor, RoundedCornerShape(30.dp))
        )

        // Shape 4: Center Right, Small Circle (15% screen width, float 5s)
        val s4Size = screenW * 0.15f
        Box(
            modifier = Modifier
                .offset(
                    x = screenW * 0.85f,
                    y = screenH * 0.60f + float2.dp
                )
                .size(s4Size)
                .background(shapeColor, CircleShape)
        )

        // Centered Scrollable Main Content matching Flutter's ConstrainedBox(maxWidth: 480)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                content()
            }
        }

        // Global Branding Signature matching Flutter
        if (showBranding) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = K.elvanParthasarathy.tr(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.2.sp,
                    color = (if (isDark) Color.White else Color(0xFF1D1D1F)).copy(alpha = 0.45f)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            floatingActionButton()
        }
    }
}

/**
 * Header section matching Flutter's AuthHeader:
 * Title (28sp ExtraBold -0.5 spacing) + Subtitle (16sp normal)
 */
@Composable
fun AuthHeader(
    title: String,
    subtitle: String = ""
) {
    val isDark = ThemeManager.isDark()
    val titleColor = LocalShellColors.current.textPrimary
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF666666)

    AuthAnimatedElement(delayIndex = 1) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = titleColor
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = subtitleColor
                )
            }
        }
    }
}

/**
 * Text Input matching Flutter's AuthInput 1:1:
 * - Label above field (14sp Medium, start 20dp)
 * - Pill-shaped container (50dp corner radius)
 * - Inner placeholder hint text
 * - Soft shadow in light mode, dark surface in dark mode
 * - Eye toggle for password
 * - Helper/error text below field (10sp, start 12dp)
 */
@Composable
fun AuthInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    helperText: String? = null,
    errorText: String? = null,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = ThemeManager.isDark()
    val textColor = LocalShellColors.current.textPrimary
    val labelColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF666666)
    val inputBg = if (isDark) Color(0xFF1E1E1E) else Color.White

    var passwordVisible by remember { mutableStateOf(false) }

    AuthAnimatedElement(delayIndex = 2) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = labelColor,
                    modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                )
            }

            // Pill container matching Flutter's BorderRadius.circular(50) + boxShadow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .then(
                        if (!isDark) {
                            Modifier.cssShadow(
                                color = Color.Black,
                                alpha = 0.08f,
                                blurRadius = 24.dp,
                                offsetY = 8.dp
                            )
                        } else Modifier
                    )
                    .background(inputBg, RoundedCornerShape(50.dp))
                    .clip(RoundedCornerShape(50.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        // Placeholder hint
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = labelColor
                            )
                        }

                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            ),
                            cursorBrush = SolidColor(if (isDark) Color.White else Color.Black),
                            visualTransformation = if (isPassword && !passwordVisible) {
                                PasswordVisualTransformation()
                            } else {
                                VisualTransformation.None
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (isPassword) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { passwordVisible = !passwordVisible },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    MaterialSymbols.Rounded.Visibility
                                } else {
                                    MaterialSymbols.Rounded.VisibilityOff
                                },
                                contentDescription = if (passwordVisible) "Hide" else "Show",
                                tint = if (isDark) Color.White else Color(0xFF111111),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Error or Helper text matching Flutter (fontSize: 10, start: 12dp)
            if (errorText != null || helperText != null) {
                Text(
                    text = errorText ?: helperText.orEmpty(),
                    fontSize = 10.sp,
                    color = if (errorText != null) MaterialTheme.colorScheme.error else labelColor,
                    modifier = Modifier.padding(start = 12.dp, top = 6.dp)
                )
            }
        }
    }
}

/**
 * Primary Pill Action Button matching Flutter's AuthButton 1:1:
 * - Height: 54dp, full width, pill border radius 50dp
 * - Background: White (dark) / Black (light)
 * - Text color: Black (dark) / White (light)
 * - Disabled alpha: 0.6
 * - Circular spinner when loading
 */
@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit,
    loading: Boolean = false,
    disabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = ThemeManager.isDark()
    val btnBg = LocalShellColors.current.textPrimary
    val btnText = LocalShellColors.current.surface
    val isDisabled = disabled || loading

    AuthAnimatedElement(delayIndex = 3) {
        Box(
            modifier = modifier
                .padding(top = 16.dp, bottom = 8.dp)
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(
                    if (isDisabled) btnBg.copy(alpha = 0.6f) else btnBg,
                    RoundedCornerShape(50.dp)
                )
                .clickable(
                    enabled = !isDisabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = btnText.copy(alpha = 0.5f),
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = btnText
                )
            }
        }
    }
}

/**
 * Circular Back Button matching Flutter's AuthBackButton 1:1:
 * - 48x48 circle, light translucent background
 * - Optically centered Chevron Back icon
 * - Margin bottom: 24dp, left aligned
 */
@Composable
fun AuthBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = ThemeManager.isDark()
    val iconColor = LocalShellColors.current.textPrimary

    Box(
        modifier = modifier
            .padding(bottom = 24.dp)
            .size(48.dp)
            .clip(CircleShape)
            .background(
                (LocalShellColors.current.textPrimary).copy(alpha = 0.08f),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = MaterialSymbols.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .offset(x = (-1).dp) // Optical centering for back chevron
        )
    }
}

/**
 * Language selection row matching Flutter's LanguageTile 1:1:
 * - Title (18sp, semi-bold if selected)
 * - Checkmark icon when selected
 * - Horizontal padding 24dp, vertical padding 20dp
 */
@Composable
fun LanguageTile(
    title: String,
    isSelected: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = ThemeManager.isDark()
    val textColor = LocalShellColors.current.textPrimary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )
        if (isSelected) {
            Icon(
                imageVector = MaterialSymbols.Rounded.CheckCircleFill,
                contentDescription = "Selected",
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
