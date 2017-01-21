package com.mordrum.apiserver.models

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import io.ebean.Model
import io.ebean.annotation.CreatedTimestamp
import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import java.sql.Time
import java.sql.Timestamp

import javax.persistence.*
import java.util.Date

@MappedSuperclass
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "id")
abstract class SuperModel : Model() {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Version
    var version: Long? = null

    @WhenCreated
    lateinit var createdAt: Timestamp

    @WhenModified
    lateinit var updatedAt: Timestamp

    @Deprecated(message = "Avoid using", replaceWith = ReplaceWith("toJson(with: List<String>)"))
    fun toJson(): ObjectNode {
        return toJson(emptyList<String>())
    }
    open fun toJson(with: List<String>): ObjectNode {
        val objectNode = JsonNodeFactory.instance.objectNode()
        objectNode.put("id", id)
        objectNode.put("version", version)
        objectNode.put("created_at", createdAt.time)
        objectNode.put("updated_at", updatedAt.time)
        return objectNode
    }
}

