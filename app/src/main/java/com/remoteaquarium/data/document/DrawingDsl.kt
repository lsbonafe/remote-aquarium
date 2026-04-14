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

fun RemoteComposeContext.rotatedFish(
    cx: RFloat,
    cy: RFloat,
    bodyWidth: Float,
    bodyHeight: Float,
    bodyColor: Int,
    finColor: Int,
    cosA: RFloat,
    sinA: RFloat,
    eyeColor: Int = NeonPalette.WHITE,
    pupilColor: Int = NeonPalette.BLACK,
) {
    val cos2 = (cosA * cosA).flush()
    val sin2 = (sinA * sinA).flush()

    // Body: swap width/height based on angle (pseudo-perspective)
    val bw = (cos2 * bodyWidth + sin2 * bodyHeight).flush()
    val bh = (cos2 * bodyHeight + sin2 * bodyWidth).flush()
    oval(cx - bw, cy - bh, cx + bw, cy + bh, color = bodyColor)

    // Fin: rotate center offset (-bodyWidth*1.05, 0), swap dimensions
    val finOx = -bodyWidth * 1.05f
    val finCx = (cx + cosA * finOx).flush()
    val finCy = (cy + sinA * finOx).flush()
    val fhw = bodyWidth * 0.35f
    val fhh = bodyHeight * 0.8f
    val fw = (cos2 * fhw + sin2 * fhh).flush()
    val fh = (cos2 * fhh + sin2 * fhw).flush()
    oval(finCx - fw, finCy - fh, finCx + fw, finCy + fh, color = finColor)

    // Eye: rotate offset (bodyWidth*0.5, -bodyHeight*0.3)
    val eyeOx = bodyWidth * 0.5f
    val eyeOy = -bodyHeight * 0.3f
    val ecx = (cx + cosA * eyeOx - sinA * eyeOy).flush()
    val ecy = (cy + sinA * eyeOx + cosA * eyeOy).flush()
    circle(ecx, ecy, bodyWidth * 0.12f, color = eyeColor)

    // Pupil: rotate offset (bodyWidth*0.55, -bodyHeight*0.3)
    val pupilOx = bodyWidth * 0.55f
    val pupilOy = -bodyHeight * 0.3f
    val pcx = (cx + cosA * pupilOx - sinA * pupilOy).flush()
    val pcy = (cy + sinA * pupilOx + cosA * pupilOy).flush()
    circle(pcx, pcy, bodyWidth * 0.06f, color = pupilColor)
}
