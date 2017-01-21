package com.mordrum.apiserver.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import com.mordrum.apiserver.util.JsonUtil
import com.mordrum.apiserver.util.MinecraftChatColor
import io.ebean.Finder
import java.util.*
import javax.persistence.*

@Entity
class Civilization : SuperModel() {
    companion object CivilizationFinder : Finder<Long, Civilization>(Civilization::class.java);

    @Column(unique = true)
    var name: String = ""
    @Column(unique = true)
    var banner: String = ""
    @OneToMany(mappedBy = "civilization")
    var players: Set<Player> = HashSet()
    @JsonProperty("invite_only")
    var inviteOnly: Boolean = false

    @OneToOne(optional = false)
    var founder: Player? = null

    @OneToMany(mappedBy = "civilization")
    var chunks: Set<CivilizationChunk> = HashSet()

    var primaryColor: MinecraftChatColor = MinecraftChatColor.YELLOW
    var secondaryColor: MinecraftChatColor = MinecraftChatColor.BLUE
    var tertiaryColor: MinecraftChatColor = MinecraftChatColor.GREEN
    // Short version of the civ name, shown in chat and other places
    var tag = ""
    // Shown when a non-citizen enters this civ's land
    var welcomeMessage = ""
    // Shown to all citizens upon login
    var motd = ""
    // Shown to anyone who looks at the civilization in a menu of some sort
    var description = ""
    // Allied civilizations
    @ManyToMany
    @JoinTable(name = "civilization_allies", joinColumns = arrayOf(JoinColumn(name = "left_ally", referencedColumnName = "id")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "right_ally", referencedColumnName = "id")))
    var allies: Set<Civilization> = HashSet()
    // Civilizations this civ is at war with
    @ManyToMany
    @JoinTable(name = "civilization_enemies", joinColumns = arrayOf(JoinColumn(name = "left_enemy", referencedColumnName = "id")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "right_enemy", referencedColumnName = "id")))
    var enemies: Set<Civilization> = HashSet()
    // What this civ focuses on primarily
    var primaryFocus: Focus? = null
    var secondaryFocuses: Collection<Focus> = HashSet()

    override fun toJson(with: List<String>): ObjectNode {
        val objectNode = super.toJson(with)
        objectNode.put("name", this.name)
        objectNode.put("banner", this.banner)
        objectNode.put("invite_only", this.inviteOnly)

        objectNode.put("primary_color", this.primaryColor.name)
        objectNode.put("secondary_color", this.secondaryColor.name)
        objectNode.put("tertiary_color", this.tertiaryColor.name)

        val playersArray = JsonUtil.newArray()
        if (with.contains("players")) players.forEach { playersArray.add(it.toJson(with - "players")) }
        else players.forEach {
            playersArray.add(it.id)
        }
        objectNode.set("players", playersArray)

        val chunksArray = JsonUtil.newArray()
        if (with.contains("chunks")) chunks.forEach { chunksArray.add(it.toJson(with - "chunks")) }
        else chunks.forEach {

            chunksArray.add(it.id)
        }
        objectNode.set("chunks", chunksArray)

        val alliesArray = JsonUtil.newArray()
        if (with.contains("allies")) allies.forEach { alliesArray.add(it.toJson(with - "allies")) }
        else allies.forEach { alliesArray.add(it.id) }
        objectNode.set("allies", alliesArray)

        return objectNode
    }

    enum class Focus {
        MILITARY, TRADING, PRODUCTION, SCIENCE, EXPANSION
    }
}