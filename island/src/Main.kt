import kotlin.random.Random
import kotlin.system.exitProcess

// Класс для хранения параметров симуляции
data class SimulationConfig(
    val islandWidth: Int,
    val islandHeight: Int,
    val initialHerbivores: Int,
    val initialPredators: Int,
    val plantGrowthProbability: Int,
    val simulationSpeed: Long,
    val stopCondition: () -> Boolean
)

// Абстрактный класс Animal
abstract class Animal(val name: String, var x: Int, var y: Int, val symbol: String) {
    abstract fun move()
    abstract fun reproduce(): Animal?
    abstract fun die(): Boolean

    // Метод для проверки, находится ли животное в пределах острова
    fun isWithinBounds(island: Array<Array<Location>>): Boolean {
        return x in island.indices && y in island[0].indices
    }
}

// Класс для травоядных животных
class Herbivore(name: String, x: Int, y: Int, symbol: String) : Animal(name, x, y, symbol) {
    override fun move() {
        // Случайное направление движения
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val (dx, dy) = directions.random()
        x += dx
        y += dy
    }

    override fun reproduce(): Animal? {
        // Вероятность размножения 50%
        return if (Random.nextInt(100) < 50) {
            Herbivore(name, x, y, symbol) // Создаем нового травоядного
        } else {
            null
        }
    }

    override fun die(): Boolean {
        // Вероятность смерти 10%
        return Random.nextInt(100) < 10
    }
}

// Класс для хищников
class Predator(name: String, x: Int, y: Int, symbol: String) : Animal(name, x, y, symbol) {
    override fun move() {
        // Случайное направление движения
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val (dx, dy) = directions.random()
        x += dx
        y += dy
    }

    override fun reproduce(): Animal? {
        // Вероятность размножения 30%
        return if (Random.nextInt(100) < 30) {
            Predator(name, x, y, symbol) // Создаем нового хищника
        } else {
            null
        }
    }

    override fun die(): Boolean {
        // Вероятность смерти 20%
        return Random.nextInt(100) < 20
    }
}

// Класс для растений
class Plant(val x: Int, val y: Int, val symbol: String)

// Класс для локации (клетки острова)
class Location {
    val plants = mutableListOf<Plant>()
    val animals = mutableListOf<Animal>()
}

// Класс для острова
class Island(val width: Int, val height: Int) {
    val grid = Array(width) { Array(height) { Location() } }

    // Инициализация острова
    fun initialize(config: SimulationConfig) {
        // Добавляем травоядных
        repeat(config.initialHerbivores) {
            addAnimal(Herbivore("Олень", Random.nextInt(width), Random.nextInt(height), "🦌"))
        }

        // Добавляем хищников
        repeat(config.initialPredators) {
            addAnimal(Predator("Волк", Random.nextInt(width), Random.nextInt(height), "🐺"))
        }

        // Добавляем растения
        for (i in 0 until width) {
            for (j in 0 until height) {
                if (Random.nextInt(100) < config.plantGrowthProbability) {
                    grid[i][j].plants.add(Plant(i, j, "🌿"))
                }
            }
        }
    }

    // Добавление животного на остров
    fun addAnimal(animal: Animal) {
        if (animal.isWithinBounds(grid)) {
            grid[animal.x][animal.y].animals.add(animal)
        }
    }

    // Обновление состояния острова
    fun update() {
        val newAnimals = mutableListOf<Animal>()

        for (i in 0 until width) {
            for (j in 0 until height) {
                val location = grid[i][j]
                val animalsToRemove = mutableListOf<Animal>()

                for (animal in location.animals) {
                    animal.move()
                    if (!animal.isWithinBounds(grid)) {
                        animalsToRemove.add(animal)
                    } else {
                        val offspring = animal.reproduce()
                        if (offspring != null) {
                            newAnimals.add(offspring)
                        }
                        if (animal.die()) {
                            animalsToRemove.add(animal)
                        }
                    }
                }

                location.animals.removeAll(animalsToRemove)
            }
        }

        newAnimals.forEach { addAnimal(it) }
    }

    // Получение состояния острова для визуализации
    fun getGridState(): Array<Array<String>> {
        return Array(width) { i ->
            Array(height) { j ->
                when {
                    grid[i][j].animals.any { it is Predator } -> "🐺" // Хищник
                    grid[i][j].animals.any { it is Herbivore } -> "🦌" // Травоядное
                    grid[i][j].plants.isNotEmpty() -> "🌿" // Растение
                    else -> "⬜" // Пустая клетка
                }
            }
        }
    }
}

// Функция для отображения острова в консоли
fun printIsland(grid: Array<Array<String>>) {
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            print(grid[i][j])
        }
        println()
    }
    println()
}

// Функция для ввода параметров симуляции
fun readSimulationConfig(): SimulationConfig {
    println("Введите параметры симуляции:")
    print("Ширина острова: ")
    val width = readLine()?.toIntOrNull() ?: 20
    print("Высота острова: ")
    val height = readLine()?.toIntOrNull() ?: 20
    print("Количество травоядных: ")
    val herbivores = readLine()?.toIntOrNull() ?: 10
    print("Количество хищников: ")
    val predators = readLine()?.toIntOrNull() ?: 5
    print("Вероятность появления растений (%): ")
    val plantGrowth = readLine()?.toIntOrNull() ?: 20
    print("Длительность такта симуляции (секунд): ")
    val speed = readLine()?.toLongOrNull() ?: 1L

    return SimulationConfig(
        islandWidth = width,
        islandHeight = height,
        initialHerbivores = herbivores,
        initialPredators = predators,
        plantGrowthProbability = plantGrowth,
        simulationSpeed = speed * 1000, // Преобразование секунд в миллисекунды
        stopCondition = {
            false // Условие остановки можно изменить
        }
    )
}


// Основная функция
fun main() {
    val config = readSimulationConfig()
    val island = Island(config.islandWidth, config.islandHeight)
    island.initialize(config)

    // Запуск симуляции
    while (true) {
        island.update()
        printIsland(island.getGridState())
        Thread.sleep(config.simulationSpeed)

        // Условие остановки (например, если все животные вымерли)
        if (island.grid.all { row -> row.all { it.animals.isEmpty() } }) {
            println("Все животные вымерли. Симуляция завершена.")
            exitProcess(0)
        }
    }
}
