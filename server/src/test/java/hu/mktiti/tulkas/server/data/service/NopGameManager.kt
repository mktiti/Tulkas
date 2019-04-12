package hu.mktiti.tulkas.server.data.service

import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.TestInjectable

@TestInjectable(arity = InjectableArity.SINGLETON, tags = ["nop"])
class NopGameManager : GameManager {

    override fun rankAllUnranked() {}

    override fun onNewBot(botId: Long) {}

}