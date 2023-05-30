package com.remi.pdfscanner.ui.test

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivityTestBinding
import com.remi.pdfscanner.util.Common
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class TestActivity : BaseActivity<ActivityTestBinding>(ActivityTestBinding::inflate) {
    val TAG = "~~~"
    override fun setSize() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.ratingbar.apply {
            numberOfStars = 5
            rating = 0f
            stepSize = 0.1f
            starSize = 11.667f * Common.screenWidth / 100f
            starCornerRadius = .5f * Common.screenWidth / 100f
            fillColor = Color.parseColor("#FFB90B")
            pressedStarBackgroundColor = Color.parseColor("#D7D7D7")
            pressedFillColor = Color.parseColor("#FFB90B")
            starBackgroundColor = Color.parseColor("#D7D7D7")

        }

    }

    class ListNode(var hehe: Int) {
        var next: ListNode? = null
    }
}

fun main() {
//    printArray1(55)
//    printArray2(100)
//    printArray3(100)

//    spiralOrder(arrayOf(intArrayOf(1, 2, 3, 4), intArrayOf(5, 6, 7, 8), intArrayOf(9, 10, 11, 12), intArrayOf(1, 2, 3, 4), intArrayOf(5, 6, 7, 8), intArrayOf(9, 10, 11, 12)))
    spiralOrder(arrayOf(intArrayOf(1, 2, 3, 4), intArrayOf(5, 6, 7, 8), intArrayOf(9, 10, 11, 12)))
//    spiralOrder(arrayOf(intArrayOf(1, 2, 3), intArrayOf(4,5,6), intArrayOf(7,8,9)))
}

fun spiralOrder(matrix: Array<IntArray>): List<Int> {
    val column = matrix[0].size
    val row = matrix.size
    var currentPos = 0
    val listResults = MutableList(matrix.size * matrix[0].size) { 0 }
    val list :MutableList<Int> = kotlin.collections.ArrayList()
    println("[1 ,2 ,3 ,4 ,8 ,12 ,4 ,8 ,12 ,11 ,10 ,9 ,5 ,1 ,9 ,5 ,6 ,7 ,11 ,3 ,7 ,6 ,1 ,10]")
    for (i in 0 until column / 2) {
        if (i*2+1>=row) break
        for (a in i until column - i) {
            listResults[currentPos] = matrix[i][a]
            currentPos++
//            list.add(matrix[i][a])
        }

            for (a in i + 1 until row - i) {
            listResults[currentPos] = matrix[a][column - i - 1]
            currentPos++
//                list.add(matrix[a][column - i - 1])
            }
            for (a in column-i-2 downTo i){
            listResults[currentPos] = matrix[row - i - 1][a]
            currentPos++
//                list.add(matrix[row - i - 1][a])
            }

            for (a in row-1 -i-1 downTo i+1){
            listResults[currentPos] = matrix[a][i]
            currentPos++
//                list.add(matrix[a][i])
            }

        for (i in row/2..(column-row/2-1)){
            listResults[currentPos] = matrix[row/2][i]
            currentPos++
        }


    }


    println(listResults)
    return listResults
}

fun printArray1(input: Int) {
    var count = 0
    var start = 0
    for (i in 0..input / 2) {
        start += i
        if (start > input)
            break
        count++
    }
    var currentRow = 1
    var rowCount = 0
    for (i in 1..input) {
        print("$i ")
        rowCount++
        if (rowCount == currentRow) {
            println()
            rowCount = 0
            currentRow++
        }
    }
}

fun printArray2(input: Int) {
    var resultPair = Pair(1, 1)
    for (i in 3..input / 2 step 2) {
        var count = resultPair.second + resultPair.first * 2 + 2
        if (count > input)
            break
        resultPair = Pair(i, count)
    }
    var currentRow = 0
    var rowCount = 0
    val list: MutableList<Int> = ArrayList()
    list.add(resultPair.first)
    for (i in resultPair.first - 2 downTo 1 step 2) {
        list.add(i)
        list.add(0, i)
    }
    println(list)

    for (j in 1..(resultPair.first / 2))
        print("    ")
    for (i in 1..input) {
        print(" ${handleText(i)} ")
        rowCount++

        if (rowCount >= list[currentRow]) {
            println()
            rowCount = 0
            currentRow++
            if (currentRow >= list.size)
                break
            if (currentRow < resultPair.first / 2) {
                for (j in 1..(resultPair.first / 2 - currentRow))
                    print("    ")
            } else if (currentRow > resultPair.first / 2) {
                for (j in 1..(currentRow - resultPair.first / 2))
                    print("    ")
            }


        }
    }

}

fun handleText(inp: Int): String {
    if (inp < 10) return "$inp "
    return inp.toString()
}

fun printArray3(input: Int) {
    var resultPair = Pair(1, 1)
    for (i in 2..input / 2 step 2) {
        var count = resultPair.second + resultPair.first * 2 + 2
        if (count > input)
            break
        resultPair = Pair(i, count)
    }
    var currentRow = 0
    var rowCount = 0
    val list: MutableList<Int> = ArrayList()
    list.add(resultPair.first)
    for (i in resultPair.first - 2 downTo 1 step 2) {
        list.add(i)
        list.add(0, i)
    }
    println(list)

    print("row 0: ")
    for (i in 1..input) {
        print(" ${handleText(i)} ")
        rowCount++

        if (rowCount == list[currentRow] / 2) {
            for (j in 0..(resultPair.first - rowCount * 2))
                print("    ")
        }
//        if (currentRow > resultPair.first / 2  && rowCount==currentRow+1) {
//            for (j in 0..(currentRow - resultPair.first / 2))
//                print("    ")
//        }

        if (rowCount >= list[currentRow]) {
            println()
            print("row ${currentRow + 1}: ")
            rowCount = 0
            currentRow++
            if (currentRow >= list.size)
                break
        }
    }
}