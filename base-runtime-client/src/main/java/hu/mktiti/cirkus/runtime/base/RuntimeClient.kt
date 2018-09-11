package hu.mktiti.cirkus.runtime.base

import hu.mktiti.kreator.InjectableType
import hu.mktiti.kreator.inject
import java.util.*

@InjectableType
interface RuntimeClient {

    fun runClient(arguments: Map<String, String>)

}

fun main(args: Array<String>) {
    if (args.size % 2 != 0) {
        println("Run with arguments: [{property_key} {property_value}]")
    } else {
        val arguments = args.toList().chunked(2).map { l -> l[0] to l[1] }.toMap()
        inject<RuntimeClient>().runClient(arguments)
    }
}