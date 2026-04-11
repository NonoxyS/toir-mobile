package ru.mirea.toir.feature.auth.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val IconVisibility: ImageVector by lazy {
    ImageVector.Builder(
        name = "Visibility",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).path(
        fill = SolidColor(Color.Black),
        fillAlpha = 1f,
        strokeAlpha = 1f,
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = PathFillType.NonZero,
    ) {
        moveTo(12f, 4.5f)
        curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
        curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
        reflectiveCurveTo(21.27f, 16.39f, 23f, 12f)
        curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
        close()
        moveTo(12f, 17f)
        curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
        reflectiveCurveTo(9.24f, 7f, 12f, 7f)
        reflectiveCurveTo(17f, 9.24f, 17f, 12f)
        reflectiveCurveTo(14.76f, 17f, 12f, 17f)
        close()
        moveTo(12f, 9f)
        curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
        reflectiveCurveTo(10.34f, 15f, 12f, 15f)
        reflectiveCurveTo(15f, 13.66f, 15f, 12f)
        reflectiveCurveTo(13.66f, 9f, 12f, 9f)
        close()
    }.build()
}

internal val IconVisibilityOff: ImageVector by lazy {
    ImageVector.Builder(
        name = "VisibilityOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).path(
        fill = SolidColor(Color.Black),
        fillAlpha = 1f,
        strokeAlpha = 1f,
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = PathFillType.NonZero,
    ) {
        moveTo(12f, 7f)
        curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
        curveTo(17f, 12.65f, 16.87f, 13.26f, 16.64f, 13.83f)
        lineTo(19.56f, 16.75f)
        curveTo(21.07f, 15.49f, 22.26f, 13.86f, 22.99f, 12f)
        curveTo(21.26f, 7.61f, 16.99f, 4.5f, 11.99f, 4.5f)
        curveTo(10.59f, 4.5f, 9.25f, 4.75f, 8.01f, 5.2f)
        lineTo(10.17f, 7.36f)
        curveTo(10.74f, 7.13f, 11.35f, 7f, 12f, 7f)
        close()
        moveTo(2f, 4.27f)
        lineTo(4.28f, 6.55f)
        lineTo(4.74f, 7.01f)
        curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1f, 12f)
        curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
        curveTo(13.55f, 19.5f, 15.03f, 19.2f, 16.38f, 18.66f)
        lineTo(16.8f, 19.08f)
        lineTo(19.73f, 22f)
        lineTo(21f, 20.73f)
        lineTo(3.27f, 3f)
        lineTo(2f, 4.27f)
        close()
        moveTo(7.53f, 9.8f)
        lineTo(9.08f, 11.35f)
        curveTo(9.03f, 11.56f, 9f, 11.78f, 9f, 12f)
        curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
        curveTo(12.22f, 15f, 12.44f, 14.97f, 12.65f, 14.92f)
        lineTo(14.2f, 16.47f)
        curveTo(13.53f, 16.8f, 12.79f, 17f, 12f, 17f)
        curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
        curveTo(7f, 11.21f, 7.2f, 10.47f, 7.53f, 9.8f)
        close()
        moveTo(11.84f, 9.02f)
        lineTo(14.99f, 12.17f)
        lineTo(15.01f, 12.01f)
        curveTo(15.01f, 10.35f, 13.67f, 9.01f, 12.01f, 9.01f)
        lineTo(11.84f, 9.02f)
        close()
    }.build()
}
