import RateLimiter.State.*
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel

/**
 * Example implementation of rate limiter using Java Swing, and log output for reference.
 */
fun main() {

    // Implement rate limiter
    val rateLimiter = object : RateLimiter(
        timeout = 500,
        resolution = 200,
        DEBUG = false,
    ) {
        override fun down() {
            log("DOWN")
        }

        override fun release() {
            log("RELEASE")
        }
    }

    val frame = JFrame()
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.contentPane.add(JLabel("Window"), BorderLayout.CENTER)
    val button = JButton("PTT")
    button.model.addChangeListener {
        val model = button.model
        if (model.isArmed) {
            rateLimiter.submitEvent(DOWN)
        } else rateLimiter.submitEvent(RELEASE)
    }
    frame.contentPane.add(button)
    button.isVisible = true
    frame.setSize(200, 200)
    frame.isVisible = true
}

var start = System.currentTimeMillis()
var counter = 0
fun log(msg: String) = println("${System.currentTimeMillis() - start} | $msg | counter: ${counter++}")