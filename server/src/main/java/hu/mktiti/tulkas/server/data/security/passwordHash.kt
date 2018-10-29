package hu.mktiti.tulkas.server.data.security

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.property.intProperty
import hu.mktiti.kreator.property.property
import hu.mktiti.tulkas.server.data.hexBytes
import hu.mktiti.tulkas.server.data.hexString
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@InjectableType
interface PasswordHasher {

    fun validate(password: String, combined: String): Boolean

    fun hash(password: String): String

}

@Injectable(arity = InjectableArity.SINGLETON, default = true)
class DefaultHasher(
        algorithm: String = property("Server.Security.Hash.Algorithm", "PBKDF2WithHmacSHA1"),
        private val defaultIteration: Int = intProperty("Server.Security.Hash.Iteration", 4096)
) : PasswordHasher {

    private data class CombinedData(
            val iteration: Int,
            val salt: ByteArray,
            val hash: ByteArray
    ) {
        override fun equals(other: Any?): Boolean =
            other is CombinedData &&
            other.iteration == iteration &&
            other.salt contentEquals salt &&
            other.hash contentEquals hash

        override fun hashCode(): Int = Objects.hash(iteration, salt, hash)

        override fun toString() = "$iteration-${salt.hexString()}-${hash.hexString()}"
    }

    private val saltLength = 128
    private val keyLength = 8 * 64 // 64 byte key length

    private val random = SecureRandom()
    private val keyFactory = SecretKeyFactory.getInstance(algorithm)

    private fun combinedFromString(string: String): CombinedData? {
        val parts = string.split("-")
        if (parts.size != 3) return null

        return CombinedData(parts[0].toIntOrNull() ?: return null, parts[1].hexBytes(), parts[2].hexBytes())
    }

    private fun hash(password: String, salt: ByteArray, iteration: Int): CombinedData {
        val spec = PBEKeySpec(password.toCharArray(), salt, iteration, keyLength)
        val hash = keyFactory.generateSecret(spec).encoded
        return CombinedData(iteration, salt, hash)
    }

    private fun generateSalt(): ByteArray = ByteArray(saltLength).apply { random.nextBytes(this) }

    override fun hash(password: String): String = hash(password, generateSalt(), defaultIteration).toString()

    override fun validate(password: String, combined: String): Boolean {
        val (iteration, salt, hash) = combinedFromString(combined) ?: return false
        return hash(password, salt, iteration).hash contentEquals hash
    }

}