package model.animals

import model.Island
import model.environment.Location

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

    fun isWithinBounds(island: Array<Array<Location>>): Boolean {
        return x in island.indices && y in island[0].indices
    }
}

data class AnimalCharacteristics(
    val weight: Double,
    val maxCount: Int,
    val speed: Double,
    val foodNeeded: Double
)
