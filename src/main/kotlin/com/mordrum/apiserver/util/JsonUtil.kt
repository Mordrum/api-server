package com.mordrum.apiserver.util

import com.fasterxml.jackson.core.JsonGenerator.Feature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.IOException

/**
 * Helper functions to handle JsonNode values.
 */
object JsonUtil {
    private val defaultObjectMapper = newDefaultMapper()
    @Volatile private var objectMapper: ObjectMapper? = null

    fun newDefaultMapper(): ObjectMapper {
        val mapper = ObjectMapper()
//        mapper.registerModule(Jdk8Module())
//        mapper.registerModule(JavaTimeModule())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }

    /**
     * Get the ObjectMapper used to serialize and deserialize objects to and from JSON values.

     * This can be set to a custom implementation using JsonUtil.setObjectMapper.

     * @return the ObjectMapper currently being used
     */
    fun mapper(): ObjectMapper {
        if (objectMapper == null) {
            return defaultObjectMapper
        } else {
            return objectMapper as ObjectMapper
        }
    }

    private fun generateJson(o: Any, prettyPrint: Boolean, escapeNonASCII: Boolean): String {
        try {
            var writer = mapper().writer()
            if (prettyPrint) {
                writer = writer.with(SerializationFeature.INDENT_OUTPUT)
            }
            if (escapeNonASCII) {
                writer = writer.with(Feature.ESCAPE_NON_ASCII)
            }
            return writer.writeValueAsString(o)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    /**
     * Convert an object to JsonNode.

     * @param data Value to convert in JsonUtil.
     */
    fun toJson(data: Any): JsonNode {
        try {
            return mapper().valueToTree<JsonNode>(data)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    /**
     * Convert a JsonNode to a Java value

     * @param json JsonUtil value to convert.
     * *
     * @param clazz Expected Java value type.
     */
    fun <A> fromJson(json: JsonNode, clazz: Class<A>): A {
        try {
            return mapper().treeToValue(json, clazz)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    /**
     * Creates a new empty ObjectNode.
     */
    fun newObject(): ObjectNode {
        return mapper().createObjectNode()
    }

    /**
     * Creates a new empty ArrayNode.
     */
    fun newArray(): ArrayNode {
        return mapper().createArrayNode()
    }

    /**
     * Convert a JsonNode to its string representation.
     */
    fun stringify(json: JsonNode): String {
        return generateJson(json, false, false)
    }

    /**
     * Convert a JsonNode to its string representation, escaping non-ascii characters.
     */
    fun asciiStringify(json: JsonNode): String {
        return generateJson(json, false, true)
    }

    /**
     * Convert a JsonNode to its string representation.
     */
    fun prettyPrint(json: JsonNode): String {
        return generateJson(json, true, false)
    }

    /**
     * Parse a String representing a json, and return it as a JsonNode.
     */
    fun parse(src: String): JsonNode {
        try {
            return mapper().readTree(src)
        } catch (t: Throwable) {
            throw RuntimeException(t)
        }

    }

    /**
     * Parse a InputStream representing a json, and return it as a JsonNode.
     */
    fun parse(src: java.io.InputStream): JsonNode {
        try {
            return mapper().readTree(src)
        } catch (t: Throwable) {
            throw RuntimeException(t)
        }

    }

    /**
     * Parse a byte array representing a json, and return it as a JsonNode.
     */
    fun parse(src: ByteArray): JsonNode {
        try {
            return mapper().readTree(src)
        } catch (t: Throwable) {
            throw RuntimeException(t)
        }

    }

    /**
     * Inject the object mapper to use.

     * This is intended to be used when Play starts up.  By default, Play will inject its own object mapper here,
     * but this mapper can be overridden either by a custom module.
     */
    fun setObjectMapper(mapper: ObjectMapper) {
        objectMapper = mapper
    }

}