package ru.yarsu.web.routes

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonFactoryBuilder
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.http4k.core.*
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.body.form
import org.http4k.core.body.formAsMap
import org.http4k.lens.contentType
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.yarsu.Colour
import ru.yarsu.Triangle
import ru.yarsu.triangleType
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ApplicationRoutes {
    fun applicationRoutes(list: MutableList<Triangle>): HttpHandler {
        return routes(
            "/v1/list-triangles" bind Method.GET to listCommand(list),
            "/v1/statistic-by-color" bind Method.GET to statisticByColourCommand(list),
            "/v1/triangle/{triangle-id}" bind Method.GET to showByIdCommand(list),
            "/v1/triangleNew" bind Method.POST to newTriangleCommand(list),
            "/v1/triangle" bind Method.GET to {
                Response(BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body(
                    "{\n" +
                            "\"error\": \"Отсутствует обязательный параметр triangle-id\"\n" +
                            "} "
                )
            }
        )
    }

    fun newTriangleCommand(list: MutableList<Triangle>): HttpHandler = HttpHandler@{ request: Request ->
        val id = request.query("id") ?: return@HttpHandler Response(Status.BAD_REQUEST).body("Error: id is not found")
        val sideA =
            request.query("sideA") ?: return@HttpHandler Response(Status.BAD_REQUEST).body("Error: sideA is not found")
        val sideB =
            request.query("sideB") ?: return@HttpHandler Response(Status.BAD_REQUEST).body("Error: sideB is not found")
        val sideC =
            request.query("sideC") ?: return@HttpHandler Response(Status.BAD_REQUEST).body("Error: sideC is not found")
        val registrationDateTime = request.query("registrationDateTime")
            ?: return@HttpHandler Response(Status.BAD_REQUEST).body("Error: registrationDateTime is not found")
        val borderColour = request.query("borderColour")
            ?: return@HttpHandler Response(Status.BAD_REQUEST).body("Error: borderColour is not found")
        val fillColour = request.query("fillColour")
            ?: return@HttpHandler Response(Status.BAD_REQUEST).body("Error: fillColour is not found")
        val description = request.query("description")
            ?: return@HttpHandler Response(Status.BAD_REQUEST).body("Error: description is not found")
        val newTriangle = Triangle(
            UUID.fromString(id),
            sideA.toDouble(),
            sideB.toDouble(),
            sideC.toDouble(),
            LocalDateTime.parse(registrationDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            Colour.valueOf(borderColour),
            Colour.valueOf(fillColour),
            description
        )
        list.add(newTriangle)
        val stringWriter = StringWriter()
        val factory: JsonFactory = JsonFactoryBuilder().build()
        val outputGenerator: JsonGenerator = factory.createGenerator(stringWriter)
        outputGenerator.prettyPrinter = DefaultPrettyPrinter()
        with(outputGenerator) {
            writeStartObject()
            for (t in list) {
                if (t.id == UUID.fromString(id)) {
                    writeFieldName("triangle")
                    writeStartObject()
                    writeFieldName("borderColour")
                    writeString(t.borderColour.toString())
                    writeFieldName("fillColour")
                    writeString(t.fillColour.toString())
                    writeFieldName("description")
                    writeString(t.description)
                    writeFieldName("id")
                    writeString(t.id.toString())
                    writeFieldName("registrationDateTime")
                    writeString(t.registrationDateTime.toString())
                    writeFieldName("sideA")
                    writeString(t.sideA.toString())
                    writeFieldName("sideB")
                    writeString(t.sideB.toString())
                    writeFieldName("sideC")
                    writeString(t.sideC.toString())
                    writeFieldName("area")
                    if (t.area.isNaN()) {
                        writeString("null")
                    } else {
                        writeString(t.area.toString())
                    }
                    writeFieldName("type")
                    writeString(t.triangleType().toString())
                    writeEndObject()
                    writeEndObject()
                    close()
                    break
                }
            }
        }
        Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(stringWriter.toString())
    }

    fun listCommand(list: List<Triangle>): HttpHandler = HttpHandler@{ request: Request ->
        val sortedList = list.sortedWith(compareBy({ it.registrationDateTime }, { it.id }))

        val stringWriter = StringWriter()
        val factory: JsonFactory = JsonFactoryBuilder().build()
        val outputGenerator: JsonGenerator = factory.createGenerator(stringWriter)
        outputGenerator.prettyPrinter = DefaultPrettyPrinter()
        with(outputGenerator) {
            writeStartObject()
            writeFieldName("triangles")
            writeStartArray()
            for (t in sortedList) {
                writeStartObject()
                writeFieldName("description")
                writeString(t.description)
                writeFieldName("id")
                writeString(t.id.toString())
                writeFieldName("registrationDateTime")
                writeString(t.registrationDateTime.toString())
                writeEndObject()
            }
            writeEndArray()
            writeEndObject()
            outputGenerator.close()
        }
        Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(stringWriter.toString())
    }

    fun statisticByColourCommand(list: List<Triangle>): HttpHandler = HttpHandler@{ request: Request ->
        var slovar = mutableMapOf<String, Int>()
        for (t in list) {
            if (!slovar.containsKey(t.fillColour.toString())) {
                slovar[t.fillColour.toString()] = list.count { it.fillColour == t.fillColour }
            }
        }
        slovar = slovar.toSortedMap()
        val stringWriter = StringWriter()
        val factory: JsonFactory = JsonFactoryBuilder().build()
        val outputGenerator: JsonGenerator = factory.createGenerator(stringWriter)
        outputGenerator.prettyPrinter = DefaultPrettyPrinter()
        with(outputGenerator) {
            writeStartObject()
            writeFieldName("statisticByColour")
            writeStartArray()
            for (c in slovar) {
                writeStartObject()
                writeFieldName("colour")
                writeString(c.key.toString())
                writeFieldName("count")
                writeString(c.value.toString())
                writeEndObject()
            }
            writeEndArray()
            writeEndObject()
            close()
        }
        Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(stringWriter.toString())
    }

    fun showByIdCommand(list: List<Triangle>): HttpHandler = HttpHandler@{ request: Request ->
        val id = request.path("triangle-id")
        try {
            if (id.isNullOrEmpty()) {
                return@HttpHandler Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON)
                    .body("Error: task-id cannot be null or empty")
            }
            val stringWriter = StringWriter()
            val factory: JsonFactory = JsonFactoryBuilder().build()
            val outputGenerator: JsonGenerator = factory.createGenerator(stringWriter)
            outputGenerator.prettyPrinter = DefaultPrettyPrinter()
            with(outputGenerator) {
                var flag = false
                writeStartObject()
                writeFieldName("id")
                writeString(id)
                for (t in list) {
                    if (t.id == UUID.fromString(id)) {
                        flag = true
                        writeFieldName("triangle")
                        writeStartObject()
                        writeFieldName("borderColour")
                        writeString(t.borderColour.toString())
                        writeFieldName("fillColour")
                        writeString(t.fillColour.toString())
                        writeFieldName("description")
                        writeString(t.description)
                        writeFieldName("id")
                        writeString(t.id.toString())
                        writeFieldName("registrationDateTime")
                        writeString(t.registrationDateTime.toString())
                        writeFieldName("sideA")
                        writeString(t.sideA.toString())
                        writeFieldName("sideB")
                        writeString(t.sideB.toString())
                        writeFieldName("sideC")
                        writeString(t.sideC.toString())
                        writeFieldName("area")
                        if (t.area.isNaN()) {
                            writeString("null")
                        } else {
                            writeString(t.area.toString())
                        }
                        writeFieldName("type")
                        writeString(t.triangleType().toString())
                        writeEndObject()
                        writeEndObject()
                        close()
                        break
                    }
                }
                if (!flag) {
                    writeFieldName("error")
                    writeString("Triangle is not found")
                    writeEndObject()
                    close()
                    return@HttpHandler Response(Status.NOT_FOUND).contentType(ContentType.APPLICATION_JSON)
                        .body(stringWriter.toString())
                }
            }
            Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(stringWriter.toString())
        } catch (e: IllegalArgumentException) {
            Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body(
                "{\n" +
                        "\"error\": \"Некорректный идентификатор треугольника. Для параметра triangle-id ожидается UUID, но получено значение «id»\"\n" +
                        "}"
            )
        }

    }

}