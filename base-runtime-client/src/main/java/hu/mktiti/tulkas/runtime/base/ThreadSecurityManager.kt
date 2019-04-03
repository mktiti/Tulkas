package hu.mktiti.tulkas.runtime.base

class ThreadSecurityManager : SecurityManager() {

    private val rootGroup: ThreadGroup by lazy {
        var root = Thread.currentThread().threadGroup
        while (root.parent != null) {
            root = root.parent
        }
        root
    }

    override fun getThreadGroup() = rootGroup

}