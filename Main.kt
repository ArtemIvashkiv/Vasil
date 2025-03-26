package ru.yarsu

import com.beust.jcommander.JCommander
import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.math.*
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import java.io.FileReader
import java.nio.charset.StandardCharsets
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.http4k.core.*
import org.http4k.client.*
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.web.routes.ApplicationRoutes
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.net.HttpURLConnection
import java.net.URL
import java.io.File

fun main(args: Array<String>) {
    val fileName: String? = "src/main/resources/Triangles.csv"

    val triangleList = mutableListOf<Triangle>()
    csvReader().open(fileName.toString()) {
        val triangles: List<List<String>> = readAllAsSequence().toList().drop(1)
        for (t in triangles) {
            triangleList.add(
                Triangle(
                    UUID.fromString(t[0]),
                    t[1].toDouble(),
                    t[2].toDouble(),
                    t[3].toDouble(),
                    LocalDateTime.parse(t[4], DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    Colour.valueOf(t[5]),
                    Colour.valueOf(t[6]),
                    t[7]
                )
            )
        }
    }

    val app = ApplicationRoutes().applicationRoutes(triangleList)
    app.asServer(Netty(9000)).start()
    val request = Request(
        Method.POST,
        Uri.of("http://localhost:9000//v1/triangleNew?id=a6be5e23-b8d4-4d78-8aa3-1c204d0befc7&sideA=4&sideB=5&sideC=6&registrationDateTime=2023-08-26T19:01:41.114511727&borderColour=BLUE&fillColour=GREEN&description=newTriangle")
    )
    println(request)
    println(app(request))

    csvWriter().open("src/main/resources/example.csv") {
        val triangles = mutableListOf<List<String>>()
        triangles.add(
            listOf(
                "Id",
                "SideA",
                "SideB",
                "SideC",
                "RegistrationDateTime",
                "BorderColor",
                "FillColor",
                "Description"
            )
        )
        for (t in triangleList) {
            triangles.add(
                listOf(
                    t.id.toString(),
                    t.sideA.toString(),
                    t.sideB.toString(),
                    t.sideC.toString(),
                    t.registrationDateTime.toString(),
                    t.borderColour.toString(),
                    t.fillColour.toString(),
                    t.description
                )
            )
        }
        writeRows(triangles)
    }



//    val client = ApacheClient()
//    val request = Request(
//        Method.GET,
//        Uri.of("https://lms.crafted.su/web-app-development/2024-2-it-3/docs/course/02-http-introduction/10-basic-http-server/triangles.json")
//    )
//    val response = client(request)
//    val message = response.body
//    if (response.status.successful) {
//        File("src/main/resources/example.json").writeText(message.toString())
//        println(response.status)
//    } else
//        println(response.status)

//    val triangleList: List<TriangleJson> = jacksonObjectMapper().readValue(File("src/main/resources/example.json"))
//
//    var slovar = mutableMapOf<String, Int>()
//    for (t in triangleList) {
//        if (!slovar.containsKey(t.borderColor.toString())) {
//            slovar[t.borderColor.toString()] = triangleList.count { it.borderColor == t.borderColor }
//        }
//    }
//    slovar = slovar.toSortedMap()
//    println("Статистика по цветам:")
//    for (c in slovar) {
//        println("* ${c.key}: ${c.value}")
//    }
//

//    val listCommand = ListCommand()
//    val showCommand = ShowCommand()
//    val listColourCommand = ListColourCommand()
//    val listAreaCommand = ListAreaCommand()
//    val statisticCommand = StatisticCommand()
//    val commander: JCommander =
//        JCommander
//            .newBuilder()
//            .addCommand("list", listCommand)
//            .addCommand("show", showCommand)
//            .addCommand("list-colour", listColourCommand)
//            .addCommand("list-area", listAreaCommand)
//            .addCommand("statistic", statisticCommand)
//            .build()
//    commander.parse(*args)
//
//    when (commander.parsedCommand) {
//        "list" -> listCommand.list()
//        "show" -> showCommand.show()
//        "list-colour" -> listColourCommand.listColour()
//        "list-area" -> listAreaCommand.listArea()
//        "statistic" -> statisticCommand.statistic()
//    }

}



