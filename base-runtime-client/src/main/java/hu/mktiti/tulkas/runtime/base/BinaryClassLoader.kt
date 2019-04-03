package hu.mktiti.tulkas.runtime.base

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.property.intProperty
import hu.mktiti.kreator.property.propertyOpt
import hu.mktiti.tulkas.runtime.common.forever
import hu.mktiti.tulkas.runtime.common.logger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FilePermission
import java.security.CodeSigner
import java.security.CodeSource
import java.security.Permissions
import java.security.ProtectionDomain
import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Injectable(arity = InjectableArity.SINGLETON)
class BinaryClassLoader(
        private val maxFileSize: Int = intProperty("BINARY_ENTRY_SIZE_LIMIT", 10_485_760), // 10MB
        private val bufferSize: Int = intProperty("BINARY_ENTRY_BUFFER_SIZE", 4096), // 4 KB
        private val readableFiles: List<String> = propertyOpt("READABLE_FILES")?.split(":") ?: emptyList()
) : ClassLoader() {

    private val log by logger()

    private val classExtension = ".class"

    private val classes = HashMap<String, Class<*>>()

    private val protectionDomain: ProtectionDomain

    init {
        val codeSource = CodeSource(null, emptyArray<CodeSigner>())

        val permissions = Permissions().apply {
            readableFiles.forEach { file ->
                add(FilePermission(file, "read"))
            }
        }

        protectionDomain = ProtectionDomain(codeSource, permissions)
    }

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

                        log.info("Binary Classloader - Defining class '{}'", name)

                        classes[name] = defineClass(name, bytes, 0, bytes.size, protectionDomain)
                    }
                }
            }
        }

        this.classes.putAll(classes)
    }

    private fun safeReadEntry(stream: ZipInputStream, entry: ZipEntry): ByteArray? {
        if (entry.size > maxFileSize) {
            log.info("Entry too big (size = {} bytes)", entry.size)
            return null
        } else if (entry.size != -1L) {
            log.info("Entry size is known: {} bytes", entry.size)
            val size = entry.size.toInt()
            val bytes = ByteArray(size)

            var pos = 0
            forever {
                val readSize = stream.read(bytes, pos, size - pos)

                if (readSize == -1) {
                    log.info("Cannot read more from entry (size: {}, read: {})", size, pos)
                    return null
                }

                pos += readSize
                if (pos == size) {
                    return bytes
                }
            }
        }

        ByteArrayOutputStream().use { bytes ->
            val buffer = ByteArray(bufferSize)

            while (true) {
                val read = stream.read(buffer)
                if (read == -1) {
                    break
                }

                if (bytes.size() + read > maxFileSize) {
                    log.info("Entry too big (currently at = {} bytes)", bytes.size())
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