package com.mordrum.apiserver.controllers

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.mordrum.apiserver.models.Civilization
import com.mordrum.apiserver.models.CivilizationChunk
import com.mordrum.apiserver.models.Player
import com.mordrum.apiserver.models.embeddables.Vector2i
import com.mordrum.apiserver.util.AbstractController
import com.mordrum.apiserver.util.bodyAsJson
import com.mordrum.apiserver.util.getQueryMap
import io.ebean.annotation.Transactional
import io.vertx.core.json.Json
import io.vertx.ext.sync.Sync
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class ChunksController(router: Router) : AbstractController(router) {
    init {
        router.get("/chunks").handler(Sync.fiberHandler { find(it) })
        router.post("/chunks").handler(Sync.fiberHandler { claimChunk(it) })
    }

    @Suspendable
    @Transactional
    private fun find(ctx: RoutingContext) {
        val chunks: List<CivilizationChunk>
        if (!ctx.request().params().isEmpty) {
            val where = CivilizationChunk.query().where()
            for ((key, value) in getQueryMap(ctx)) {
                where.eq(key, value)
            }
            chunks = where.findList()
        } else {
            chunks = CivilizationChunk.all()
        }

        val arrayNode = JsonNodeFactory.instance.arrayNode()
        for (chunk in chunks) {
            val objectNode = JsonNodeFactory.instance.objectNode()
            objectNode.put("id", chunk.id)
            objectNode.put("x", chunk.position!!.x)
            objectNode.put("z", chunk.position!!.z)
            arrayNode.add(objectNode)
        }

        val response = ctx.response()
        response.putHeader("content-type", "application/json")
        response.end(arrayNode.toString())
    }

    @Suspendable
    @Transactional
    private fun claimChunk(ctx: RoutingContext) {
        val body = bodyAsJson(ctx)
        val response = ctx.response()

        // Validate the request
        if (!body.containsKey("x")) {
            response.statusCode = 422
            response.end(JsonNodeFactory.instance.objectNode().put("message", "You must specify the X position of the chunk to claim").toString())
            return
        }

        if (!body.containsKey("z")) {
            response.statusCode = 422
            response.end(JsonNodeFactory.instance.objectNode().put("message", "You must specify the Z position of the chunk to claim").toString())
            return
        }

        val x = body.getInteger("x")
        val z = body.getInteger("z")
        // Ensure the chunk is unclaimed
        if (CivilizationChunk.query().where().eq("x", x).eq("z", z).findCount() > 0) {
            response.statusCode = 422
            response.end(JsonNodeFactory.instance.objectNode().put("message", "That chunk has already been claimed").toString())
            return
        }

        val claimingPlayer: Player?
        val civilization: Civilization

        // If the body has a UUID property, look up the associated player
        if (body.containsKey("uuid")) {
            claimingPlayer = Player.query().where().eq("uuid", body.getString("uuid")).findUnique()

            // Make sure the player exists and is part of a civilization
            if (claimingPlayer == null || claimingPlayer.civilization == null) {
                response.statusCode = 422
                response.end(JsonNodeFactory.instance.objectNode().put("message", "Player with UUID ${body.getString("uuid")} is not part of a civilization").toString())
                return
            }

            // Set the civilization to the player's civ
            civilization = claimingPlayer.civilization!!
        } else if (body.containsKey("civilization")) {
            val placeholder = Civilization.byId(body.getLong("civilization"))
            if (placeholder == null) {
                response.statusCode = 422
                response.end(JsonNodeFactory.instance.objectNode().put("message", "Civilization with ID " + body.getString("civilization") + " does not exist").toString())
                return
            }
            civilization = placeholder
            claimingPlayer = null
        } else {
            response.statusCode = 422
            response.end(JsonNodeFactory.instance.objectNode().put("message", "You must specify the player that is claiming the chunk").toString())
            return
        }

        val chunk = CivilizationChunk()
        chunk.position = Vector2i(x, z)
        chunk.civilization = civilization
        chunk.claimingPlayer = claimingPlayer
        chunk.save()

        response.statusCode = 200
        response.end(Json.encode(chunk))
    }
}