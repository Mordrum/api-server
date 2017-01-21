package com.mordrum.apiserver.models

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import io.ebean.Finder
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class FishingRecord() : SuperModel() {
    companion object FishingRecordFinder : Finder<Long, FishingRecord>(FishingRecord::class.java)

    @ManyToOne
    var player: Player? = null
    var fish: Int? = 0
    var weight: Double? = 0.0

    constructor(player: Player, fish: Int, weight: Double) : this() {
        this.player = player
        this.fish = fish
        this.weight = weight
    }

    override fun toJson(with: List<String>): ObjectNode {
        val objectNode = super.toJson(with)
        objectNode.put("fish", this.fish)
        objectNode.put("weight", this.weight)

        if (with.contains("player")) objectNode.set("player", this.player?.toJson())
        else objectNode.put("player", this.player?.id)

        return objectNode
    }
}
