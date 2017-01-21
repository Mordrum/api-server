package com.mordrum.apiserver

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.ServerSocket
import java.util.*

@RunWith(VertxUnitRunner::class)
class TestFishingController {
    lateinit var vertx: Vertx
    var port: Int = 0

    @Before
    fun setUp(ctx: TestContext) {
        val socket = ServerSocket(0)
        port = socket.localPort
        socket.close()

        val options = DeploymentOptions()
                .setConfig(JsonObject().put("http.port", port))

        vertx = Vertx.vertx()
        vertx.deployVerticle(MainVerticle::class.qualifiedName, options, ctx.asyncAssertSuccess())
    }

    @After
    fun tearDown(ctx: TestContext) {
        vertx.close(ctx.asyncAssertSuccess())
    }

    @Test
    fun testCreateRecord(ctx: TestContext) {
        val async = ctx.async()
        val json = JsonObject()
                .put("uuid", UUID.randomUUID().toString())
                .put("weight", 1.0)
                .put("fish", 0)
                .toString()
        vertx.createHttpClient().post(port, "localhost", "/fishing")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", json.length.toString())
                .handler({ response ->
                    response.handler { responseBody ->
                        ctx.assertEquals(response.statusCode(), 200)
                        ctx.assertEquals(response.getHeader("content-type"), "application/json")

                        val expectedJson = JsonObject()
                                .put("id",1)
                                .put("player", 1)
                                .put("weight", 1.0)
                                .put("fish", 0)
                                .put("is_highscore", true)
                        val parsedBody = JsonObject(responseBody.toString())
                        ctx.assertTrue(parsedBody == expectedJson)
                        async.complete()
                    }
                })
                .write(json)
                .end()
    }
}