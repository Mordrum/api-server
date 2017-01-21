package com.mordrum.apiserver.controllers

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.mordrum.apiserver.models.Player
import com.mordrum.apiserver.util.AbstractController
import com.mordrum.apiserver.util.simpleError
import com.mordrum.apiserver.util.success
import io.ebean.annotation.Transactional
import io.vertx.core.json.Json
import io.vertx.ext.sync.Sync.fiberHandler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.*

class PlayerController(router: Router) : AbstractController(router) {
    init {
        router.get("/players/:uuid").handler(fiberHandler {
            findOne(it)
        })
    }

    @Suspendable
    @Transactional
    private fun findOne(ctx: RoutingContext) {
        val uuidString = ctx.pathParam("uuid")
        val uuid = UUID.fromString(uuidString)
        val player = Player.query().fetch("civilization").where().eq("uuid", uuid).findUnique()
        if (player == null) {
            simpleError(ctx, 404, "Player with UUID $uuidString not found")
        } else {
            success(ctx, player.toJson(ctx.request().params().getAll("with")))
        }
    }
}