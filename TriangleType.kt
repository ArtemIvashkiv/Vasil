package ru.yarsu

enum class TriangleType (val description: String) {
    INCORRECT("Некорректный треугольник"),
    SEGMENT("Отрезок"),
    SHARPANGLED("Остроугольный треугольник"),
    RIGHTANGLED("Прямоугольный треугольник"),
    BLUNTANGLED("Тупоугольный треугольник"),
    UNKNOWN("Неизвестный треугольник")
    ;
}
