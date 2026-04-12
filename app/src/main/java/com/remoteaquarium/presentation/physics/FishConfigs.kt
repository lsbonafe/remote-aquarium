package com.remoteaquarium.presentation.physics

object FishConfigs {

    fun create(width: Float, height: Float): List<PhysicsObject> = listOf(
        // Large (heavy, slow swim)
        fish(width * 0.3f, height * 0.25f, drag = 0.96f, gravity = 600f, restitution = 0.25f, radius = 52f, swimSpeed = 0.25f to 0.15f, swimForce = 30f, phase = 0f),
        fish(width * 0.7f, height * 0.40f, drag = 0.95f, gravity = 550f, restitution = 0.22f, radius = 48f, swimSpeed = 0.2f to 0.18f, swimForce = 28f, phase = 1.2f),
        fish(width * 0.5f, height * 0.60f, drag = 0.96f, gravity = 580f, restitution = 0.2f, radius = 45f, swimSpeed = 0.3f to 0.12f, swimForce = 32f, phase = 2.5f),
        // Medium (moderate swim)
        fish(width * 0.2f, height * 0.35f, drag = 0.97f, gravity = 750f, restitution = 0.3f, radius = 34f, swimSpeed = 0.4f to 0.25f, swimForce = 50f, phase = 0.8f),
        fish(width * 0.8f, height * 0.50f, drag = 0.97f, gravity = 800f, restitution = 0.32f, radius = 38f, swimSpeed = 0.35f to 0.3f, swimForce = 45f, phase = 3.1f),
        fish(width * 0.4f, height * 0.70f, drag = 0.96f, gravity = 720f, restitution = 0.28f, radius = 32f, swimSpeed = 0.45f to 0.2f, swimForce = 55f, phase = 1.7f),
        fish(width * 0.6f, height * 0.30f, drag = 0.97f, gravity = 780f, restitution = 0.3f, radius = 36f, swimSpeed = 0.38f to 0.28f, swimForce = 48f, phase = 4.2f),
        fish(width * 0.15f, height * 0.55f, drag = 0.96f, gravity = 740f, restitution = 0.28f, radius = 33f, swimSpeed = 0.42f to 0.22f, swimForce = 52f, phase = 2.0f),
        fish(width * 0.85f, height * 0.65f, drag = 0.97f, gravity = 810f, restitution = 0.32f, radius = 35f, swimSpeed = 0.33f to 0.27f, swimForce = 46f, phase = 5.5f),
        // Small (fast swim, darty)
        fish(width * 0.25f, height * 0.20f, drag = 0.98f, gravity = 1000f, restitution = 0.4f, radius = 22f, swimSpeed = 0.7f to 0.5f, swimForce = 80f, phase = 0.3f),
        fish(width * 0.75f, height * 0.28f, drag = 0.98f, gravity = 1050f, restitution = 0.42f, radius = 18f, swimSpeed = 0.8f to 0.45f, swimForce = 85f, phase = 1.9f),
        fish(width * 0.45f, height * 0.42f, drag = 0.98f, gravity = 980f, restitution = 0.38f, radius = 21f, swimSpeed = 0.65f to 0.55f, swimForce = 75f, phase = 3.7f),
        fish(width * 0.55f, height * 0.75f, drag = 0.98f, gravity = 1020f, restitution = 0.4f, radius = 18f, swimSpeed = 0.75f to 0.48f, swimForce = 82f, phase = 5.0f),
        fish(width * 0.1f, height * 0.68f, drag = 0.98f, gravity = 1060f, restitution = 0.42f, radius = 22f, swimSpeed = 0.85f to 0.4f, swimForce = 90f, phase = 2.3f),
        fish(width * 0.9f, height * 0.38f, drag = 0.98f, gravity = 1100f, restitution = 0.44f, radius = 20f, swimSpeed = 0.6f to 0.6f, swimForce = 78f, phase = 4.8f),
        fish(width * 0.35f, height * 0.48f, drag = 0.98f, gravity = 970f, restitution = 0.38f, radius = 16f, swimSpeed = 0.9f to 0.35f, swimForce = 88f, phase = 0.6f),
        fish(width * 0.65f, height * 0.58f, drag = 0.98f, gravity = 1080f, restitution = 0.43f, radius = 21f, swimSpeed = 0.72f to 0.52f, swimForce = 83f, phase = 3.3f),
        fish(width * 0.5f, height * 0.15f, drag = 0.98f, gravity = 950f, restitution = 0.36f, radius = 18f, swimSpeed = 0.68f to 0.42f, swimForce = 76f, phase = 1.4f),
    )

    fun createBubbles(width: Float, height: Float): List<PhysicsObject> = listOf(
        PhysicsObject(width * 0.20f, height * 0.85f, vy = -60f, drag = 0.99f, gravityScale = 150f, restitution = 0.1f, radius = 5f),
        PhysicsObject(width * 0.40f, height * 0.90f, vy = -50f, drag = 0.99f, gravityScale = 120f, restitution = 0.1f, radius = 4f),
        PhysicsObject(width * 0.60f, height * 0.80f, vy = -70f, drag = 0.99f, gravityScale = 130f, restitution = 0.1f, radius = 6f),
        PhysicsObject(width * 0.80f, height * 0.88f, vy = -55f, drag = 0.99f, gravityScale = 140f, restitution = 0.1f, radius = 4f),
        PhysicsObject(width * 0.30f, height * 0.75f, vy = -65f, drag = 0.99f, gravityScale = 110f, restitution = 0.1f, radius = 5f),
        PhysicsObject(width * 0.70f, height * 0.92f, vy = -45f, drag = 0.99f, gravityScale = 160f, restitution = 0.1f, radius = 3f),
    )

    private fun fish(
        x: Float, y: Float,
        drag: Float, gravity: Float, restitution: Float, radius: Float,
        swimSpeed: Pair<Float, Float>, swimForce: Float, phase: Float,
    ) = PhysicsObject(
        x = x, y = y,
        drag = drag, gravityScale = gravity, restitution = restitution, radius = radius,
        swimSpeedX = swimSpeed.first, swimSpeedY = swimSpeed.second,
        swimForce = swimForce, swimPhase = phase,
    )
}
