package de.wpaul.fritztemp

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.experimental.launch
import java.util.*

class Server(private val logger: TemperatureLogger) {

    private val status: Map<String, String>
        get() {
            return mapOf("running" to "OK",
                    "log file" to logger.config.logPath,
                    "log file size" to logger.config.logFile.length().toString(),
                    "log entries" to logger.config.logFile.readLines().size.toString(),
                    "sensor ain" to logger.config.ain,
                    "log interval" to logger.config.interval.toString(),
                    "temperature" to logger.getTemperature().toString())
        }
    private val server: NettyApplicationEngine

    init {
        server = embeddedServer(Netty, port = 8080) {
            routing {
                install(ContentNegotiation) {
                    jackson {
                        enable(SerializationFeature.INDENT_OUTPUT)
                    }
                }
                install(StatusPages) {
                    status(HttpStatusCode.NotFound) {
                        call.respond(HttpStatusCode.NotFound, "Error 404: Not Found")
                    }
                }

                get("/log") {
                    call.respondFile(logger.config.logFile)
                }
                delete("/log") {
                    logger.deleteLog()
                    call.respond("")
                }
                get("/status") {
                    call.respond(status)
                }
                get("/date") {
                    call.respondText(Date().toString())
                }
                get("/config") { call.respond(logger.config.map) }
                get("/config/{setting}") {
                    call.respond(logger.config.map[call.parameters["setting"]]
                            ?: HttpStatusCode.NotFound)
                }
                put("/config/{setting}") {
                    if (logger.config.map.containsKey((call.parameters["setting"]))) {
                        try {
                            logger.config[call.parameters["setting"]!!] = call.receiveText()
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.BadRequest, "Data validation failed")
                        }
                        call.respond(HttpStatusCode.OK)
                    } else call.respond(HttpStatusCode.NotFound)
                }
                put("/credentials") {
                    val body = call.receiveText().lines()
                    if (body.size < 2) call.respond(HttpStatusCode.BadRequest, "Body must have 2 lines")
                    else if (App.instance == null) call.respond(HttpStatusCode.InternalServerError)
                    else {
                        App.instance?.username = body[0]
                        App.instance?.password = body[1]
                        call.respond(HttpStatusCode.OK)
                    }
                }
                get("{...}") { call.respond(HttpStatusCode.NotFound) }
            }
        }
        launch {
            server.start(true)
        }
    }
}