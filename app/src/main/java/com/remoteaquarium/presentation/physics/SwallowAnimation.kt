package com.remoteaquarium.presentation.physics

/**
 * Animates prey fish being swallowed by the predator.
 *
 * Behavior rules:
 *  1. Prey just caught  → start shrink timer (0.3s)
 *  2. Shrinking          → scale decreases toward 0 over the duration
 *  3. Swallow complete   → hide prey at (-100, -100), mark dead
 */
object SwallowAnimation {

    private const val DURATION = 0.3f
    private const val HIDDEN = -100f

    fun startSwallow(timers: FloatArray, preyIndex: Int) {
        timers[preyIndex] = DURATION
    }

    fun isBeingSwallowed(timers: FloatArray, index: Int): Boolean = timers[index] >= 0f

    fun update(
        timers: FloatArray,
        fishObjects: List<PhysicsObject>,
        fishScale: FloatArray,
        stateMachine: PredatorCycleStateMachine,
        dt: Float,
    ) {
        for (i in timers.indices) {
            if (timers[i] < 0f) continue

            timers[i] -= dt
            val progress = 1f - (timers[i] / DURATION).coerceIn(0f, 1f)
            fishScale[i] = (1f - progress).coerceAtLeast(0f) * fishScale[i]

            if (timers[i] <= 0f) {
                timers[i] = -1f
                fishObjects[i].x = HIDDEN
                fishObjects[i].y = HIDDEN
                stateMachine.markDead(i)
            }
        }
    }
}
