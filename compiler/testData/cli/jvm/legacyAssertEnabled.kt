// TARGET_BACKEND: JVM
// KOTLIN_CONFIGURATION_FLAGS: ASSERTIONS_MODE=legacy
// WITH_RUNTIME
// FULL_JDK

fun setDesiredAssertionStatus(): Container {
    val loader = Dummy::class.java.classLoader

    loader.setClassAssertionStatus("kotlin._Assertions", true)
    loader.loadClass("kotlin._Assertions")

    return loader.loadClass("Container").newInstance() as Container
}

class Container {
    fun checkTrue(): Boolean {
        var hit = false
        val l = { hit = true; true }
        assert(l())
        return hit
    }

    fun checkTrueWithMessage(): Boolean {
        var hit = false
        val l = { hit = true; true }
        assert(l()) { "BOOYA!" }
        return hit
    }

    fun checkFalse(): Boolean {
        var hit = false
        val l = { hit = true; false }
        assert(l())
        return hit
    }

    fun checkFalseWithMessage(): Boolean {
        var hit = false
        val l = { hit = true; false }
        assert(l()) { "BOOYA!" }
        return hit
    }
}

class Dummy

fun box(): String {
    val c = setDesiredAssertionStatus()

    if (!c.checkTrue()) return "FAIL 1"
    if (!c.checkTrueWithMessage()) return "FAIL 3"

    try {
        c.checkFalse()
        return "FAIL 5"
    } catch (ignore: AssertionError) {
    }

    try {
        c.checkFalseWithMessage()
        return "FAIL 7"
    } catch (ignore: AssertionError) {
    }

    return "OK"
}

fun main() {
    println(box())
}