package ru.yarsu

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonFactoryBuilder
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.http4k.core.*
import org.http4k.lens.contentType
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.domain.MovementType
import ru.yarsu.domain.Product
import ru.yarsu.domain.ProductMovement
import ru.yarsu.domain.ProductMovementStore
import ru.yarsu.domain.ProductStore
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

fun main() {
    val productMovementStore = ProductMovementStore.fromCsv()
    val productStore = ProductStore.fromCsv()
    val pingRouteHandler: HttpHandler = appRoutes(productMovementStore, productStore)
    val server = pingRouteHandler.asServer(Netty(9000))
    server.start()
    println("Application is available on http://localhost:${server.port()}")
    val request = Request(
        Method.GET,
        Uri.of("http://localhost:9000/top-products?product-count=11")
    )
    println(request)
    println(pingRouteHandler(request))
}


fun appRoutes(productMovement: ProductMovementStore, product: ProductStore): HttpHandler {
    return routes(
        "/top-products" bind Method.GET to topProductsCommand(raschitivanieTop(productMovement, product)
        )
    )

}

fun raschitivanieTop(productMovement: ProductMovementStore, product: ProductStore): List<List<String>> {
    val productList = product.list()
    val productMovementList = productMovement.list()
    val spisok = mutableListOf<List<String>>()
    for (p in productList){
        spisok.add(listOf(p.name, productMovementList.count { it.productId == p.id }.toString()))
    }
//    val slovar = mutableMapOf<String, Double>()
//    for (p in productList) {
//        slovar[p.name] = productMovementList.count { it.productId == p.id }.toDouble()
//    }
    return spisok.sortedByDescending { it[1].toDouble() }
}

//fun sortirovka(slovar: Map<String, Double>): List<List<String>>{
//    val spisok = mutableListOf<List<String>>()
//    for (i in slovar){
//        spisok.add(listOf(i.key, i.value.toString()))
//    }
//    return spisok.sortedByDescending { it[1].toDouble() }
//}

fun topProductsCommand(spisok: List<List<String>>): HttpHandler = HttpHandler@{ request: Request ->
    val productCount = request.query("product-count") ?: "5"
    val stringWriter = StringWriter()
    val factory: JsonFactory = JsonFactoryBuilder().build()
    val outputGenerator: JsonGenerator = factory.createGenerator(stringWriter)
    outputGenerator.prettyPrinter = DefaultPrettyPrinter()
    try{
        if(productCount.toInt() < 0 || productCount.toInt() > spisok.size){
            with(outputGenerator) {
                writeStartObject()
                writeFieldName("Error")
                writeString("Incorrect product-count")
                writeEndObject()
                close()
            }
            return@HttpHandler Response(Status.BAD_REQUEST).body(stringWriter.toString())
        }
    }
    catch (e: Exception){
        with(outputGenerator) {
            writeStartObject()
            writeFieldName("Error")
            writeString("Incorrect product-count")
            writeEndObject()
            close()
        }
        return@HttpHandler Response(Status.BAD_REQUEST).body(stringWriter.toString())
    }

    with(outputGenerator) {
        writeStartArray()
        for (i in 0..productCount.toInt()-1) {
            writeStartObject()
            writeFieldName("ProductName")
            writeString(spisok[i][0])
            writeFieldName("ConsumedAmount")
            writeNumber(spisok[i][1].toDouble())
            writeEndObject()
        }
        writeEndArray()
        close()
    }
    Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(stringWriter.toString())
}

