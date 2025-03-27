package model.environment

import model.animals.Animal

class Location {
    val plants = mutableListOf<Plant>()
    val animals = mutableListOf<Animal>()
}
