import kotlin.random.Random
import kotlin.system.exitProcess
import java.util.concurrent.ThreadLocalRandom
import java.io.FileInputStream
import java.io.IOException

// Загрузка данных из файла (CSV формат)
fun loadDataFromFile(filePath: String): Pair<Map<String, Map<String, Int>>, Map<String, AnimalCharacteristics>> {
    val eatProbabilities = mutableMapOf<String, MutableMap<String, Int>>()
    val animalCharacteristics = mutableMapOf<String, AnimalCharacteristics>()

    try {
        val file = FileInputStream(filePath).bufferedReader()
        val lines = file.readLines()

        // Проверяем, что есть как минимум две строки (заголовок и данные)
        if (lines.size < 2) {
            println("Файл CSV должен содержать как минимум 2 строки (заголовок и данные).")
            file.close()
            return Pair(eatProbabilities.toMap(), animalCharacteristics.toMap())
        }

        val header = lines[0].split(",").map { it.trim().replace("\"", "") }
        val animalNames = header.subList(1, header.size).toMutableList()

        // Заполнение вероятностей поедания
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
            }
            else{
                println("Некорректное имя животного в строке ${i+1}: $eater")
            }
        }

        // Устанавливаем характеристики животных (можно раскомментировать, если нужна загрузка характеристик из файла)
        val animalChar = AnimalCharacteristics(50.0, 20, 3.0, 10.0)
        val animals = listOf("Волк", "Удав", "Лиса", "Медведь", "Орел", "Лошадь", "Олень", "Кролик", "Мышь", "Коза", "Овца", "Кабан", "Буйвол", "Утка", "Гусеница")

        for (animal in animals) {
            animalCharacteristics[animal] = animalChar
        }

        file.close()

    } catch (e: IOException) {
        println("Ошибка при чтении файла: ${e.message}")
    }

    return Pair(eatProbabilities.toMap(), animalCharacteristics.toMap())
}

// Класс для хранения характеристик животного
data class AnimalCharacteristics(
    val weight: Double,
    val maxCount: Int,
    val speed: Double,
    val foodNeeded: Double
)

// Класс для хранения параметров симуляции
data class SimulationConfig(
    val islandWidth: Int,
    val islandHeight: Int,
    val initialAnimals: Map<String, Int>,
    val plantGrowthProbability: Int,
    val simulationSpeed: Long,
    val stopCondition: () -> Boolean,
    val animalDataFile: String
)

// Абстрактный класс Animal
abstract class Animal(
    val name: String,
    var x: Int,
    var y: Int,
    val symbol: String,
    val characteristics: AnimalCharacteristics
) {
    abstract fun move(island: Island)
    abstract fun reproduce(): Animal?
    abstract fun die(): Boolean
    abstract fun eat(location: Location, eatProbabilities: Map<String, Map<String, Int>>)

    // Метод для проверки, находится ли животное в пределах острова
    fun isWithinBounds(island: Array<Array<Location>>): Boolean {
        return x in island.indices && y in island[0].indices
    }
}

// Иерархия хищников
sealed class Predator(name: String, x: Int, y: Int, symbol: String, characteristics: AnimalCharacteristics) :
    Animal(name, x, y, symbol, characteristics) {
    class Wolf(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Predator("Волк", x, y, "🐺", characteristics)

    class Boa(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Predator("Удав", x, y, "🐍", characteristics)

    class Fox(x: Int, y: Int, symbol: String, characteristics: AnimalCharacteristics) :
        Predator("Лиса", x, y, symbol, characteristics)

    class Bear(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Predator("Медведь", x, y, "🐻", characteristics)

    class Eagle(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Predator("Орел", x, y, "🦅", characteristics)

    override fun move(island: Island) {
        // Случайное направление движения
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val (dx, dy) = directions.random()
        val speed = characteristics.speed.toInt()

        for (i in 1..speed) { // Учитываем скорость передвижения
            val newX = x + dx
            val newY = y + dy

            if (newX in island.grid.indices && newY in island.grid[0].indices) { // Проверяем границы острова
                x = newX
                y = newY
            } else {
                //Если животное вышло за границы, остаёмся на месте.
                break
            }
        }
    }

    override fun reproduce(): Animal? {
        // Вероятность размножения зависит от вида
        val reproduceProbability = when (this) {
            is Wolf -> 30
            is Boa -> 20
            is Fox -> 40
            is Bear -> 10
            is Eagle -> 25
        }

        return if (ThreadLocalRandom.current().nextInt(100) < reproduceProbability) {
            when (this) {
                is Wolf -> Wolf(x, y, this.characteristics)
                is Boa -> Boa(x, y, this.characteristics)
                is Fox -> Fox(x, y, this.symbol, this.characteristics)
                is Bear -> Bear(x, y, this.characteristics)
                is Eagle -> Eagle(x, y, this.characteristics)
            }
        } else {
            null
        }
    }

    override fun die(): Boolean {
        // Вероятность смерти зависит от вида
        val dieProbability = when (this) {
            is Wolf -> 15
            is Boa -> 10
            is Fox -> 20
            is Bear -> 5
            is Eagle -> 12
        }
        return ThreadLocalRandom.current().nextInt(100) < dieProbability
    }

    override fun eat(location: Location, eatProbabilities: Map<String, Map<String, Int>>) {
        val prey = location.animals.filter { it !== this }.toMutableList() // Список потенциальной добычи
        if (prey.isNotEmpty()) {
            val eatProbabilityMap = eatProbabilities[this.name] ?: return // Вероятности поедания для этого хищника
            val animalsToRemove = mutableListOf<Animal>()
            for (victim in prey) {
                val eatProbability = eatProbabilityMap[victim.name] ?: 0 // Вероятность съесть конкретную жертву
                if (ThreadLocalRandom.current().nextInt(100) < eatProbability) {
                    animalsToRemove.add(victim)
                    // println("${this.name} съел ${victim.name}")
                    break // Съедаем только одну жертву за раз
                }
            }
            location.animals.removeAll(animalsToRemove)
        }
    }
}

// Иерархия травоядных
sealed class Herbivore(name: String, x: Int, y: Int, symbol: String, characteristics: AnimalCharacteristics) :
    Animal(name, x, y, symbol, characteristics) {
    class Horse(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Herbivore("Лошадь", x, y, "🐎", characteristics)

    class Deer(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Herbivore("Олень", x, y, "🦌", characteristics)

    class Rabbit(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Herbivore("Кролик", x, y, "🐇", characteristics)

    class Mouse(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Herbivore("Мышь", x, y, "🐁", characteristics)

    class Goat(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Herbivore("Коза", x, y, "🐐", characteristics)

    class Sheep(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Herbivore("Овца", x, y, "🐑", characteristics)

    class Boar(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Herbivore("Кабан", x, y, "🐗", characteristics)

    class Buffalo(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Herbivore("Буйвол", x, y, "🐃", characteristics)

    class Duck(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Herbivore("Утка", x, y, "🦆", characteristics)

    class Caterpillar(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Herbivore("Гусеница", x, y, "🐛", characteristics)

    override fun move(island: Island) {
        // Случайное направление движения
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val (dx, dy) = directions.random()
        val speed = characteristics.speed.toInt()

        for (i in 1..speed) { // Учитываем скорость передвижения
            val newX = x + dx
            val newY = y + dy

            if (newX in island.grid.indices && newY in island.grid[0].indices) { // Проверяем границы острова
                x = newX
                y = newY
            } else {
                //Если животное вышло за границы, остаёмся на месте.
                break
            }
        }
    }

    override fun reproduce(): Animal? {
        // Вероятность размножения зависит от вида
        val reproduceProbability = when (this) {
            is Horse -> 20
            is Deer -> 25
            is Rabbit -> 60
            is Mouse -> 70
            is Goat -> 30
            is Sheep -> 25
            is Boar -> 40
            is Buffalo -> 15
            is Duck -> 50
            is Caterpillar -> 80
        }
        return if (ThreadLocalRandom.current().nextInt(100) < reproduceProbability) {
            when (this) {
                is Horse -> Horse(x, y, this.characteristics)
                is Deer -> Deer(x, y, this.characteristics)
                is Rabbit -> Rabbit(x, y, this.characteristics)
                is Mouse -> Mouse(x, y, this.characteristics)
                is Goat -> Goat(x, y, this.characteristics)
                is Sheep -> Sheep(x, y, this.characteristics)
                is Boar -> Boar(x, y, this.characteristics)
                is Buffalo -> Buffalo(x, y, this.characteristics)
                is Duck -> Duck(x, y, this.characteristics)
                is Caterpillar -> Caterpillar(x, y, this.characteristics)
            }
        } else {
            null
        }
    }

    override fun die(): Boolean {
        // Вероятность смерти зависит от вида
        val dieProbability = when (this) {
            is Horse -> 5
            is Deer -> 7
            is Rabbit -> 10
            is Mouse -> 15
            is Goat -> 8
            is Sheep -> 7
            is Boar -> 10
            is Buffalo -> 3
            is Duck -> 12
            is Caterpillar -> 20
        }
        return ThreadLocalRandom.current().nextInt(100) < dieProbability
    }

    override fun eat(location: Location, eatProbabilities: Map<String, Map<String, Int>>) {
        if (this is Duck) {
            // Утка ест гусениц
            if (location.animals.any { it is Caterpillar }) {
                val eatProbabilityMap = eatProbabilities[this.name] ?: return
                val caterpillar = location.animals.find { it is Caterpillar }
                if (caterpillar != null) {
                    val eatProbability = eatProbabilityMap[caterpillar.name] ?: 0
                    if (ThreadLocalRandom.current().nextInt(100) < eatProbability) {
                        location.animals.remove(caterpillar)
                    }
                }
            } else { // Другие травоядные едят растения
                if (location.plants.isNotEmpty()) {
                    location.plants.removeAt(0) // Едим первое растение
                }
            }
        } else { // Другие травоядные едят растения
            if (location.plants.isNotEmpty()) {
                location.plants.removeAt(0) // Едим первое растение
            }
        }
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
    fun initialize(config: SimulationConfig, animalCharacteristics: Map<String, AnimalCharacteristics>) {
        // Добавляем животных
        config.initialAnimals.forEach { (animalName, count) ->
            repeat(count) {
                val x = ThreadLocalRandom.current().nextInt(width)
                val y = ThreadLocalRandom.current().nextInt(height)
                val characteristics = animalCharacteristics[animalName] ?: error("Нет характеристик для животного $animalName")
                val animal = when (animalName) {
                    "Волк" -> Predator.Wolf(x, y, characteristics)
                    "Удав" -> Predator.Boa(x, y, characteristics)
                    "Лиса" -> {
                        val foxSymbol = "🦊"
                        Predator.Fox(x, y, foxSymbol, characteristics)
                    }
                    "Медведь" -> Predator.Bear(x, y, characteristics)
                    "Орел" -> Predator.Eagle(x, y, characteristics)
                    "Лошадь" -> Herbivore.Horse(x, y, characteristics)
                    "Олень" -> Herbivore.Deer(x, y, characteristics)
                    "Кролик" -> Herbivore.Rabbit(x, y, characteristics)
                    "Мышь" -> Herbivore.Mouse(x, y, characteristics)
                    "Коза" -> Herbivore.Goat(x, y, characteristics)
                    "Овца" -> Herbivore.Sheep(x, y, characteristics)
                    "Кабан" -> Herbivore.Boar(x, y, characteristics)
                    "Буйвол" -> Herbivore.Buffalo(x, y, characteristics)
                    "Утка" -> Herbivore.Duck(x, y, characteristics)
                    "Гусеница" -> Herbivore.Caterpillar(x, y, characteristics)
                    else -> error("Неизвестный тип животного: $animalName")
                }
                addAnimal(animal)
            }
        }

        // Добавляем растения
        for (i in 0 until width) {
            for (j in 0 until height) {
                if (ThreadLocalRandom.current().nextInt(100) < config.plantGrowthProbability) {
                    grid[i][j].plants.add(Plant(i, j, "🌿"))
                }
            }
        }
    }

    // Добавляем животное на остров
    fun addAnimal(animal: Animal) {
        if (animal.isWithinBounds(grid)) {
            grid[animal.x][animal.y].animals.add(animal)
        }
    }

    // Обновляем состояние острова
    fun update(eatProbabilities: Map<String, Map<String, Int>>) {
        val newAnimals = mutableListOf<Animal>()

        for (i in 0 until width) {
            for (j in 0 until height) {
                val location = grid[i][j]
                val animalsToRemove = mutableListOf<Animal>()

                //Едим
                for (animal in location.animals) {
                    animal.eat(location, eatProbabilities)
                }

                //Двигаемся
                for (animal in location.animals) {
                    animal.move(this)
                }

                //Размножаемся и умираем
                for (animal in location.animals) {
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

    // Получаем состояние острова для визуализации
    fun getGridState(): Array<Array<String>> {
        return Array(width) { i ->
            Array(height) { j ->
                when {
                    grid[i][j].animals.any { it is Predator } -> grid[i][j].animals.first { it is Predator }.symbol // Хищник
                    grid[i][j].animals.any { it is Herbivore } -> grid[i][j].animals.first { it is Herbivore }.symbol // Травоядное
                    grid[i][j].plants.isNotEmpty() -> "🌿" // Растение
                    else -> "⬜" // Пустая клетка
                }
            }
        }
    }

    // Статистика по острову
    fun getStatistics(): String {
        var herbivoresCount = 0
        var predatorsCount = 0
        var plantsCount = 0

        for (i in 0 until width) {
            for (j in 0 until height) {
                herbivoresCount += grid[i][j].animals.count { it is Herbivore }
                predatorsCount += grid[i][j].animals.count { it is Predator }
                plantsCount += grid[i][j].plants.size
            }
        }

        return "Травоядные: $herbivoresCount, Хищники: $predatorsCount, Растения: $plantsCount"
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

    // Ввод количества животных каждого вида
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
    val animalDataFile = readLine() ?: "C:\\Users\\NATA\\Downloads\\Таблицы-к-Острову.csv"

    return SimulationConfig(
        islandWidth = width,
        islandHeight = height,
        initialAnimals = initialAnimals.toMap(),
        plantGrowthProbability = plantGrowth,
        simulationSpeed = speed * 1000, // Преобразование секунд в миллисекунды
        stopCondition = {
            false // Условие остановки можно изменить
        },
        animalDataFile = animalDataFile
    )
}

// Основная функция
fun main() {
    val config = readSimulationConfig()
    val (eatProbabilities, animalCharacteristics) = loadDataFromFile(config.animalDataFile)

    val island = Island(config.islandWidth, config.islandHeight)
    island.initialize(config, animalCharacteristics)

    // Запуск симуляции
    while (true) {
        island.update(eatProbabilities)
        printIsland(island.getGridState())
        println(island.getStatistics()) // Вывод статистики
        Thread.sleep(config.simulationSpeed)

        // Условие остановки (например, если все животные вымерли)
        if (island.grid.all { row -> row.all { it.animals.isEmpty() } }) {
            println("Все животные вымерли. Симуляция завершена.")
            exitProcess(0)
        }
    }
}
