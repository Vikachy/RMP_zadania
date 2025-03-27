import java.util.*

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
    val scanner = Scanner(System.`in`)

    println("=== Конфигурация симуляции ===")

    print("Ширина острова (по умолчанию 20): ")
    val width = scanner.nextLine().toIntOrNull() ?: 20

    print("Высота острова (по умолчанию 20): ")
    val height = scanner.nextLine().toIntOrNull() ?: 20

    val animals = listOf(
        "Волк", "Удав", "Лиса", "Медведь", "Орел",
        "Лошадь", "Олень", "Кролик", "Мышь", "Коза",
        "Овца", "Кабан", "Буйвол", "Утка", "Гусеница"
    )

    val initialAnimals = mutableMapOf<String, Int>()
    println("\nВведите начальное количество животных:")
    animals.forEach { animal ->
        print("$animal (по умолчанию 0): ")
        initialAnimals[animal] = scanner.nextLine().toIntOrNull() ?: 0
    }

    print("\nВероятность роста растений (%, по умолчанию 20): ")
    val plantGrowth = scanner.nextLine().toIntOrNull() ?: 20

    print("Скорость симуляции (сек, по умолчанию 1): ")
    val speed = scanner.nextLine().toLongOrNull() ?: 1

    print("Путь к файлу данных (по умолчанию data/animals.csv): ")
    val dataFile = scanner.nextLine().ifEmpty { "data/animals.csv" }

    return SimulationConfig(
        islandWidth = width,
        islandHeight = height,
        initialAnimals = initialAnimals,
        plantGrowthProbability = plantGrowth,
        simulationSpeed = speed * 1000,
        stopCondition = { false },
        animalDataFile = dataFile
    )
}