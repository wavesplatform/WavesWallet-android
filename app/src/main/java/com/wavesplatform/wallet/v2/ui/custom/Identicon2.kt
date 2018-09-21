package com.wavesplatform.wallet.v2.ui.custom

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import com.wavesplatform.wallet.v2.ui.custom.Identicon2.Options.Palette.Colors

class Identicon2 {


    data class Options(
            val grid: Size,
            val palette: Palette,
            val mainRange: Range,
            val hollowRange: Range,
            val backgroundRange: Range) {

        sealed class Palette {
            object RandomColor : Palette()
            data class Colors(
                    val background: Color,
                    val main: Color,
                    val hollow: Color) : Palette()
        }

        data class Range(
                val step: Float,
                val length: Float)

        data class Size(
                val cells: Int,
                val rows: Int)
    }

    companion object {
        var defaultOptions: Options = {
            val options = Options(
                    grid = Options.Size(cells = 8, rows = 8),
                    palette = Options.Palette.RandomColor,
                    mainRange = Options.Range(step = 4F, length = 1.5F),
                    hollowRange = Options.Range(step = 5F, length = 3F),
                    backgroundRange = Options.Range(step = 3F, length = 4F))
            options
        }()
    }

    class Identity(private val options: Options) {


        fun createImage(hash: String, sideLength: Int): Bitmap {
            val grid = options.grid
            val colors = colors(options = options, hash = hash)
            val matrix = matrix(hash = hash, grid = grid)
            val w: Float = sideLength / grid.cells.toFloat()
            val h: Float = sideLength / grid.rows.toFloat()

            val bitmap = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.RGB_565)
            for (row in 0 until grid.rows) {
                for (cell in 0 until grid.cells) {
                    val color = color(Point(cell, row), matrix = matrix, colors = colors)
                    val x = cell.toFloat() * w - 0.5
                    val y = row.toFloat() * h - 0.5



                    for (startXPixel in x.toInt()..(x + h).toInt())
                        for (startYPixel in y.toInt()..(y + h).toInt())
                            bitmap.setPixel(startXPixel, startYPixel, color.toArgb())
                }
            }

            return bitmap
        }


        fun matrix(hash: String, grid: Options.Size): Array<Array<Int?>> {
            var i = 0
            val hashMatrix = Array(grid.rows) { arrayOfNulls<Int?>(grid.cells) }
            var newHash = hash
            for (x in 0 until grid.cells) {
                for (y in 0 until grid.rows) {
                    if (symbol(newHash, i) == null) {
                        newHash += hash
                    }
                    val newSymbol = symbol(newHash, i)
                    if (newSymbol != null) {
                        hashMatrix[x][y] = newSymbol.toIntOrNull(30)
                    } else {
                        hashMatrix[x][y] = null
                    }
                    i += 1
                }
            }
            return hashMatrix
        }

        private fun colorRange(hash: String, range: Options.Range): Color {
            val symbols: List<String> = hash.map { it.toString() }
            val colors = arrayOfNulls<Float>(3)
            val length = range.length
            val step = range.step
            for (i in 0 until 3) {
                val begin = Math.round(-(7F / length) - length * 3 - step * i)
                val end = hash.length - (step * i).toInt()
                val symbolsForColor = slice(symbols, begin, end)
                colors[i] = colorValue(symbolsForColor, length).toFloat()
            }
            return Color.valueOf(
                    colors[0]!! / 255,
                    colors[1]!! / 255,
                    colors[2]!! / 255)
        }

        private fun slice(symbols: List<String>, beginIndex: Int, endIndex: Int) : List<String> {
            var trueBeginIndex = if (beginIndex < 0) {
                symbols.size + beginIndex
            } else {
                beginIndex
            }
            var trueEndIndex = if (endIndex < 0) {
                symbols.size + endIndex
            } else {
                endIndex
            }
            trueBeginIndex = Math.min(symbols.size, trueBeginIndex)
            trueBeginIndex = Math.max(trueBeginIndex, 0)
            trueEndIndex = Math.min(symbols.size, trueEndIndex)
            trueEndIndex = Math.max(trueEndIndex, 0)

            return symbols.subList(trueBeginIndex, trueEndIndex)
        }

        private fun colorValue(symbolsList: List<String>, length: Float): Int {
            var newSymbols = symbolsList
            val color: ArrayList<List<String>> = arrayListOf()

            while (newSymbols.isNotEmpty()) {
                val normalizedLength = Math.min(newSymbols.size, length.toInt())
                val symbols = newSymbols.subList(0, normalizedLength)
                color.add(symbols)
                newSymbols = newSymbols.subList(normalizedLength, newSymbols.size)
            }

            var reduce = 0
            for (symbols in color) {
                val symbol = symbols.firstOrNull() ?: ""
                val value = Math.min(reduce + symbol.toInt(36), 255)
                reduce = Math.max(value, 0)
            }

            return reduce
        }

        private fun colors(options: Options, hash: String): Colors {
            lateinit var backgroundColor: Color
            lateinit var mainColor: Color
            lateinit var hollowColor: Color

            when (options.palette) {
                Options.Palette.RandomColor -> {
                    backgroundColor = colorRange(hash = hash, range = options.backgroundRange)
                    mainColor = colorRange(hash = hash, range = options.mainRange)
                    hollowColor = colorRange(hash = hash, range = options.hollowRange)
                }
                is Colors -> {
                    backgroundColor = options.palette.background
                    mainColor = options.palette.main
                    hollowColor = options.palette.hollow
                }
            }
            return Colors(background = backgroundColor, main = mainColor, hollow = hollowColor)
        }

        private fun color(point: Point, matrix: Array<Array<Int?>>, colors: Colors): Color {
            if (point.x < (matrix.size / 2.0)) {
                var cellIndex = Math.max(0, matrix.size - 1)
                cellIndex = Math.max(Math.min(cellIndex, (point.x)), 0)
                var rowIndex = Math.max(0, matrix[cellIndex].size - 1)
                rowIndex = Math.max(Math.min(rowIndex, (point.y)), 0)
                val value = matrix[cellIndex][rowIndex]
                if (value == null) {
                    return colors.hollow
                } else {
                    if (value % 2 != 0) {
                        return colors.main
                    } else {
                        return colors.background
                    }
                }
            } else {
                return color(
                        point = Point((matrix.size - 1 - point.x), point.y),
                        matrix = matrix,
                        colors = colors)
            }
        }

        fun symbol(string: String?, index: Int): String? {
            if (string == null || string.isEmpty() || string.length <= index) {
                return null
            }
            return string[index].toString()
        }
    }
}