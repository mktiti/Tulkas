package hu.mktiti.cirkus.runtime.handler.client

import java.io.File

private const val CLIENT_SCRIPT_POST = "-runtime-client/start.sh"
private const val HOST = "localhost"

fun startBot(port: Int) = startClient("bot", port)

fun startEngine(port: Int) = startClient("engine", port)

private fun startClient(clientName: String, port: Int): Process {
    val path = File(clientName + CLIENT_SCRIPT_POST).absolutePath
    return with(ProcessBuilder(path, HOST, port.toString())) {
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
        redirectError(ProcessBuilder.Redirect.INHERIT)
        start()
    }
}
