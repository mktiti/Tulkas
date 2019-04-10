package hu.mktiti.tulkas.server.data.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import javax.inject.Singleton
import javax.ws.rs.ext.ContextResolver
import javax.ws.rs.ext.Provider

@Singleton
@Provider
class JacksonKotlinMapper : ContextResolver<ObjectMapper> {

    private val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    override fun getContext(clazz: Class<*>?): ObjectMapper = mapper

}