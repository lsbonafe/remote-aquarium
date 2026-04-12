package com.remoteaquarium.presentation.physics

class PhysicsWorld(
    val width: Float,
    val height: Float,
    val margin: Float = 40f,
) {
    fun clampAndBounce(obj: PhysicsObject) {
        if (obj.x < margin) { obj.x = margin; obj.vx = -obj.vx * obj.restitution }
        if (obj.x > width - margin) { obj.x = width - margin; obj.vx = -obj.vx * obj.restitution }
        if (obj.y < margin) { obj.y = margin; obj.vy = -obj.vy * obj.restitution }
        if (obj.y > height - margin) { obj.y = height - margin; obj.vy = -obj.vy * obj.restitution }
    }
}
