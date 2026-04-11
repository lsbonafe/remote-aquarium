package com.remoteaquarium.data.document

import androidx.compose.remote.creation.RFloat
import androidx.compose.remote.creation.RemoteComposeContext

// === Solid shapes (Float coords — static elements) ===

fun RemoteComposeContext.rect(
    left: Float, top: Float, right: Float, bottom: Float,
    color: Int,
) {
    writer.rcPaint.setColor(color).commit()
    drawRect(left, top, right, bottom)
}

fun RemoteComposeContext.oval(
    left: Float, top: Float, right: Float, bottom: Float,
    color: Int,
) {
    writer.rcPaint.setColor(color).commit()
    drawOval(left, top, right, bottom)
}

fun RemoteComposeContext.circle(
    cx: Float, cy: Float, radius: Float,
    color: Int,
) {
    writer.rcPaint.setColor(color).commit()
    drawCircle(cx, cy, radius)
}

// === Solid shapes (RFloat coords — dynamic/physics elements) ===

fun RemoteComposeContext.oval(
    left: RFloat, top: RFloat, right: RFloat, bottom: RFloat,
    color: Int,
) {
    writer.rcPaint.setColor(color).commit()
    drawOval(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}

fun RemoteComposeContext.circle(
    cx: RFloat, cy: RFloat, radius: Float,
    color: Int,
) {
    writer.rcPaint.setColor(color).commit()
    drawCircle(cx.toFloat(), cy.toFloat(), radius)
}

// === Stroked shapes ===

fun RemoteComposeContext.line(
    x1: Float, y1: Float, x2: Float, y2: Float,
    color: Int, strokeWidth: Float = 1f,
) {
    writer.rcPaint.setColor(color).setStrokeWidth(strokeWidth).commit()
    drawLine(x1, y1, x2, y2)
}

fun RemoteComposeContext.line(
    x1: Float, y1: RFloat, x2: Float, y2: RFloat,
    color: Int, strokeWidth: Float = 1f,
) {
    writer.rcPaint.setColor(color).setStrokeWidth(strokeWidth).commit()
    drawLine(x1, y1.toFloat(), x2, y2.toFloat())
}

fun RemoteComposeContext.line(
    x1: Float, y1: Float, x2: RFloat, y2: Float,
    color: Int, strokeWidth: Float = 1f,
) {
    writer.rcPaint.setColor(color).setStrokeWidth(strokeWidth).commit()
    drawLine(x1, y1, x2.toFloat(), y2)
}

// === Gradient shapes ===

fun RemoteComposeContext.gradientRect(
    left: Float, top: Float, right: Float, bottom: Float,
    colors: IntArray, stops: FloatArray,
) {
    writer.rcPaint.setLinearGradient(0f, 0f, 0f, 1f, colors, stops, 0).commit()
    drawRect(left, top, right, bottom)
}

// === Composite shapes ===

fun RemoteComposeContext.fish(
    cx: RFloat,
    cy: RFloat,
    bodyWidth: Float,
    bodyHeight: Float,
    bodyColor: Int,
    finColor: Int,
    eyeColor: Int = NeonPalette.WHITE,
    pupilColor: Int = NeonPalette.BLACK,
) {
    oval(cx - bodyWidth, cy - bodyHeight, cx + bodyWidth, cy + bodyHeight, color = bodyColor)
    oval(cx - bodyWidth * 1.4f, cy - bodyHeight * 0.8f, cx - bodyWidth * 0.7f, cy + bodyHeight * 0.8f, color = finColor)
    circle(cx + bodyWidth * 0.5f, cy - bodyHeight * 0.3f, bodyWidth * 0.12f, color = eyeColor)
    circle(cx + bodyWidth * 0.55f, cy - bodyHeight * 0.3f, bodyWidth * 0.06f, color = pupilColor)
}
