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

fun RemoteComposeContext.circle(
    cx: RFloat, cy: RFloat, radius: RFloat,
    color: Int,
) {
    writer.rcPaint.setColor(color).commit()
    drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat())
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
    mouthOpen: RFloat,
    scale: RFloat,
    eyeColor: Int = NeonPalette.WHITE,
    pupilColor: Int = NeonPalette.BLACK,
) {
    val sw = (scale * bodyWidth).flush()
    val sh = (scale * bodyHeight).flush()
    val cos2 = (cosA * cosA).flush()
    val sin2 = (sinA * sinA).flush()

    // Body: swap width/height based on angle (pseudo-perspective)
    val bw = (cos2 * sw + sin2 * sh).flush()
    val bh = (cos2 * sh + sin2 * sw).flush()
    oval(cx - bw, cy - bh, cx + bw, cy + bh, color = bodyColor)

    // Fin: rotate center offset, swap dimensions
    val finOx = (sw * -1.05f).flush()
    val finCx = (cx + cosA * finOx).flush()
    val finCy = (cy + sinA * finOx).flush()
    val fhw = (sw * 0.35f).flush()
    val fhh = (sh * 0.8f).flush()
    val fw = (cos2 * fhw + sin2 * fhh).flush()
    val fh = (cos2 * fhh + sin2 * fhw).flush()
    oval(finCx - fw, finCy - fh, finCx + fw, finCy + fh, color = finColor)

    // Mouth: oval at front of fish, height scales with mouthOpen (0 = invisible, 1 = open)
    val mouthOx = (sw * 0.65f).flush()
    val mcx = (cx + cosA * mouthOx).flush()
    val mcy = (cy + sinA * mouthOx).flush()
    val mouthW = (sw * 0.1f).flush()
    val mouthH = (mouthOpen * sh * 0.35f).flush()
    oval(mcx - mouthW, mcy - mouthH, mcx + mouthW, mcy + mouthH, color = NeonPalette.BLACK)

    // Eye: rotate offset
    val eyeOx = (sw * 0.5f).flush()
    val eyeOy = (sh * -0.3f).flush()
    val ecx = (cx + cosA * eyeOx - sinA * eyeOy).flush()
    val ecy = (cy + sinA * eyeOx + cosA * eyeOy).flush()
    val eyeR = (sw * 0.12f).flush()
    circle(ecx, ecy, eyeR, color = eyeColor)

    // Pupil: rotate offset
    val pupilOx = (sw * 0.55f).flush()
    val pupilOy = (sh * -0.3f).flush()
    val pcx = (cx + cosA * pupilOx - sinA * pupilOy).flush()
    val pcy = (cy + sinA * pupilOx + cosA * pupilOy).flush()
    val pupilR = (sw * 0.06f).flush()
    circle(pcx, pcy, pupilR, color = pupilColor)
}
