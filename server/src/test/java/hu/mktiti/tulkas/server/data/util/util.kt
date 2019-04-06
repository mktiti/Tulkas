package hu.mktiti.tulkas.server.data.util

import org.glassfish.jersey.test.JerseyTest
import java.time.LocalDateTime
import java.time.temporal.TemporalAmount

inline fun <reified T> JerseyTest.fetchTest(path: String): T = target(path).request().get(T::class.java)

fun LocalDateTime.withinRange(date: LocalDateTime, interval: TemporalAmount)
    = minus(interval) <= date && plus(interval) >= date