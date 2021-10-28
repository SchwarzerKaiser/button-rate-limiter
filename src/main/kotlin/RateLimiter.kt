import RateLimiter.State.*
import java.util.*

/**
 * A simple, generic construct to guarantee consistency of ON/OFF button states.
 *
 * @param timeout Minimum length of time in ms a state should remain the
 * same before transitioning. Set to 100ms by default.
 * @param resolution Regularity in ms of algorithm step being executed
 */
abstract class RateLimiter(
    private val timeout: Long = 500L,
    private val resolution: Long = 100L,
    var DEBUG: Boolean = false,
) {

    enum class State { DOWN, RELEASE }

    private var currentState: State = RELEASE
    private var timeLastEvent = System.currentTimeMillis() - timeout

    private val eventQueue = Collections.synchronizedList(ArrayList<State>())

    init {
        Thread {
            while (true) {
                step()
                Thread.sleep(resolution)
            }
        }.start()
    }

    private fun step() {
        if (DEBUG) println(
            "time: ${System.currentTimeMillis()}, currentState: $currentState, queue: $eventQueue")

        synchronized(eventQueue) {

            if (eventQueue.isNotEmpty()) {

                // Check timer status
                if (System.currentTimeMillis() - timeLastEvent < timeout)
                    return

                // If many items added, last of which is same as current, then clear and ignore
                if (eventQueue.size > 1 && eventQueue.last() == currentState) {
                    eventQueue.clear()
                    return
                }

                // Remove first item in the event queue and process
                when (val state = eventQueue.removeFirst()) {
                    DOWN -> {
                        if (currentState != DOWN) {
                            down()
                            currentState = state
                        }
                    }

                    RELEASE -> {
                        if (currentState != RELEASE) {
                            release()
                            currentState = state
                        }
                    }
                }
                timeLastEvent = System.currentTimeMillis()
            }
        }
    }

    /**
     * Submit a state transition. Ignored if the given state is the same as
     * the current state or last event in the event queue.
     *
     * @see RateLimiter.State
     */
    fun submitEvent(state: State) {
        synchronized(eventQueue) {
            if (eventQueue.isNotEmpty()) {
                if (eventQueue.last() == state) return
            } else {
                if (currentState == state) return
            }
            eventQueue.add(state)
        }
    }

    /**
     * Executes the implementation corresponding to an ON state, or DOWN.
     */
    abstract fun down()

    /**
     * Executes the implementation corresponding to an OFF state, or RELEASE.
     */
    abstract fun release()
}