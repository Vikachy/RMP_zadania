package model

import config.SimulationConfig
import model.animals.*
import model.environment.Location
import model.environment.Plant
import java.util.concurrent.ThreadLocalRandom

class Island(val width: Int, val height: Int) {
    val grid = Array(width) { Array(height) { Location() } }

    fun initialize(config: SimulationConfig, animalCharacteristics: Map<String, AnimalCharacteristics>) {
        config.initialAnimals.forEach { (animalName, count) ->
            repeat(count) {
                val x = ThreadLocalRandom.current().nextInt(width)
                val y = ThreadLocalRandom.current().nextInt(height)
                val characteristics = animalCharacteristics[animalName]
                    ?: error("Нет характеристик для животного $animalName")
                val animal = createAnimal(animalName, x, y, characteristics)
                addAnimal(animal)
            }
        }

        for (i in 0 until width) {
            for (j in 0 until height) {
                if (ThreadLocalRandom.current().nextInt(100) < config.plantGrowthProbability) {
                    grid[i][j].plants.add(Plant(i, j))
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

    fun addAnimal(animal: Animal) {
        if (animal.isWithinBounds(grid)) {
            grid[animal.x][animal.y].animals.add(animal)
        }
    }

    fun update(eatProbabilities: Map<String, Map<String, Int>>) {
        val newAnimals = mutableListOf<Animal>()

        for (i in 0 until width) {
            for (j in 0 until height) {
                val location = grid[i][j]
                val animalsToRemove = mutableListOf<Animal>()

                for (animal in location.animals) {
                    animal.eat(location, eatProbabilities)
                }

                for (animal in location.animals) {
                    animal.move(this)
                }

                for (animal in location.animals) {
                    if (!animal.isWithinBounds(grid)) {
                        animalsToRemove.add(animal)
                    } else {
                        animal.reproduce()?.let { newAnimals.add(it) }
                        if (animal.die()) animalsToRemove.add(animal)
                    }
                }

                location.animals.removeAll(animalsToRemove)
            }
        }

        newAnimals.forEach { addAnimal(it) }
    }

    fun getGridState(): Array<Array<String>> {
        return Array(width) { i ->
            Array(height) { j ->
                when {
                    grid[i][j].animals.any { it is Predator } ->
                        grid[i][j].animals.first { it is Predator }.symbol
                    grid[i][j].animals.any { it is Herbivore } ->
                        grid[i][j].animals.first { it is Herbivore }.symbol
                    grid[i][j].plants.isNotEmpty() -> "🌿"
                    else -> "⬜️"
                }
            }
        }
    }

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
