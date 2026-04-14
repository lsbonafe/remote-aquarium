package com.remoteaquarium.presentation.physics

enum class CycleState { NORMAL, PREDATOR, CROWN, RESET }

/**
 * Manages the predator cycle state machine.
 *
 * Behavior rules:
 *  1. NORMAL    → check if biggest fish >= 4x 2nd biggest → enter PREDATOR
 *  2. PREDATOR  → track predator index + alive set. 1 fish left → enter CROWN
 *  3. CROWN     → 15s countdown, then → enter RESET
 *  4. RESET     → when animations done (completeReset called) → enter NORMAL
 */
class PredatorCycleStateMachine(private val fishCount: Int) {

    var currentState: CycleState = CycleState.NORMAL
        private set

    var predatorIndex: Int = -1
        private set

    var crownStartTime: Float = 0f
        private set

    private val alive = BooleanArray(fishCount) { true }

    companion object {
        private const val PREDATOR_SCALE_THRESHOLD = 1.5f
        private const val CROWN_DURATION_SEC = 15f
    }

    fun update(fishScale: FloatArray, elapsedTimeSec: Float) {
        when (currentState) {
            CycleState.NORMAL   -> checkPredatorActivation(fishScale)
            CycleState.PREDATOR -> checkCrownTransition(elapsedTimeSec)
            CycleState.CROWN    -> checkResetTransition(elapsedTimeSec)
            CycleState.RESET    -> { /* wait for completeReset() */ }
        }
    }

    fun isAlive(index: Int): Boolean = alive[index]

    fun isPredator(index: Int): Boolean = index == predatorIndex && currentState == CycleState.PREDATOR

    fun markDead(index: Int) {
        alive[index] = false
    }

    fun markAlive(index: Int) {
        alive[index] = true
    }

    fun aliveCount(): Int = alive.count { it }

    fun preyIndices(): List<Int> = alive.indices.filter { alive[it] && it != predatorIndex }

    fun completeReset() {
        currentState = CycleState.NORMAL
        predatorIndex = -1
        for (i in alive.indices) alive[i] = true
    }

    private fun checkPredatorActivation(fishScale: FloatArray) {
        val aliveScales = fishScale.indices
            .filter { alive[it] }
            .map { it to fishScale[it] }
            .sortedByDescending { it.second }

        if (aliveScales.size < 2) return

        val (biggestIdx, biggestScale) = aliveScales[0]
        val (_, secondScale) = aliveScales[1]

        if (secondScale > 0f && biggestScale >= PREDATOR_SCALE_THRESHOLD * secondScale) {
            currentState = CycleState.PREDATOR
            predatorIndex = biggestIdx
        }
    }

    private fun checkCrownTransition(elapsedTimeSec: Float) {
        if (aliveCount() <= 1) {
            currentState = CycleState.CROWN
            crownStartTime = elapsedTimeSec
        }
    }

    private fun checkResetTransition(elapsedTimeSec: Float) {
        if (elapsedTimeSec - crownStartTime >= CROWN_DURATION_SEC) {
            currentState = CycleState.RESET
        }
    }
}
