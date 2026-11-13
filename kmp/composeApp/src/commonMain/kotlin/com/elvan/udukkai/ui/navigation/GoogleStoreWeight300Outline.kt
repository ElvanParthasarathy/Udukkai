// Google Material Symbols Rounded: opsz=24, wght=300, FILL=0, GRAD=0.
// Source: https://fonts.gstatic.com/render/v1/Material+Symbols+Rounded/24dp/store.kt?var=opsz,wght,FILL,GRAD@24,300,0,0
package com.elvan.udukkai.ui.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val GoogleStoreWeight300Outline: ImageVector
  get() {
    if (_GoogleStoreWeight300Outline != null) {
      return _GoogleStoreWeight300Outline!!
    }
    _GoogleStoreWeight300Outline =
      ImageVector.Builder(
          name = "store",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(4.96f, 4.25f)
            horizontalLineTo(19.04f)
            quadToRelative(0.32f, 0f, 0.53f, 0.22f)
            reflectiveQuadTo(19.79f, 5f)
            reflectiveQuadTo(19.57f, 5.53f)
            reflectiveQuadTo(19.04f, 5.75f)
            horizontalLineTo(4.96f)
            quadTo(4.64f, 5.75f, 4.43f, 5.53f)
            reflectiveQuadTo(4.21f, 5f)
            reflectiveQuadTo(4.43f, 4.47f)
            reflectiveQuadTo(4.96f, 4.25f)
            close()
            moveToRelative(0.19f, 15.5f)
            quadToRelative(-0.38f, 0f, -0.64f, -0.26f)
            reflectiveQuadTo(4.25f, 18.85f)
            verticalLineToRelative(-5.1f)
            horizontalLineTo(3.94f)
            quadToRelative(-0.43f, 0f, -0.7f, -0.33f)
            reflectiveQuadTo(3.06f, 12.66f)
            lineToRelative(1f, -4.69f)
            quadTo(4.13f, 7.65f, 4.38f, 7.45f)
            reflectiveQuadTo(4.95f, 7.25f)
            horizontalLineToRelative(14.1f)
            quadToRelative(0.32f, 0f, 0.57f, 0.2f)
            reflectiveQuadToRelative(0.32f, 0.51f)
            lineToRelative(1f, 4.69f)
            quadToRelative(0.1f, 0.43f, -0.18f, 0.76f)
            reflectiveQuadToRelative(-0.7f, 0.33f)
            horizontalLineTo(19.75f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.32f, -0.22f, 0.53f)
            reflectiveQuadTo(19f, 19.75f)
            reflectiveQuadTo(18.47f, 19.53f)
            reflectiveQuadTo(18.25f, 19f)
            verticalLineTo(13.75f)
            horizontalLineToRelative(-4.5f)
            verticalLineToRelative(5.1f)
            quadToRelative(0f, 0.38f, -0.26f, 0.64f)
            reflectiveQuadToRelative(-0.64f, 0.26f)
            horizontalLineTo(5.15f)
            close()
            moveToRelative(0.6f, -1.5f)
            horizontalLineToRelative(6.5f)
            verticalLineToRelative(-4.5f)
            horizontalLineTo(5.75f)
            verticalLineToRelative(4.5f)
            close()
            moveToRelative(-1.08f, -6f)
            horizontalLineTo(19.33f)
            horizontalLineTo(4.67f)
            close()
            moveToRelative(0f, 0f)
            horizontalLineTo(19.33f)
            lineTo(18.57f, 8.75f)
            horizontalLineTo(5.43f)
            lineToRelative(-0.76f, 3.5f)
            close()
          }
        }
        .build()
    return _GoogleStoreWeight300Outline!!
  }

private var _GoogleStoreWeight300Outline: ImageVector? = null
