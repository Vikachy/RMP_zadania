package config

data class AnimalCharacteristics(
    val weight: Double,
    val maxCount: Int,
    val speed: Double,
    val foodNeeded: Double
)

data class SimulationConfig(
    val islandWidth: Int,
    val islandHeight: Int,
    val initialAnimals: Map<String, Int>,
    val plantGrowthProbability: Int,
    val simulationSpeed: Long,
    val stopCondition: () -> Boolean,
    val animalDataFile: String
)

fun readSimulationConfig(): SimulationConfig {
    println("Введите параметры симуляции:")
    print("Ширина острова: ")
    val width = readLine()?.toIntOrNull() ?: 20
    print("Высота острова: ")
    val height = readLine()?.toIntOrNull() ?: 20

    val initialAnimals = mutableMapOf<String, Int>()
    println("Введите начальное количество животных каждого вида (или 0, если не хотите добавлять):")
    val animals = listOf("Волк", "Удав", "Лиса", "Медведь", "Орел", "Лошадь", "Олень", "Кролик", "Мышь", "Коза", "Овца", "Кабан", "Буйвол", "Утка", "Гусеница")
    for (animal in animals) {
        print("Количество $animal: ")
        val count = readLine()?.toIntOrNull() ?: 0
        initialAnimals[animal] = count
    }

    print("Вероятность появления растений (%): ")
    val plantGrowth = readLine()?.toIntOrNull() ?: 20
    print("Длительность такта симуляции (секунд): ")
    val speed = readLine()?.toLongOrNull() ?: 7
    print("Путь к файлу с данными (CSV): ")
    val animalDataFile = readLine() ?: "data/animals.csv"

    return SimulationConfig(
        islandWidth = width,
        islandHeight = height,
        initialAnimals = initialAnimals.toMap(),
        plantGrowthProbability = plantGrowth,
        simulationSpeed = speed * 1000,
        stopCondition = { false },
        animalDataFile = animalDataFile
    )
}
