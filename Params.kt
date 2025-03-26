package ru.yarsu

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonFactoryBuilder
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

//@Parameters(separators = "=")
//open class Arguments {
//    @Parameter(names = ["--triangles-file"], required = true)
//    var fileName: String? = null
//
//    fun schitivanieTriangle(): List<Triangle> {
//        val list = mutableListOf<Triangle>()
//        csvReader().open(fileName.toString()) {
//            val triangles: List<List<String>> = readAllAsSequence().toList().drop(1)
//            for (t in triangles) {
//                list.add(
//                    Triangle(
//                        UUID.fromString(t[0]),
//                        t[1].toDouble(),
//                        t[2].toDouble(),
//                        t[3].toDouble(),
//                        LocalDateTime.parse(t[4], DateTimeFormatter.ISO_LOCAL_DATE_TIME),
//                        Colour.valueOf(t[5]),
//                        Colour.valueOf(t[6]),
//                        t[7]
//                    )
//                )
//            }
//        }
//        return list
//    }
//
//}
//
//@Parameters(separators = "=", commandNames = ["list"])
//class ListCommand : Arguments() {
//    fun list() {
//        val list = schitivanieTriangle()
//        val sortedList = list.sortedWith(compareBy({ it.registrationDateTime }, { it.id }))
//
//        val factory: JsonFactory = JsonFactoryBuilder().build()
//        val outputGenerator: JsonGenerator = factory.createGenerator(System.out)
//        outputGenerator.prettyPrinter = DefaultPrettyPrinter()
//        with(outputGenerator) {
//            writeStartObject()
//            writeFieldName("triangles")
//            writeStartArray()
//            for (t in sortedList) {
//                writeStartObject()
//                writeFieldName("description")
//                writeString(t.description)
//                writeFieldName("id")
//                writeString(t.id.toString())
//                writeFieldName("registrationDateTime")
//                writeString(t.registrationDateTime.toString())
//                writeEndObject()
//            }
//            writeEndArray()
//            writeEndObject()
//            close()
//        }
//    }
//}
//
//
//@Parameters(separators = "=", commandNames = ["show"])
//class ShowCommand : Arguments() {
//    @Parameter(names = ["--id"], required = true)
//    var id: String? = null
//    fun show() {
//        val list = schitivanieTriangle()
//        val factory: JsonFactory = JsonFactoryBuilder().build()
//        val outputGenerator: JsonGenerator = factory.createGenerator(System.out)
//        outputGenerator.prettyPrinter = DefaultPrettyPrinter()
//        with(outputGenerator) {
//            writeStartObject()
//            writeFieldName("id")
//            writeString(id)
//            for (t in list) {
//                if (t.id == UUID.fromString(id)) {
//                    writeFieldName("triangle")
//                    writeStartObject()
//                    writeFieldName("borderColour")
//                    writeString(t.borderColour.toString())
//                    writeFieldName("fillColour")
//                    writeString(t.fillColour.toString())
//                    writeFieldName("description")
//                    writeString(t.description)
//                    writeFieldName("id")
//                    writeString(t.id.toString())
//                    writeFieldName("registrationDateTime")
//                    writeString(t.registrationDateTime.toString())
//                    writeFieldName("sideA")
//                    writeString(t.sideA.toString())
//                    writeFieldName("sideB")
//                    writeString(t.sideB.toString())
//                    writeFieldName("sideC")
//                    writeString(t.sideC.toString())
//                    writeFieldName("area")
//                    if (t.area.isNaN()) {
//                        writeString("null")
//                    } else {
//                        writeString(t.area.toString())
//                    }
//                    writeFieldName("type")
//                    writeString(t.triangleType().toString())
//                    writeEndObject()
//                    break
//                }
//            }
//            writeEndObject()
//            close()
//        }
//    }
//}
//
//
//@Parameters(separators = "=", commandNames = ["list-colour"])
//class ListColourCommand : Arguments() {
//    @Parameter(names = ["--border-colour"], required = true)
//    var borderColour: String? = null
//    fun listColour() {
//        val list = schitivanieTriangle()
//        val sortedList = list.sortedWith(compareBy({ it.registrationDateTime }, { it.id }))
//            .filter { it.borderColour == Colour.valueOf(borderColour.toString()) }
//
//        val factory: JsonFactory = JsonFactoryBuilder().build()
//        val outputGenerator: JsonGenerator = factory.createGenerator(System.out)
//        outputGenerator.prettyPrinter = DefaultPrettyPrinter()
//        with(outputGenerator) {
//            writeStartObject()
//            writeFieldName("borderColour")
//            writeString(borderColour)
//            writeFieldName("triangles")
//            writeStartArray()
//            for (t in sortedList) {
//                writeStartObject()
//                writeFieldName("id")
//                writeString(t.id.toString())
//                writeFieldName("sideA")
//                writeString(t.sideA.toString())
//                writeFieldName("sideB")
//                writeString(t.sideB.toString())
//                writeFieldName("sideC")
//                writeString(t.sideC.toString())
//                writeEndObject()
//            }
//            writeEndArray()
//            writeEndObject()
//            close()
//        }
//    }
//}
//
//
//@Parameters(separators = "=", commandNames = ["list-area"])
//class ListAreaCommand : Arguments() {
//    @Parameter(names = ["--area-min"])
//    var areaMin: Double = -1.0
//
//    @Parameter(names = ["--area-max"])
//    var areaMax: Double = -1.0
//    fun listArea() {
//        val list = schitivanieTriangle()
//        var sortedList =
//            list.sortedWith(compareBy({ it.registrationDateTime }, { it.id })).filter { !it.area.isNaN() }
//        if (areaMin != -1.0) {
//            if (areaMax != -1.0) {
//                sortedList = sortedList.filter { it.area >= areaMin && it.area <= areaMax }
//            } else {
//                sortedList = sortedList.filter { it.area >= areaMin }
//            }
//        } else {
//            if (areaMax != -1.0) {
//                sortedList = sortedList.filter { it.area <= areaMax }
//            }
//        }
//        val factory: JsonFactory = JsonFactoryBuilder().build()
//        val outputGenerator: JsonGenerator = factory.createGenerator(System.out)
//        outputGenerator.prettyPrinter = DefaultPrettyPrinter()
//        with(outputGenerator) {
//            writeStartObject()
//            if (areaMin != -1.0) {
//                writeFieldName("areaMin")
//                writeString(areaMin.toString())
//            }
//            if (areaMax != -1.0) {
//                writeFieldName("ariaMax")
//                writeString(areaMax.toString())
//            }
//            writeFieldName("triangles")
//            writeStartArray()
//            for (t in sortedList) {
//                writeStartObject()
//                writeFieldName("id")
//                writeString(t.id.toString())
//                writeFieldName("sideA")
//                writeString(t.sideA.toString())
//                writeFieldName("sideB")
//                writeString(t.sideB.toString())
//                writeFieldName("sideC")
//                writeString(t.sideC.toString())
//                writeEndObject()
//            }
//            writeEndArray()
//            writeEndObject()
//            close()
//        }
//    }
//}
//
//
//@Parameters(separators = "=", commandNames = ["statistic"])
//class StatisticCommand : Arguments() {
//    @Parameter(names = ["--by"], required = true)
//    var colourOrType: String? = null
//    fun statistic() {
//        val list = schitivanieTriangle()
//        if (colourOrType == "colour") {
//            var slovar = mutableMapOf<String, Int>()
//            for (t in list) {
//                if (!slovar.containsKey(t.fillColour.toString())) {
//                    slovar[t.fillColour.toString()] = list.count { it.fillColour == t.fillColour }
//                }
//            }
//            slovar = slovar.toSortedMap()
//            val factory: JsonFactory = JsonFactoryBuilder().build()
//            val outputGenerator: JsonGenerator = factory.createGenerator(System.out)
//            outputGenerator.prettyPrinter = DefaultPrettyPrinter()
//            with(outputGenerator) {
//                writeStartObject()
//                writeFieldName("statisticByColour")
//                writeStartArray()
//                for (c in slovar) {
//                    writeStartObject()
//                    writeFieldName("colour")
//                    writeString(c.key.toString())
//                    writeFieldName("count")
//                    writeString(c.value.toString())
//                    writeEndObject()
//                }
//                writeEndArray()
//                writeEndObject()
//                close()
//            }
//        } else if (colourOrType == "type") {
//            var slovar2 = mutableMapOf<String, Int>()
//            for (t in list) {
//                if (!slovar2.containsKey(t.triangleType().description)) {
//                    slovar2[t.triangleType().description] = list.count { it.triangleType() == t.triangleType() }
//                }
//            }
//            slovar2 = slovar2.toSortedMap()
//            val factory: JsonFactory = JsonFactoryBuilder().build()
//            val outputGenerator: JsonGenerator = factory.createGenerator(System.out)
//            outputGenerator.prettyPrinter = DefaultPrettyPrinter()
//            with(outputGenerator) {
//                writeStartObject()
//                writeFieldName("statisticByType")
//                writeStartArray()
//                for (t in slovar2) {
//                    writeStartObject()
//                    writeFieldName("type")
//                    writeString(t.key.toString())
//                    writeFieldName("count")
//                    writeString(t.value.toString())
//                    writeEndObject()
//                }
//                writeEndArray()
//                writeEndObject()
//                close()
//            }
//        } else if (colourOrType == "colour,type" || colourOrType == "type,colour") {
//            var slovar = mutableMapOf<String, Int>()
//            var slovar2 = mutableMapOf<String, Int>()
//
//            for (t in list) {
//                if (!slovar.containsKey(t.fillColour.toString())) {
//                    slovar[t.fillColour.toString()] = list.count { it.fillColour == t.fillColour }
//                }
//            }
//            for (t in list) {
//                if (!slovar2.containsKey(t.triangleType().description)) {
//                    slovar2[t.triangleType().description] = list.count { it.triangleType() == t.triangleType() }
//                }
//            }
//
//            slovar = slovar.toSortedMap()
//            slovar2 = slovar2.toSortedMap()
//            val factory: JsonFactory = JsonFactoryBuilder().build()
//            val outputGenerator: JsonGenerator = factory.createGenerator(System.out)
//            outputGenerator.prettyPrinter = DefaultPrettyPrinter()
//            with(outputGenerator) {
//                writeStartObject()
//                writeFieldName("statisticByColour")
//                writeStartArray()
//                for (c in slovar) {
//                    writeStartObject()
//                    writeFieldName("colour")
//                    writeString(c.key.toString())
//                    writeFieldName("count")
//                    writeString(c.value.toString())
//                    writeEndObject()
//                }
//                writeEndArray()
//                writeEndObject()
//                writeStartObject()
//                writeFieldName("statisticByType")
//                writeStartArray()
//                for (t in slovar2) {
//                    writeStartObject()
//                    writeFieldName("type")
//                    writeString(t.key.toString())
//                    writeFieldName("count")
//                    writeString(t.value.toString())
//                    writeEndObject()
//                }
//                writeEndArray()
//                writeEndObject()
//                close()
//            }
//        }
//    }
//}
//
