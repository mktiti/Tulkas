package hu.mktiti.cirkus.runtime.base

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.property.intProperty
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Injectable(arity = InjectableArity.SINGLETON)
class BinaryClassLoader(
        private val maxFileSize: Int = intProperty("BINARY_ENTRY_SIZE_LIMIT", 10_485_760), // 10MB
        private val bufferSize: Int = intProperty("BINARY_ENTRY_BUFFER_SIZE", 4096) // 4 KB
) : ClassLoader() {

    private val classExtension = ".class"

    private val classes = HashMap<String, Class<*>>()

    fun loadFromBinary(binary: ByteArray) {
        val classes = HashMap<String, Class<*>>()

        fun className(entryName: String): String = entryName.removeSuffix(classExtension).replace("/", ".")

        ByteArrayInputStream(binary).use { byteInStream ->
            JarInputStream(byteInStream).use { jarStream ->
                generateSequence(jarStream::getNextJarEntry).forEach { e ->
                    if (e.name.endsWith(classExtension)) {
                        val name = className(e.name)

                        val bytes = safeReadEntry(jarStream, e)
                            ?: throw RuntimeException("Failed to load class '${e.name}'")

                        println("Binary Classloader - Defining class '$name'")

                        classes[name] = defineClass(name, bytes, 0, bytes.size)
                    }
                }
            }
        }

        this.classes.putAll(classes)
    }

    private fun safeReadEntry(stream: ZipInputStream, entry: ZipEntry): ByteArray? {
        if (entry.size > maxFileSize) {
            println("Entry too big (size = ${entry.size} bytes)")
            return null
        } else if (entry.size != -1L) {
            println("Entry size is known: ${entry.size} bytes")
            val size = entry.size.toInt()
            val bytes = ByteArray(size)
            if (stream.read(bytes) != size) {
                println("Entry size is different fom read size")
                return null
            }
            return bytes
        }

        ByteArrayOutputStream().use { bytes ->
            val buffer = ByteArray(bufferSize)

            while (true) {
                val read = stream.read(buffer)
                if (read == -1) {
                    break
                }

                if (bytes.size() + read > maxFileSize) {
                    println("Entry too big (currently at = ${bytes.size()} bytes)")
                    return null
                }

                bytes.write(buffer, 0, read)
            }

            bytes.flush()
            return bytes.toByteArray()
        }
    }

    fun allClasses(): Collection<Class<*>> = classes.values

    override fun findClass(name: String?): Class<*> {
        return name?.let { classes[it] } ?: throw ClassNotFoundException("Class '$name' cannot be loaded")
    }

}