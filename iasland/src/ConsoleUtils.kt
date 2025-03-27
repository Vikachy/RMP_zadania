fun printIsland(grid: Array<Array<String>>) {
    val border = "═".repeat(grid[0].size * 2)
    println("╔$border╗")
    grid.forEach { row ->
        print("║")
        row.forEach { cell -> print(cell) }
        println("║")
    }
    println("╚$border╝")
}