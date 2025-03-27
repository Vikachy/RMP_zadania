package model.animals

import model.Island
import model.environment.Location
import java.util.concurrent.ThreadLocalRandom

sealed class Predator(
    name: String,
    x: Int,
    y: Int,
    symbol: String,
    characteristics: AnimalCharacteristics
) : Animal(name, x, y, symbol, characteristics) {

    class Wolf(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Predator("Волк", x, y, "🐺", characteristics)

    class Boa(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Predator("Удав", x, y, "🐍", characteristics)

    class Fox(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Predator("Лиса", x, y, "🦊", characteristics)

    class Bear(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Predator("Медведь", x, y, "🐻", characteristics)

    class Eagle(x: Int, y: Int, characteristics: AnimalCharacteristics) :
        Predator("Орел", x, y, "🦅", characteristics)

    override fun move(island: Island) {
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val (dx, dy) = directions.random()
        val speed = characteristics.speed.toInt()

        for (i in 1..speed) {
            val newX = x + dx
            val newY = y + dy
            if (newX in island.grid.indices && newY in island.grid[0].indices) {
                x = newX
                y = newY
            } else {
                break
            }
        }
    }

    override fun reproduce(): Animal? {
        val reproduceProbability = when (this) {
            is Wolf -> 30
            is Boa -> 20
            is Fox -> 40
            is Bear -> 10
            is Eagle -> 25
        }
        return if (ThreadLocalRandom.current().nextInt(100) < reproduceProbability) {
            when (this) {
                is Wolf -> Wolf(x, y, characteristics)
                is Boa -> Boa(x, y, characteristics)
                is Fox -> Fox(x, y, characteristics)
                is Bear -> Bear(x, y, characteristics)
                is Eagle -> Eagle(x, y, characteristics)
            }
        } else null
    }

    override fun die(): Boolean {
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
        val prey = location.animals.filter { it !== this }.toMutableList()
        if (prey.isNotEmpty()) {
            val eatProbabilityMap = eatProbabilities[name] ?: return
            val animalsToRemove = mutableListOf<Animal>()
            for (victim in prey) {
                val eatProbability = eatProbabilityMap[victim.name] ?: 0
                if (ThreadLocalRandom.current().nextInt(100) < eatProbability) {
                    animalsToRemove.add(victim)
                    break
                }
            }
            location.animals.removeAll(animalsToRemove)
        }
    }
}
