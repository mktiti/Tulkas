package hu.mktiti.tulkas.runtime.common

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.companionObject

object UnifiedLogFactory {

    private val appenders: MutableCollection<Appender<ILoggingEvent>> = ArrayList(1)

    fun addAppender(appender: Appender<ILoggingEvent>) {
        appenders += appender
    }

    fun getLogger(name: String): Logger {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        return context.getLogger(name).apply {
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