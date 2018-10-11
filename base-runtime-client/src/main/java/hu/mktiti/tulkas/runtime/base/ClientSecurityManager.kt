package hu.mktiti.tulkas.runtime.base

import hu.mktiti.kreator.property.intProperty
import hu.mktiti.kreator.property.property
import hu.mktiti.kreator.property.propertyOpt
import java.io.FilePermission
import java.security.Permission

class ClientSecurityManager(
        private val host: String = property("SOCKET_HOST"),
        private val port: Int = intProperty("SOCKET_PORT"),
        private val readableFiles: List<String> = propertyOpt("READABLE_FILES")?.split(":") ?: emptyList(),
        private val userDefinedClasses: List<String>
) : SecurityManager() {

    override fun checkPermission(permission: Permission?) {
        println("Check permission for $permission")

        if (permission is FilePermission) {
            if (permission.actions != "read" || !readableFiles.contains(permission.name)) {
                throw SecurityException("You have tried to access file ${permission.name} with action ${permission.actions}." +
                        " File access is forbidden except for reading some allowed files (e.g., for random generation)")
            } else {
                println("Allowed access $permission")
            }
        }

        // throw SecurityException("Limited environment, action forbidden")
    }

    override fun checkPermission(permission: Permission?, context: Any?) {
        checkPermission(permission)
    }

}