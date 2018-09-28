package hu.mktiti.cirkus.runtime.handler.client

import hu.mktiti.kreator.property.boolProperty
import java.io.File

private const val CLIENT_SCRIPT_POST = "-runtime-client/start.sh"
private const val HOST = "localhost"

fun startBot(port: Int) = startClient("bot", port, boolProperty("LOG_BOT_OUT", true))

fun startEngine(port: Int) = startClient("engine", port, boolProperty("LOG_ENGINE_OUT", true))

private fun startClient(clientName: String, port: Int, log: Boolean = true): Process {
    val path = File(clientName + CLIENT_SCRIPT_POST).absolutePath
    return with(ProcessBuilder(path, HOST, port.toString())) {
        if (log) {
            redirectOutput(ProcessBuilder.Redirect.INHERIT)
            redirectError(ProcessBuilder.Redirect.INHERIT)
        }
        start()
    }
}
