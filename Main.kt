import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock

// Задача 1: Синхронизация счётчика
fun task1() {
    val lock = ReentrantLock()
    var counter = 0

    val threads = List(5) {
        Thread {
            repeat(1000) {
                lock.lock()
                try {
                    counter++
                } finally {
                    lock.unlock()
                }
            }
        }
    }
    threads.forEach { it.start() }
    threads.forEach { it.join() }
    println("Задача 1: Итоговое значение счётчика: $counter")
}

// Задача 2: Потокобезопасный список
fun task2() {
    val numbers = CopyOnWriteArrayList<Int>()

    val threads = List(10) {
        Thread {
            for (i in 1..100) {
                numbers.add(i)
            }
        }
    }
    threads.forEach { it.start() }
    threads.forEach { it.join() }
    println("Задача 2: Размер списка после добавления: ${numbers.size}")
}

// Задача 3: Пул потоков
fun task3() {
    val executor = Executors.newFixedThreadPool(4)

    for (i in 1..20) {
        executor.submit {
            println("Задача 3: Поток ${Thread.currentThread().name} выполняет задачу $i")
        }
    }
    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)
}

// Задача 4: Симуляция работы банка
fun task4() {
    val lock = ReentrantLock()
    val accounts = mutableMapOf(1 to 1000, 2 to 1000, 3 to 1000)

    fun transfer(from: Int, to: Int, amount: Int) {
        lock.lock()
        try {
            if (accounts[from]!! >= amount) {
                accounts[from] = accounts[from]!! - amount
                accounts[to] = accounts[to]!! + amount
                println("Задача 4: Перевод $amount с $from на $to выполнен.")
            } else {
                println("Задача 4: Недостаточно средств на счете $from.")
            }
        } finally {
            lock.unlock()
        }
    }

    val threads = List(10) {
        Thread {
            repeat(100) {
                transfer(1, 2, 10)
            }
        }
    }
    threads.forEach { it.start() }
    threads.forEach { it.join() }
    println("Задача 4: Итоговые балансы: $accounts")
}

// Задача 5: Барьер синхронизации
fun task5() {
    val barrier = CyclicBarrier(5) {
        println("Задача 5: Все потоки достигли барьера.")
    }

    val threads = List(5) {
        Thread {
            println("Задача 5: Поток ${Thread.currentThread().name} выполняет работу.")
            barrier.await()
            println("Задача 5: Поток ${Thread.currentThread().name} продолжает работу.")
        }
    }
    threads.forEach { it.start() }
    threads.forEach { it.join() }
}

// Задача 6: Ограниченный доступ к ресурсу
fun task6() {
    val semaphore = Semaphore(2)

    val threads = List(10) {
        Thread {
            semaphore.acquire()
            println("Задача 6: Поток ${Thread.currentThread().name} получил доступ к ресурсу.")
            Thread.sleep(1000)
            println("Задача 6: Поток ${Thread.currentThread().name} освобождает ресурс.")
            semaphore.release()
        }
    }
    threads.forEach { it.start() }
    threads.forEach { it.join() }
}

// Задача 7: Обработка результатов задач
fun task7() {
    val executor = Executors.newFixedThreadPool(10)
    val futures = mutableListOf<Future<Int>>()

    for (i in 1..10) {
        val future = executor.submit(Callable {
            val factorial = (1..i).reduce { acc, num -> acc * num }
            println("Задача 7: Поток ${Thread.currentThread().name} вычислил факториал $i = $factorial")
            factorial
        })
        futures.add(future)
    }

    futures.forEach { it.get() }
    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)
}

// Задача 8: Симуляция производственной линии
fun task8() {
    val queue = LinkedBlockingQueue<Int>()

    val producer = Thread {
        for (i in 1..10) {
            queue.put(i)
            println("Задача 8: Произведено: $i")
            Thread.sleep(100)
        }
    }

    val consumer = Thread {
        while (true) {
            val item = queue.take()
            println("Задача 8: Обработано: $item")
            if (item == 10) break
        }
    }

    producer.start()
    consumer.start()
    producer.join()
    consumer.join()
}

// Задача 9: Многопоточная сортировка
fun task9() {
    val array = (1..100).shuffled().toIntArray()
    val chunks = array.size / 4
    val sortedArray = array.copyOf()

    val threads = List(4) { index ->
        Thread {
            val start = index * chunks
            val end = if (index == 3) array.size else start + chunks
            sortedArray.sort(start, end)
            println("Задача 9: Поток ${Thread.currentThread().name} отсортировал часть массива.")
        }
    }
    threads.forEach { it.start() }
    threads.forEach { it.join() }

    sortedArray.sort() // Объединение отсортированных частей
    println("Задача 9: Итоговый массив: ${sortedArray.joinToString()}")
}

// Задача 10: Обед философов (с ограничением циклов)
fun task10() {
    val forks = List(5) { ReentrantLock() }
    val philosophers = List(5) { index ->
        Thread {
            val leftFork = forks[index]
            val rightFork = forks[(index + 1) % 5]
            var mealsEaten = 0 // Счётчик приёмов пищи

            repeat(3) { // Каждый философ ест 3 раза
                leftFork.lock()
                try {
                    if (rightFork.tryLock()) {
                        try {
                            println("Задача 10: Философ $index ест (${mealsEaten + 1} раз).")
                            Thread.sleep(100)
                            mealsEaten++
                        } finally {
                            rightFork.unlock()
                        }
                    }
                } finally {
                    leftFork.unlock()
                }
                Thread.sleep(100) // Философ думает
            }
            println("Задача 10: Философ $index закончил трапезу.")
        }
    }
    philosophers.forEach { it.start() }
    philosophers.forEach { it.join() }
}
// Задача 11: Умножение матриц
fun task11() {
    val matrixA = arrayOf(
        intArrayOf(1, 2, 3),
        intArrayOf(4, 5, 6),
        intArrayOf(7, 8, 9)
    )
    val matrixB = arrayOf(
        intArrayOf(9, 8, 7),
        intArrayOf(6, 5, 4),
        intArrayOf(3, 2, 1)
    )
    val result = Array(matrixA.size) { IntArray(matrixB[0].size) }

    val threads = List(matrixA.size) { row ->
        Thread {
            for (col in matrixB[0].indices) {
                for (k in matrixA[0].indices) {
                    result[row][col] += matrixA[row][k] * matrixB[k][col]
                }
            }
            println("Задача 11: Поток ${Thread.currentThread().name} вычислил строку $row.")
        }
    }
    threads.forEach { it.start() }
    threads.forEach { it.join() }
    println("Задача 11: Результат умножения матриц:")
    result.forEach { println(it.joinToString()) }
}

// Задача 12: Таймер с многопоточностью
fun task12() {
    var running = true

    val timerThread = Thread {
        var seconds = 0
        while (running) {
            println("Задача 12: Прошло $seconds секунд.")
            Thread.sleep(1000)
            seconds++
        }
    }

    val stopperThread = Thread {
        Thread.sleep(10000)
        running = false
        println("Задача 12: Таймер остановлен.")
    }

    timerThread.start()
    stopperThread.start()
    timerThread.join()
    stopperThread.join()
}

// Основная функция для запуска всех задач
fun main() {
    task1()
    task2()
    task3()
    task4()
    task5()
    task6()
    task7()
    task8()
    task9()
    task10()
    task11()
    task12()
}