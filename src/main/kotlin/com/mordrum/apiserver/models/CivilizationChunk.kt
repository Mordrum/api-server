package com.mordrum.apiserver.models

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import io.ebean.Finder
import com.mordrum.apiserver.models.embeddables.Vector2i
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class CivilizationChunk : SuperModel() {
    companion object CivilizationChunkFinder: Finder<Long, CivilizationChunk>(CivilizationChunk::class.java);

    @ManyToOne
    var civilization: Civilization? = null
    var claimingPlayer: Player? = null

    @Embedded
    @Column(unique = true)
    var position: Vector2i? = null

    override fun toJson(with: List<String>): ObjectNode {
        val objectNode = super.toJson(with)
        objectNode.put("x", this.position?.x)
        objectNode.put("z", this.position?.z)

        if (with.contains("civilization")) objectNode.set("civilization", this.civilization?.toJson(with))
        else objectNode.put("civilization", this.civilization?.id)

        if (with.contains("claiming_player")) objectNode.set("claiming_player", this.claimingPlayer?.toJson(with))
        else objectNode.put("claiming_player", this.claimingPlayer?.id)

        return objectNode
    }

}
