class Island(val width: Int, val height: Int) {
    val grid = Array(width) { Array(height) { Location() } }

    fun initialize(config: SimulationConfig, animalCharacteristics: Map<String, AnimalCharacteristics>) {
        // Инициализация животных
        config.initialAnimals.forEach { (name, count) ->
            repeat(count) {
                val x = (0 until width).random()
                val y = (0 until height).random()
                val animal = createAnimal(name, x, y, animalCharacteristics[name]!!)
                grid[x][y].animals.add(animal)
            }
        }

        // Инициализация растений
        for (x in 0 until width) {
            for (y in 0 until height) {
                if ((0..100).random() < config.plantGrowthProbability) {
                    grid[x][y].plants.add(Plant(x, y))
                }
            }
        }
    }

    private fun createAnimal(
        name: String,
        x: Int,
        y: Int,
        characteristics: AnimalCharacteristics
    ): Animal = when (name) {
        "Волк" -> Predator.Wolf(x, y, characteristics)
        "Удав" -> Predator.Boa(x, y, characteristics)
        "Лиса" -> Predator.Fox(x, y, characteristics)
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
        else -> error("Неизвестный тип животного: $name")
    }

    fun update(eatProbabilities: Map<String, Map<String, Int>>) {
        val newAnimals = mutableListOf<Animal>()

        for (x in 0 until width) {
            for (y in 0 until height) {
                val location = grid[x][y]
                val deadAnimals = mutableListOf<Animal>()

                // Процесс питания
                location.animals.toList().forEach { it.eat(location, eatProbabilities) }

                // Процесс движения
                location.animals.toList().forEach { it.move(this) }

                // Процессы размножения и смерти
                location.animals.toList().forEach { animal ->
                    animal.reproduce()?.let { newAnimals.add(it) }
                    if (animal.die()) deadAnimals.add(animal)
                }

                location.animals.removeAll(deadAnimals)
            }
        }

        // Добавление новых животных
        newAnimals.forEach { animal ->
            if (animal.x in grid.indices && animal.y in grid[0].indices) {
                grid[animal.x][animal.y].animals.add(animal)
            }
        }

        // Рост новых растений
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (grid[x][y].plants.isEmpty() && (0..100).random() < 10) {
                    grid[x][y].plants.add(Plant(x, y))
                }
            }
        }
    }

    fun getGridState(): Array<Array<String>> = Array(width) { x ->
        Array(height) { y ->
            when {
                grid[x][y].animals.any { it is Predator } -> grid[x][y].animals.first { it is Predator }.symbol
                grid[x][y].animals.any { it is Herbivore } -> grid[x][y].animals.first { it is Herbivore }.symbol
                grid[x][y].plants.isNotEmpty() -> "🌿"
                else -> "⬜"
            }
        }
    }

    fun getStatistics(): String {
        var predators = 0
        var herbivores = 0
        var plants = 0

        for (x in 0 until width) {
            for (y in 0 until height) {
                predators += grid[x][y].animals.count { it is Predator }
                herbivores += grid[x][y].animals.count { it is Herbivore }
                plants += grid[x][y].plants.size
            }
        }

        return "Хищники: $predators, Травоядные: $herbivores, Растения: $plants"
    }
}