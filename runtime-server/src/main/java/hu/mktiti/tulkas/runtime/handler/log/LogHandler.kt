package hu.mktiti.tulkas.runtime.handler.log

import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.runtime.common.logger

class LogHandler(
        private val logQueue: LogQueue,
        private val converter: LogConverter = inject()
) {

    private val log by logger()

    fun printLogs() {
        log.info("Logs:")
        logQueue.getAll().map(converter::convert).forEach(log::info)
        log.info("End of logs")
    }

}