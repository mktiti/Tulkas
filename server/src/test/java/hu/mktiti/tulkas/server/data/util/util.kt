package hu.mktiti.tulkas.server.data.util

import org.glassfish.jersey.test.JerseyTest

inline fun <reified T> JerseyTest.fetchTest(path: String): T = target(path).request().get(T::class.java)