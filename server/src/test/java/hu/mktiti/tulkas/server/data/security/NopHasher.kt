package hu.mktiti.tulkas.server.data.security

import hu.mktiti.kreator.annotation.TestInjectable

@TestInjectable(environment = "unit", tags = ["mock"])
internal class NopHasher : PasswordHasher {

    override fun hash(password: String) = password

    override fun validate(password: String, combined: String) = password == combined

}