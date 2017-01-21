package com.mordrum.apiserver

import co.paralleluniverse.fibers.Suspendable
import com.mordrum.apiserver.controllers.ChunksController
import com.mordrum.apiserver.controllers.CivilizationController
import com.mordrum.apiserver.controllers.FishingController
import com.mordrum.apiserver.controllers.PlayerController
import io.ebean.EbeanServer
import io.ebean.EbeanServerFactory
import io.ebean.config.ServerConfig
import io.vertx.core.http.HttpServer
import io.vertx.ext.sync.SyncVerticle
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import org.avaje.datasource.DataSourceConfig
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.DriverManager
import java.util.*

class MainVerticle() : SyncVerticle() {
    lateinit var ebean: EbeanServer
    lateinit var http: HttpServer

    @Suspendable
    override fun start() {
        // Configure and initialize Ebean
        val serverConfig = ServerConfig()

        val properties = Properties()
        properties.load(Files.newInputStream(Paths.get("ebean.properties")))
        serverConfig.loadFromProperties(properties)
        serverConfig.addPackage("com.mordrum.apiserver.models")
        this.ebean = EbeanServerFactory.create(serverConfig)

        // Generate the routes
        val router = Router.router(vertx)
        router.route().handler { BodyHandler.create().handle(it) }
        ChunksController(router)
        CivilizationController(router)
        FishingController(router)
        PlayerController(router)

        // Setup an HTTP server
        this.http = vertx.createHttpServer()
        http.requestHandler({
            router.accept(it)
        })
        http.listen(config().getInteger("http.port", 8080))
    }

    @Suspendable
    override fun stop() {
        this.http.close()
    }


}
