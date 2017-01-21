package com.mordrum.apiserver.util

import com.fasterxml.jackson.databind.node.BaseJsonNode
import com.google.common.collect.ImmutableMap
import io.vertx.core.json.DecodeException
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

fun validationError(ctx: RoutingContext, message: String) {
    simpleError(ctx, 422, message)
}

fun success(ctx: RoutingContext, json: BaseJsonNode) {
    ctx.response().statusCode = 200
    ctx.response().putHeader("content-type", "application/json")
    ctx.response().end(JsonUtil.prettyPrint(json))
}

fun simpleError(ctx: RoutingContext, statusCode: Int, message: String) {
    ctx.response().statusCode = statusCode
    val node = JsonUtil.newObject().put("message", message)
    ctx.response().end(JsonUtil.prettyPrint(node))
}

fun bodyAsJson(ctx: RoutingContext): JsonObject {
    try {
        return ctx.bodyAsJson
    } catch (e: DecodeException) {
        return JsonObject()
    }
}

fun getQueryMap(ctx: RoutingContext): Map<String, Any> {
    val params = ctx.request().params()

    return params.associateBy({it.key}, {
        try {
            Integer.parseInt(it.value)
        } catch (e: NumberFormatException) {
            it.value
        }
    })
}