package com.mordrum.apiserver

import com.mordrum.apiserver.util.JsonUtil
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
class TestCivilizationController {
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
    fun testFindNoCivs(ctx: TestContext) {
        val async = ctx.async()
        vertx.createHttpClient().getNow(port, "localhost", "/civilizations", { response ->
            ctx.assertEquals(response.statusCode(), 200)
            ctx.assertEquals(response.getHeader("content-type"), "application/json")

            response.handler { body ->
                ctx.assertTrue(body.toString() == "[]")
                async.complete()
            }
        })
    }

    @Test
    fun testCreateCiv(ctx: TestContext) {
        val async = ctx.async()
        val json = JsonUtil.newObject()
                .put("player", UUID.randomUUID().toString())
                .put("banner", "fake_banner")
                .put("name", "test_civ")
                .put("home_x", 0)
                .put("home_z", 0)
                .toString()
        vertx.createHttpClient().post(port, "localhost", "/civilizations")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", json.length.toString())
                .handler({ response ->
                    response.handler { responseBody ->
                        ctx.assertEquals(response.statusCode(), 200)
                        ctx.assertEquals(response.getHeader("content-type"), "application/json")

                        val parsedBody = JsonObject(responseBody.toString())
                        ctx.assertTrue(parsedBody == JsonObject().put("id", 1))
                        async.complete()
                    }
                })
                .write(json)
                .end()
    }
}