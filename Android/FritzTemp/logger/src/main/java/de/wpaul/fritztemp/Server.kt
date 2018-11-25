package de.wpaul.fritztemp

import de.wpaul.fritztempcommons.DateTimeConverter
import de.wpaul.fritztempcommons.Measurement
import de.wpaul.fritztempcommons.Status
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.event.Level
import toliner.kotatu.klaxon
import java.util.*

class Server(private val logger: TemperatureLogger) {

    private val status: Status
        get() {
            return Status(if (logger.config.sensor == null) "Sensor not configured!" else "OK",
                    logger.config.sensor ?: "",
                    logger.config.interval,
                    logger.db.measurementsDao().countAllDistinct(),
                    logger.db.measurementsDao().getYoungestEntry()?.timestamp,
                    logger.getTemperature()?.toString() ?: "")
        }
    private val server: NettyApplicationEngine
    private val dateConverter = DateTimeConverter()

    init {
        server = embeddedServer(Netty, port = 8080) {
            routing {
                install(ContentNegotiation) {
                    klaxon()
                }
                install(StatusPages) {
                    status(HttpStatusCode.NotFound) {
                        call.respond(HttpStatusCode.NotFound, "Error 404: Not Found")
                    }
                }
                install(CallLogging) {
                    level = Level.INFO
                }

                get("/log") {
                    val date = dateConverter.toDateTimeOrNull(call.parameters["after"])
                    call.respond(logger.getLogCsvString(date))
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
                get("/config") { call.respond(logger.config) }
                get("/config/{setting}") {
                    call.respond(logger.config[call.parameters["setting"]!!]
                            ?: HttpStatusCode.NotFound)
                }
                put("/config/{setting}") {
                    if (logger.config.hasProperty((call.parameters["setting"]!!))) {
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
                    when {
                        body.size < 2 -> call.respond(HttpStatusCode.BadRequest, "Body must have 2 lines")
                        App.instance == null -> call.respond(HttpStatusCode.InternalServerError)
                        else -> {
                            App.instance?.username = body[0]
                            App.instance?.password = body[1]
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
                put("/log") {
                    val body = call.receiveText()
                    logger.db.measurementsDao().insert(body.lines().map { line -> Measurement.parse(line) })
                    call.respond(HttpStatusCode.OK)
                }
                get("/database.sqlite") {
                    call.respondFile(logger.dbFile)
                }
                get("{...}") { call.respond(HttpStatusCode.NotFound) }
            }
        }
        GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
            server.start(true)
        }
    }
}