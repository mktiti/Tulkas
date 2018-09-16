package hu.mktiti.cirkus.runtime.handler.log

import hu.mktiti.kreator.inject

class LogHandler(
        private val logQueue: LogQueue,
        private val converter: LogConverter = inject()
) {

    fun printLogs() {
        println("Logs:")
        logQueue.getAll().map(converter::convert).forEach(::println)
        println("End of logs")
    }

}