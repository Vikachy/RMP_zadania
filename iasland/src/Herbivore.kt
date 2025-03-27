import java.util.concurrent.ThreadLocalRandom

sealed class Herbivore(
    name: String,
    x: Int,
    y: Int,
    symbol: String,
    characteristics: AnimalCharacteristics
) : Animal(name, x, y, symbol, characteristics) {

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
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val (dx, dy) = directions.random()
        repeat(characteristics.speed.toInt()) {
            val newX = x + dx
            val newY = y + dy
            if (newX in island.grid.indices && newY in island.grid[0].indices) {
                x = newX
                y = newY
            }
        }
    }

    override fun reproduce(): Animal? {
        val probability = when (this) {
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
        return if (ThreadLocalRandom.current().nextInt(100) < probability) {
            when (this) {
                is Horse -> Horse(x, y, characteristics)
                is Deer -> Deer(x, y, characteristics)
                is Rabbit -> Rabbit(x, y, characteristics)
                is Mouse -> Mouse(x, y, characteristics)
                is Goat -> Goat(x, y, characteristics)
                is Sheep -> Sheep(x, y, characteristics)
                is Boar -> Boar(x, y, characteristics)
                is Buffalo -> Buffalo(x, y, characteristics)
                is Duck -> Duck(x, y, characteristics)
                is Caterpillar -> Caterpillar(x, y, characteristics)
            }
        } else null
    }

    override fun die(): Boolean {
        val probability = when (this) {
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
        return ThreadLocalRandom.current().nextInt(100) < probability
    }

    override fun eat(location: Location, eatProbabilities: Map<String, Map<String, Int>>) {
        if (this is Duck) {
            location.animals
                .filterIsInstance<Caterpillar>()
                .firstOrNull {
                    (eatProbabilities[name]?.get(it.name) ?: 0) > ThreadLocalRandom.current().nextInt(100)
                }
                ?.let { location.animals.remove(it) }
        }

        if (location.plants.isNotEmpty()) {
            location.plants.removeAt(0)
        }
    }
}