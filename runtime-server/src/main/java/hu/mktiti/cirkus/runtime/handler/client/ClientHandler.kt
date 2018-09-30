package hu.mktiti.cirkus.runtime.handler.client

import hu.mktiti.cirkus.runtime.common.forever
import hu.mktiti.kreator.property.boolProperty
import hu.mktiti.kreator.property.property
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private const val CLIENT_SCRIPT_POST = "-runtime-client/start.sh"
private const val HOST = "localhost"

fun createLogDir(logDirBase: String = property("LOG_DIR_BASE")): Path {
    val basePath = Paths.get(logDirBase)
    if (!Files.exists(basePath)) {
        Files.createDirectory(basePath)
    }

    if (!Files.isWritable(basePath)) {
        throw RuntimeException("Cannot create log dir, cannot access base dir '$logDirBase'")
    }

    var counter = 0
    forever {
        val subPath = basePath.resolve("log-${counter++}")
        if (!Files.exists(subPath)) {
            Files.createDirectory(subPath)
            return subPath
        }
    }
}

fun startBot(port: Int, logPath: Path) = startClient("bot", port, logPath, boolProperty("LOG_BOT_OUT", true))

fun startEngine(port: Int, logPath: Path) = startClient("engine", port, logPath, boolProperty("LOG_ENGINE_OUT", true))

private fun startClient(clientName: String, port: Int, logPath: Path, log: Boolean = true): Process {
    val path = File(clientName + CLIENT_SCRIPT_POST).absolutePath
    return with(ProcessBuilder(path, HOST, port.toString(), logPath.toAbsolutePath().toString())) {
        if (log) {
            redirectOutput(ProcessBuilder.Redirect.INHERIT)
            redirectError(ProcessBuilder.Redirect.INHERIT)
        }
        start()
    }
}
