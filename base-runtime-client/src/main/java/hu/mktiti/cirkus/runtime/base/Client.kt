package hu.mktiti.cirkus.runtime.base

import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.kreator.annotation.InjectableType

@InjectableType
interface Client {

    fun runClient(inQueue: InQueue, outQueue: OutQueue)

}