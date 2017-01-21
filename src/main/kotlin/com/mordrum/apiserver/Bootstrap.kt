package com.mordrum.apiserver

import com.ea.agentloader.AgentLoader
import io.vertx.core.Vertx
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    saveDefaultConfig()

    val options = getOptions(args)

    if (options.hasOption("dev")) loadAgents()

    Vertx.vertx().deployVerticle(MainVerticle())
}

private fun saveDefaultConfig() {
    if (Files.notExists(Paths.get("ebean.properties"))) {
        val stream = ClassLoader.getSystemResourceAsStream("ebean.properties")
        Files.copy(stream, Paths.get("ebean.properties"))
    }
}

private fun loadAgents() {
    try {
        AgentLoader.loadAgentClass(io.ebean.enhance.agent.Transformer::class.qualifiedName, "")
        AgentLoader.loadAgentClass(co.paralleluniverse.fibers.instrument.JavaAgent::class.qualifiedName, "")
    } catch (e: Exception) {
        if (e !is ClassNotFoundException && e !is NoClassDefFoundError) {
            System.out.println("Failed to load runtime instrumentation: " + e.message)
            System.out.println("This message can be ignored if using a fatjar that was instrumented AOT")
        } else {
            System.out.println("Running with AOT instrumentation")
        }
    }
}


private fun getOptions(args: Array<String>): CommandLine {
    val devOption = Option.builder("dev")
            .desc("dynamically loads Quasar and Ebean Java agents")
            .build()
    val confOption = Option.builder("c")
            .desc("the configuration file to use, in JSON format")
            .hasArg()
            .longOpt("conf")
            .build()

    val options = Options()
    options.addOption(devOption)
    options.addOption(confOption)
    return DefaultParser().parse(options, args)
}