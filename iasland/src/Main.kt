fun main() {
    val config = readSimulationConfig()
    val (eatProbabilities, animalCharacteristics) = loadDataFromFile(config.animalDataFile)

    val island = Island(config.islandWidth, config.islandHeight)
    island.initialize(config, animalCharacteristics)

    while (true) {
        island.update(eatProbabilities)
        printIsland(island.getGridState())
        println(island.getStatistics())
        Thread.sleep(config.simulationSpeed)

        if (island.grid.all { row -> row.all { it.animals.isEmpty() } }) {
            println("Все животные вымерли. Симуляция завершена.")
            exitProcess(0)
        }
    }
}