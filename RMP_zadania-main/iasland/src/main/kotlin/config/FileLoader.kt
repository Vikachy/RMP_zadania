package config

import java.io.FileInputStream
import java.io.IOException

fun loadDataFromFile(filePath: String): Pair<Map<String, Map<String, Int>>, Map<String, AnimalCharacteristics>> {
    val eatProbabilities = mutableMapOf<String, MutableMap<String, Int>>()
    val animalCharacteristics = mutableMapOf<String, AnimalCharacteristics>()

    try {
        val file = FileInputStream(filePath).bufferedReader()
        val lines = file.readLines()

        if (lines.size < 2) {
            println("Файл CSV должен содержать как минимум 2 строки (заголовок и данные).")
            file.close()
            return Pair(eatProbabilities.toMap(), animalCharacteristics.toMap())
        }

        val header = lines[0].split(",").map { it.trim().replace("\"", "") }
        val animalNames = header.subList(1, header.size).toMutableList()

        for (i in 1 until lines.size) {
            val parts = lines[i].split(",").map { it.trim().replace("\"", "") }
            if (parts.size != header.size) {
                println("Некорректное количество столбцов в строке ${i + 1}. Ожидалось ${header.size}, получено ${parts.size}.")
                continue
            }

            val eater = parts[0]
            if (animalNames.contains(eater)) {
                val preyProbabilities = mutableMapOf<String, Int>()
                for (j in 1 until parts.size) {
                    val prey = header[j]
                    val probability = parts[j].toIntOrNull() ?: 0
                    preyProbabilities[prey] = probability
                }
                eatProbabilities[eater] = preyProbabilities
            } else {
                println("Некорректное имя животного в строке ${i+1}: $eater")
            }
        }

        val animalChar = AnimalCharacteristics(50.0, 20, 3.0, 10.0)
        for (animal in animalNames) {
            animalCharacteristics[animal] = animalChar
        }

        file.close()
    } catch (e: IOException) {
        println("Ошибка при чтении файла: ${e.message}")
    }

    return Pair(eatProbabilities.toMap(), animalCharacteristics.toMap())
}
