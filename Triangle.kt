package ru.yarsu

import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.*

class Triangle(
    val _id: UUID,
    val _sideA: Double,
    val _sideB: Double,
    val _sideC: Double,
    val _registrationDateTime: LocalDateTime,
    val _borderColour: Colour,
    val _fillColour: Colour,
    val _description: String
) {
    val id = _id
    val sideA = _sideA
    val sideB = _sideB
    val sideC = _sideC
    val registrationDateTime = _registrationDateTime
    val borderColour: Colour = _borderColour
    val fillColour: Colour = _fillColour
    val description = _description

    private val semiPerimeter: Double = (sideA + sideB + sideC) / 2
    var area: Double = sqrt((semiPerimeter * (semiPerimeter - sideA) * (semiPerimeter - sideB) * (semiPerimeter - sideC)).toDouble())



    val maxStorona: Double = max(max(sideA, sideB), sideC)

    val perimeter: Double = sideA + sideB + sideC

    val triangleView: String = "Треугольник со сторонами ${sides()[0]}, ${sides()[1]}, ${sides()[2]}"


    fun sides(): List<Double> = listOf(sideA, sideB, sideC).sortedDescending()
}

fun Triangle.triangleType(): TriangleType {
    val storoni = sides()
    if (storoni[0] > storoni[1] + storoni[2]) {
        return TriangleType.INCORRECT
    } else if (storoni[0] == storoni[1] + storoni[2]) {
        return TriangleType.SEGMENT
    } else if (storoni[0] * storoni[0] < storoni[1] * storoni[1] + storoni[2] * storoni[2]) {
        return TriangleType.SHARPANGLED
    } else if (storoni[0] * storoni[0] == storoni[1] * storoni[1] + storoni[2] * storoni[2]) {
        return TriangleType.RIGHTANGLED
    } else if (storoni[0] * storoni[0] > storoni[1] * storoni[1] + storoni[2] * storoni[2]) {
        return TriangleType.BLUNTANGLED
    } else {
        return TriangleType.UNKNOWN
    }
}
