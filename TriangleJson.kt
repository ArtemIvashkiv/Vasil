package ru.yarsu

import java.util.UUID
import kotlin.math.max
import kotlin.math.sqrt

data class TriangleJson(
    val id: UUID,
    val sideA: Double,
    val sideB: Double,
    val sideC: Double,
    val borderColor: Colour
) {
    private val semiPerimeter: Double = (sideA + sideB + sideC) / 2
    var area: Double =
        sqrt((semiPerimeter * (semiPerimeter - sideA) * (semiPerimeter - sideB) * (semiPerimeter - sideC)).toDouble())


    val maxStorona: Double = max(max(sideA, sideB), sideC)

    val perimeter: Double = sideA + sideB + sideC

    val triangleView: String = "Треугольник со сторонами ${sides()[0]}, ${sides()[1]}, ${sides()[2]}"


    fun sides(): List<Double> = listOf(sideA, sideB, sideC).sortedDescending()
}

fun TriangleJson.triangleType(): TriangleType {
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
