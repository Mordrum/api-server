package com.mordrum.apiserver.models

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import io.ebean.Finder
import java.util.*
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class Player : SuperModel() {
    companion object PlayerFinder: Finder<Long, Player>(Player::class.java) {
        fun  getOrCreateByUUID(uuid: UUID): Player {
            var player = Player.query().where().eq("uuid", uuid).findUnique()
            if (player == null) {
                player = Player()
                player.uuid = uuid
                player.save()
            }
            return player
        }
    }

    var uuid: UUID? = null
    @ManyToOne
    var civilization: Civilization? = null

    override fun toJson(with: List<String>): ObjectNode {
        val objectNode = super.toJson(with)
        objectNode.put("uuid", uuid?.toString())

        if (with.contains("civilization")) objectNode.set("civilization", this.civilization?.toJson(with - "civilization"))
        else objectNode.put("civilization", this.civilization?.id)

        return objectNode
    }
}
