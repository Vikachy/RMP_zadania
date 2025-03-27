package utils

fun printIsland(grid: Array<Array<String>>) {
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            print(grid[i][j])
        }
        println()
    }
    println()
}
