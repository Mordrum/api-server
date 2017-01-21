package com.mordrum.apiserver.controllers

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.mordrum.apiserver.models.Civilization
import com.mordrum.apiserver.models.FishingRecord
import com.mordrum.apiserver.models.Player
import com.mordrum.apiserver.util.*
import io.ebean.annotation.Transactional
import io.vertx.ext.sync.Sync.fiberHandler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.*

class FishingController(router: Router) : AbstractController(router) {
    init {
        router.get("/fishing").handler(fiberHandler { find(it) } )
        router.post("/fishing").handler(fiberHandler { createRecord(it) } )
    }

    @Suspendable
    @Transactional
    private fun find(ctx: RoutingContext) {
        val records: List<FishingRecord>
        if (!ctx.request().params().isEmpty) {
            val queryString = getQueryMap(ctx)
            val where = FishingRecord.query().fetch("player").where()
            for ((key, value) in queryString) {
                if (key == "with") continue
                where.eq(key, value)
            }
            records = where.findList()
        } else {
            records = FishingRecord.query().fetch("player").findList()
        }

        val arrayNode = JsonNodeFactory.instance.arrayNode()
        records.forEach { arrayNode.add(it.toJson(ctx.request().params().getAll("with"))) }
        success(ctx, arrayNode)
    }


    @Suspendable
    @Transactional
    private fun createRecord(ctx: RoutingContext) {
        val body = bodyAsJson(ctx)
        val uuid = UUID.fromString(body.getString("uuid"))
        val weight = body.getDouble("weight")
        val fish = body.getInteger("fish")

        var player = Player.query().where().eq("uuid", uuid).findUnique()
        if (player == null) {
            player = Player()
            player.uuid = uuid
            player.save()
        }

        val isHighscore = FishingRecord.query().where().eq("fish", fish).gt("weight", weight).findCount() <= 0
        val record = FishingRecord(player, fish, weight)
        record.save()

        return success(ctx, record.toJson().put("is_highscore", isHighscore))
    }
}