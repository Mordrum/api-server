package com.mordrum.apiserver.controllers

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.mordrum.apiserver.models.Civilization
import com.mordrum.apiserver.models.CivilizationChunk
import com.mordrum.apiserver.models.Player
import com.mordrum.apiserver.models.embeddables.Vector2i
import com.mordrum.apiserver.util.*
import io.ebean.annotation.Transactional
import io.vertx.core.json.JsonObject
import io.vertx.ext.sync.Sync.fiberHandler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.*

class CivilizationController(router: Router) : AbstractController(router) {

    init {
        router.get("/civilizations").handler(fiberHandler({ find(it) }))
        router.post("/civilizations").handler(fiberHandler({ create(it) }))
        router.get("/civilizations/:id/chunks").handler(fiberHandler({ getChunksForCivilization(it) }))
        router.post("/civilizations/:id/chunks").handler(fiberHandler({ claimChunkForCivilization(it) }))
        router.post("/civilizations/:id/addplayer").handler(fiberHandler({ addPlayerToCiv(it) }))
    }

    @Suspendable
    @Transactional
    private fun find(ctx: RoutingContext) {
        val civilizations: List<Civilization>
        if (!ctx.request().params().isEmpty()) {
            val queryString = getQueryMap(ctx)
            val where = Civilization.query().fetch("players").where()
            for ((key, value) in queryString) {
                where.eq(key, value)
            }
            civilizations = where.findList()
        } else {
            civilizations = Civilization.query().fetch("players").findList()
        }

        val arrayNode = JsonNodeFactory.instance.arrayNode()
        for (civilization in civilizations) {
            val objectNode = JsonNodeFactory.instance.objectNode()
            objectNode.put("id", civilization.id)
            objectNode.put("name", civilization.name)
            objectNode.put("banner", civilization.banner)
            objectNode.put("invite_only", civilization.inviteOnly)

            val playersJson = JsonNodeFactory.instance.arrayNode()
            for (player in civilization.players) {
                playersJson.add(player.uuid!!.toString())
            }
            objectNode.set("players", playersJson)
            arrayNode.add(JsonUtil.toJson(civilization))
        }
        success(ctx, arrayNode)
    }

    @Suspendable
    @Transactional
    private fun create(ctx: RoutingContext) {
        val body = bodyAsJson(ctx)

        // Handle validation errors
        if (!body.containsKey("player")) return validationError(ctx, "You must specify the player that is founding the civilization")
        if (!body.containsKey("banner")) return validationError(ctx, "You must specify a banner pattern")
        if (!body.containsKey("name")) return validationError(ctx, "You must specify a civilization name")
        if (!body.containsKey("home_x")) return validationError(ctx, "You must specify the X value of the home chunk")
        if (!body.containsKey("home_z")) return validationError(ctx, "You must specify the Z value of the home chunk")

        val playerId = UUID.fromString(body.getString("player"))
        val bannerId = body.getString("banner")
        val civName = body.getString("name")

        // Duplicate founder
        if (Civilization.query().where().eq("founder.uuid", playerId).findCount() > 0) {
            return validationError(ctx, "That player has already founded a civilization")
        }

        // Duplicate banner
        if (Civilization.query().where().eq("banner", bannerId).findCount() > 0) {
            return validationError(ctx, "A civilization is already using that banner")
        }

        // Duplicate name
        if (Civilization.query().where().eq("name", civName).findCount() > 0) {
            return validationError(ctx, "That name is already in use by another civilization")
        }

        // Check that the surrounding chunks are unoccupied
        val homeX = body.getInteger("home_x")
        val homeZ = body.getInteger("home_z")
        val rowCount = CivilizationChunk.query().where()
                .ge("x", homeX - 2)
                .ge("z", homeZ - 2)
                .le("x", homeX + 2)
                .le("z", homeZ + 2)
                .findCount()
        if (rowCount > 0) {
            return validationError(ctx, "Too close to another civilization")
        }

        val player = Player.getOrCreateByUUID(playerId)

        val civilization = Civilization()
        civilization.banner = bannerId
        civilization.name = civName
        civilization.inviteOnly = true
        civilization.players += player
        civilization.founder = player
        civilization.save()

        player.civilization = civilization
        player.save()

        for (x in homeX - 2..homeX + 2 - 1) {
            for (z in homeZ - 2..homeZ + 2 - 1) {
                val civilizationChunk = CivilizationChunk()
                civilizationChunk.position = Vector2i(x, z)
                civilizationChunk.civilization = civilization
                civilizationChunk.claimingPlayer = player
                civilizationChunk.save()
            }
        }

        return success(ctx, JsonUtil.newObject().put("id", civilization.id))
    }

    @Suspendable
    @Transactional
    fun claimChunkForCivilization(ctx: RoutingContext) {
        val body = bodyAsJson(ctx)
        val civilizationId = ctx.pathParam("id").toLong()

        // Handle validation errors
        if (!body.containsKey("x")) return validationError(ctx, "You must specify an X coordinate")
        if (!body.containsKey("z")) return validationError(ctx, "You must specify a Z coordinate")
        val civilization = Civilization.byId(civilizationId) ?: return simpleError(ctx, 404, "Civilization with ID $civilizationId does not exist")

        val x = body.getInteger("x")
        val z = body.getInteger("z")
        val chunk = CivilizationChunk.query().where()
                .eq("x", x)
                .eq("z", z)
                .findUnique()

        // Ensure that the chunk is not already claimed (PATCH should be used to claim the chunk instead)
        if (chunk != null) {
            return validationError(ctx, "Chunk at $x,$z has already been claimed by ${chunk.civilization!!.name}")
        } else {
            val newChunk = CivilizationChunk()
            newChunk.position = Vector2i(x, z)
            newChunk.civilization = civilization
            //			if (body.has("claiming_player")) newChunk.claimingPlayer = UUID.fromString(body.get("claiming_player").asText());
            newChunk.save()

            return success(ctx, newChunk.toJson(ctx.request().params().getAll("with")))
        }
    }

    @Suspendable
    @Transactional
    fun getChunksForCivilization(ctx: RoutingContext) {
        val civilizationId = ctx.pathParam("id").toLong()

        val civilization = Civilization.byId(civilizationId)
        if (civilization == null) {
            return simpleError(ctx, 404, "")
        } else {
            val arrayNode = JsonUtil.newArray()
            for (chunk in civilization.chunks) {
                val objectNode = JsonUtil.newObject()
                objectNode.put("claimingPlayer", chunk.claimingPlayer!!.toString())
                objectNode.put("x", chunk.position!!.x)
                objectNode.put("z", chunk.position!!.z)
                arrayNode.add(objectNode)
            }
            return success(ctx, arrayNode)
        }
    }

    @Suspendable
    @Transactional
    fun addPlayerToCiv(ctx: RoutingContext) {
        val civilizationId = ctx.pathParam("id").toLong()
        val json = bodyAsJson(ctx)

        val civilization = Civilization.byId(civilizationId)
        if (civilization == null) {
            return simpleError(ctx, 404, "")
        } else {
            if (!json.containsKey("uuid")) return validationError(ctx, "You must provide the UUID of the player to add")

            val player: Player = Player.getOrCreateByUUID(UUID.fromString(json.getString("uuid")))
            civilization.players += player
            civilization.save()
            player.civilization = civilization
            player.save()

            return success(ctx, civilization.toJson())
        }
    }
}