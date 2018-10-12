package hu.mktiti.tulkas.runtime.common

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import hu.mktiti.kreator.property.boolProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLogger
import kotlin.reflect.full.companionObject

object UnifiedLogFactory {

    private val logCreator: (String) -> Logger = if (boolProperty("DISABLE_LOG", false)) {
        { NOPLogger.NOP_LOGGER }
    } else {
        { name -> getContext().getLogger(name) }
    }

    private val appenders: MutableCollection<Appender<ILoggingEvent>> = ArrayList(1)

    fun addAppender(appender: Appender<ILoggingEvent>) {
        appenders += appender
    }

    fun getLogger(name: String): Logger {
        return logCreator(name).apply {
            appenders.forEach { addAppender(it) }
        }
    }

    fun getContext() = LoggerFactory.getILoggerFactory() as LoggerContext

}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { UnifiedLogFactory.getLogger(unwrapCompanionClass(this.javaClass).simpleName) }
}

fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}