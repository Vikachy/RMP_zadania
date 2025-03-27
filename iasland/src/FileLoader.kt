import java.io.File
import java.io.IOException

fun loadDataFromFile(filePath: String): Pair<Map<String, Map<String, Int>>, Map<String, AnimalCharacteristics>> {
    val eatProbabilities = mutableMapOf<String, MutableMap<String, Int>>()
    val animalCharacteristics = mutableMapOf<String, AnimalCharacteristics>()

    try {
        val lines = File(filePath).readLines()
        if (lines.size < 2) {
            println("Ошибка: CSV файл должен содержать хотя бы 2 строки")
            return eatProbabilities to animalCharacteristics
        }

        val headers = lines[0].split(",").map { it.trim().removeSurrounding("\"") }
        val animals = headers.drop(1)

        lines.drop(1).forEach { line ->
            val values = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (values.size != headers.size) {
                println("Предупреждение: Пропущена строка с некорректным количеством колонок")
                return@forEach
            }

            val predator = values[0]
            if (predator !in animals) {
                println("Предупреждение: Неизвестный хищник '$predator'")
                return@forEach
            }

            val probabilities = mutableMapOf<String, Int>()
            for (i in 1 until values.size) {
                val prey = headers[i]
                val probability = values[i].toIntOrNull() ?: 0
                probabilities[prey] = probability
            }
            eatProbabilities[predator] = probabilities
        }

        // Установка характеристик по умолчанию для всех животных
        val defaultCharacteristics = AnimalCharacteristics(
            weight = 50.0,
            maxCount = 20,
            speed = 3.0,
            foodNeeded = 10.0
        )
        animals.forEach {
            animalCharacteristics[it] = defaultCharacteristics
        }

    } catch (e: IOException) {
        println("Ошибка при чтении файла: ${e.message}")
    }

    return eatProbabilities to animalCharacteristics
}