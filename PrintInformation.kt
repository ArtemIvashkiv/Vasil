package ru.yarsu.hhtpHandler

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import org.http4k.core.*
import org.http4k.lens.contentType
import org.http4k.routing.path
import ru.yarsu.helpFunction.HelpFunction
import ru.yarsu.trianglesStorage.Color
import ru.yarsu.trianglesStorage.TriangleStorage
import ru.yarsu.trianglesStorage.typeOfTriangle
import ru.yarsu.userStorage.UserStorage
import java.io.StringWriter
import java.util.UUID
import kotlin.system.exitProcess

class PrintInformation {
    companion object {
        fun listTriangles(triangleStorage: TriangleStorage): HttpHandler = HttpHandler@{ request: Request ->
            val pageParam = request.query("page")?.toIntOrNull() ?: 1
            val recordsPerPageParam = request.query("records-per-page")?.toIntOrNull() ?: 10


            if (pageParam < 1) {
                return@HttpHandler Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body("Error: Page number must be >= 1.")
            }

            if (recordsPerPageParam !in listOf(5, 10, 20, 50)) {
                return@HttpHandler Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body("Error: records-per-page must be one of the following values: 5, 10, 20, 50.")
            }


            val triangles = triangleStorage.storage.sortedWith(compareBy({ it.registrationDateTime }, { it.id }))


            val totalRecords = triangles.size
            val totalPages = (totalRecords + recordsPerPageParam - 1) / recordsPerPageParam


            if (pageParam > totalPages) {
                return@HttpHandler Response(Status.OK).contentType(ContentType.APPLICATION_JSON)
                    .body("[]")
            }


            val startIndex = (pageParam - 1) * recordsPerPageParam
            val endIndex = minOf(startIndex + recordsPerPageParam, totalRecords)


            val paginatedTasks = triangles.subList(startIndex, endIndex)


            val stringWriter = StringWriter()
            val factory = JsonFactory()
            val outputGenerator: JsonGenerator = factory.createGenerator(stringWriter)

            outputGenerator.prettyPrinter = DefaultPrettyPrinter()

            with(outputGenerator) {
                if (paginatedTasks.isNotEmpty()) {
                    writeStartArray()
                    for (triangle in paginatedTasks) {
                        writeStartObject()
                        writeStringField("id", triangle.id.toString())
                        writeStringField("registrationDateTime", triangle.registrationDateTime.toString())
                        writeStringField("description", triangle.description)
                        writeEndObject()
                    }
                    writeEndArray()
                    outputGenerator.close()
                }
            }
            Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(stringWriter.toString())
        }
        fun printTaskById(triangleStorage: TriangleStorage, userStorage: UserStorage): HttpHandler =
            HttpHandler@{ request: Request ->
                val triangles = triangleStorage.storage
                val users = userStorage.storage

                val triangleIdParam = request.path("triangle-id")

                try {
                    if (triangleIdParam.isNullOrEmpty()) {
                        return@HttpHandler Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body("Error: task-id cannot be null or empty")
                    }

                    val uuid = UUID.fromString(triangleIdParam)
                    val factory = JsonFactory()
                    val stringWriter = StringWriter()
                    val outputGenerator: JsonGenerator = factory.createGenerator(stringWriter)
                    outputGenerator.prettyPrinter = DefaultPrettyPrinter()


                    with(outputGenerator) {
                        val taskFound = triangles.find { it.id.toString() == triangleIdParam }
                        if (taskFound == null) {
                            writeStartObject()
                            writeStringField("triangle-id", triangleIdParam)
                            writeStringField("error", "Треугольник не найден")
                            writeEndObject()
                            outputGenerator.close()
                            return@HttpHandler Response(Status.NOT_FOUND).contentType(ContentType.APPLICATION_JSON).body(stringWriter.toString())
                        }
                        else{
                                writeStartObject()
                                writeStringField("Id", taskFound.id.toString())
                                writeNumberField("SideA", taskFound.A)
                                writeNumberField("SideB", taskFound.B)
                                writeNumberField("SideC", taskFound.C)
                                writeStringField("RegistrationDateTime", taskFound.registrationDateTime.toString())
                                writeStringField("BorderColor", taskFound.borderColor.toString())
                                writeStringField("FillColor", taskFound.fillColor.toString())
                                writeStringField("Description", taskFound.description)
                                if (!(taskFound.getArea.toString().equals("NaN"))) {
                                    writeNumberField("Area", taskFound.getArea)
                                } else {
                                    writeFieldName("Area")
                                    writeNull()
                                }
                                writeStringField("Type", taskFound.typeOfTriangle())
                                val author = users.find { it.id.toString() == taskFound.owner.toString() }
                                if(author != null){
                                    writeStringField("Owner", author.id.toString())
                                    writeStringField("OwnerLogin", author.login)
                                }
                                writeEndObject()
                        }
                        outputGenerator.close()
                    }
                    Response(Status.OK)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(stringWriter.toString())


                }catch (e:IllegalArgumentException){
                    Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body("{\n" +
                            "\"error\": \"Некорректный идентификатор треугольника. Для параметра triangle-id ожидается UUID, но получено значение «id»\"\n" +
                            "}")
                }
            }
        fun statisticColor(triangleStorage: TriangleStorage):Response{
            val list = triangleStorage.storage
            val stringWriter = StringWriter()
            val factory2: JsonFactory = JsonFactory()
            val outputGenerator2: JsonGenerator = factory2.createGenerator(stringWriter)
            outputGenerator2.prettyPrinter = DefaultPrettyPrinter()
            val statistics = mutableMapOf<String, Int>()

            for (triangle in list) {
                statistics[triangle.fillColor.toString()] =
                    statistics.getOrDefault(triangle.fillColor.toString(), 0) + 1
            }
            val sortedStatistic = statistics.toList().sortedBy { it.first }.toMap()

            with(outputGenerator2) {
                writeStartObject()
                writeFieldName("statisticByColor")
                writeStartArray()
                for (triangle in sortedStatistic) {
                    writeStartObject()
                    writeStringField("color",triangle.key)
                    writeNumberField("count", triangle.value)
                    writeEndObject()
                }
                outputGenerator2.close()
            }
            return Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(stringWriter.toString())


        }
        fun statisticType(triangleStorage: TriangleStorage):Response{
            val list = triangleStorage.storage
            val factory2: JsonFactory = JsonFactory()
            val stringWriter = StringWriter()
            val outputGenerator2: JsonGenerator = factory2.createGenerator(stringWriter)
            outputGenerator2.prettyPrinter = DefaultPrettyPrinter()
            val statistics = mutableMapOf<String, Int>()

            for (triangle in list) {
                statistics[triangle.typeOfTriangle()] =
                    statistics.getOrDefault(triangle.typeOfTriangle().toString(), 0) + 1
            }
            val sortedStatistic = statistics.toList().sortedBy { it.first }.toMap()

            with(outputGenerator2) {
                writeStartObject()
                writeFieldName("statisticByType")
                writeStartArray()
                for (triangle in sortedStatistic) {
                    writeStartObject()
                    writeStringField("type",triangle.key)
                    writeNumberField("count", triangle.value)
                    writeEndObject()
                }
                outputGenerator2.close()
            }
            return Response(Status.OK)
                .contentType(ContentType.APPLICATION_JSON)
                .body(stringWriter.toString())


        }
        fun statisticColorAndType(triangleStorage: TriangleStorage):Response {
            val list = triangleStorage.storage
            val stringWriter = StringWriter()
            val factory2: JsonFactory = JsonFactory()
            val outputGenerator2: JsonGenerator = factory2.createGenerator(stringWriter)
            outputGenerator2.prettyPrinter = DefaultPrettyPrinter()
            val statisticsType = mutableMapOf<String, Int>()

            for (triangle in list) {
                statisticsType[triangle.typeOfTriangle()] =
                    statisticsType.getOrDefault(triangle.typeOfTriangle().toString(), 0) + 1
            }
            val sortedStatisticType = statisticsType.toList().sortedBy { it.first }.toMap()
            val statisticsColor = mutableMapOf<String, Int>()

            for (triangle in list) {
                statisticsColor[triangle.fillColor.toString()] =
                    statisticsColor.getOrDefault(triangle.fillColor.toString(), 0) + 1
            }
            val sortedStatisticColor = statisticsColor.toList().sortedBy { it.first }.toMap()
            with(outputGenerator2) {
                writeStartObject()
                writeFieldName("statisticByColor")
                writeStartArray()
                for (triangle in sortedStatisticColor) {
                    writeStartObject()
                    writeStringField("color",triangle.key)
                    writeNumberField("count", triangle.value)
                    writeEndObject()
                }
                writeEndArray()

                writeFieldName("statisticByType")
                writeStartArray()
                for (triangle in sortedStatisticType) {
                    writeStartObject()
                    writeStringField("type",triangle.key)
                    writeNumberField("count", triangle.value)
                    writeEndObject()
                }
                writeEndArray()
                writeEndObject()
                outputGenerator2.close()
            }
            return Response(Status.OK)
                .contentType(ContentType.APPLICATION_JSON)
                .body(stringWriter.toString())


        }
        fun listColor(triangleStorage: TriangleStorage): HttpHandler = HttpHandler@{ request: Request ->
            val list = triangleStorage.storage.sortedWith(compareBy({ it.registrationDateTime }, { it.id }))

            val borderColor = request.query("border-color")
            if (borderColor.isNullOrEmpty()) {
                return@HttpHandler Response(Status.BAD_REQUEST)
                    .contentType(ContentType.APPLICATION_JSON)
                    .body("{\"error\": \"Отсутствует обязательный параметр border-color\"}")
            }

            if (borderColor !in Color.entries.map { it.name }) {
                return@HttpHandler Response(Status.BAD_REQUEST)
                    .contentType(ContentType.APPLICATION_JSON)
                    .body("{\"error\": \"Некорректный цвет. Для параметра border-color ожидается цвет, но получено значение «${borderColor}»\"}")
            }


            val pageParam = request.query("page")?.toIntOrNull() ?: 1
            val recordsPerPageParam = request.query("records-per-page")?.toIntOrNull() ?: 10


            if (pageParam < 1) {
                return@HttpHandler Response(Status.BAD_REQUEST)
                    .contentType(ContentType.APPLICATION_JSON)
                    .body("{\"error\": \"Page number must be >= 1.\"}")
            }


            if (recordsPerPageParam !in listOf(5, 10, 20, 50)) {
                return@HttpHandler Response(Status.BAD_REQUEST)
                    .contentType(ContentType.APPLICATION_JSON)
                    .body("{\"error\": \"records-per-page must be one of the following values: 5, 10, 20, 50.\"}")
            }


            val filteredTriangles = list.filter { it.borderColor.toString() == borderColor}

            val totalRecords = filteredTriangles.size
            val totalPages = (totalRecords + recordsPerPageParam - 1) / recordsPerPageParam


            if (pageParam > totalPages) {
                return@HttpHandler Response(Status.OK)
                    .contentType(ContentType.APPLICATION_JSON)
                    .body("[]") // Возвращаем пустой JSON массив
            }


            val startIndex = (pageParam - 1) * recordsPerPageParam
            val endIndex = minOf(startIndex + recordsPerPageParam, totalRecords)


            val paginatedTasks = filteredTriangles.subList(startIndex, endIndex)







            val stringWriter = StringWriter()
            val factory2: JsonFactory = JsonFactory()
            val outputGenerator2: JsonGenerator = factory2.createGenerator(stringWriter)

            outputGenerator2.prettyPrinter = DefaultPrettyPrinter()

            with(outputGenerator2) {
                writeStartArray()

                for (triangle in paginatedTasks) {
                    writeStartObject()
                    writeStringField("id", triangle.id.toString())
                    writeNumberField("sideA", triangle.A)
                    writeNumberField("sideB", triangle.B)
                    writeNumberField("sideC", triangle.C)
                    writeEndObject()
                }

                writeEndArray()
                close()
            }


            Response(Status.OK)
                .contentType(ContentType.APPLICATION_JSON)
                .body(stringWriter.toString())
        }



    }
}
