package hu.mktiti.tulkas.runtime.base

import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.runtime.common.InQueue
import hu.mktiti.tulkas.runtime.common.OutQueue

@InjectableType
interface Client {

    fun runClient(
            inQueue: InQueue,
            outQueue: OutQueue,
            messageConverter: MessageConverter,
            binaryClassLoader: BinaryClassLoader,
            clientHelper: RuntimeClientHelper
    )

}