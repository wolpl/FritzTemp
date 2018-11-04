//modified from https://github.com/toliner/Kotatu-ktor-klaxon
package de.wpaul.fritztemp

import com.beust.klaxon.Klaxon
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.content.TextContent
import io.ktor.features.ContentConverter
import io.ktor.features.ContentNegotiation
import io.ktor.features.suitableCharset
import io.ktor.http.ContentType
import io.ktor.http.withCharset
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.readRemaining

class KlaxonConverter(private val klaxon: Klaxon = Klaxon()) : ContentConverter {

    @io.ktor.util.KtorExperimentalAPI
    override suspend fun convertForSend(context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any): Any? {
        return TextContent(klaxon.toJsonString(value), contentType.withCharset(context.call.suitableCharset()))
    }

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        val request = context.subject
        val channel = request.value as? ByteReadChannel ?: return null
        val reader = channel.readRemaining().readText().reader()
        return klaxon.parse(reader)
    }
}

fun ContentNegotiation.Configuration.klaxon(block: Klaxon.() -> Unit) {
    val converter = KlaxonConverter(Klaxon().apply(block))
    register(ContentType.Application.Json, converter)
}

fun ContentNegotiation.Configuration.klaxon() {
    register(ContentType.Application.Json, KlaxonConverter())
}
