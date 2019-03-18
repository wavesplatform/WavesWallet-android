package com.wavesplatform.wallet.v2.data.model.local

import java.util.regex.Pattern

data class Complexity(var score: Int, var complexityType: ComplexityType) {

    fun isTooSimple(): Boolean {
        return complexityType.complexity <= Complexity.ComplexityType.GOOD.complexity
    }

    enum class ComplexityType(var complexity: Int, var msg: String) {
        TOO_SHORT(0, "Too Short"),
        BAD(1, "Week; Use letters & numbers"),
        MEDIUM(2, "Medium; Use special characters"),
        GOOD(3, "Good"),
        STRONG(4, "Strong")
    }

    companion object {
        fun complexity(password: String, minLen: Int = 25): Complexity {
            var score = 0

            if (password.isEmpty() || password.length < minLen) {
                return Complexity(0, ComplexityType.TOO_SHORT)
            }

            score += password.length

//            score += (checkRepetition(1, password).length - password.length);
//            score += (checkRepetition(2, password).length - password.length);
//            score += (checkRepetition(3, password).length - password.length);
//            score += (checkRepetition(4, password).length - password.length);

            //password has 3 numbers
            if (Pattern.compile(".*[0-9].*[0-9].*[0-9]").matcher(password).find()) score += 5

            //password has 2 symbols
            if (Pattern.compile(".*[!,@#$%^&*?_~].*[!,@#$%^&*?_~]").matcher(password).find()) score += 5

            //password has Upper and Lower chars
            if (Pattern.compile("([a-z].*[A-Z])|([A-Z].*[a-z])").matcher(password).find()) score += 10

            //password has number and chars
            if (Pattern.compile("[a-zA-Z]").matcher(password).find() && Pattern.compile("[0-9]").matcher(password).find()) score += 15
            //
            //password has number and symbol
            if (Pattern.compile("[!,@#$%^&*?_~]").matcher(password).find() && Pattern.compile("[0-9]").matcher(password).find()) score += 15

            //password has char and symbol
            if (Pattern.compile("[!,@#$%^&*?_~]").matcher(password).find() && Pattern.compile("[a-zA-Z]").matcher(password).find()) score += 15

            //password is just a numbers or chars
            if (Pattern.compile("[^\\w+$/]").matcher(password).matches() || Pattern.compile("[^\\d+$/]").matcher(password).matches()) score -= 10

            return when {
                score < 25 ->
                    Complexity(score, ComplexityType.BAD)
                score < 50 ->
                    Complexity(score, ComplexityType.MEDIUM)
                score < 75 ->
                    Complexity(score, ComplexityType.GOOD)
                else ->
                    Complexity(score, ComplexityType.STRONG)
            }
        }
//
//        private fun checkRepetition(pLen: Int, str: String): String {
//            var res = ""
//            var repeated: Boolean
//            str.forEachIndexed { i, c ->
//                var i = i
//                repeated = true
//                str.forEachIndexed { j, c ->
//                    if (j < pLen && (j + i + pLen) < str.length) {
//                        return@forEachIndexed
//                    } else {
//                        if (j < pLen) {
//                            repeated = false
//                        }
//                        if (repeated) {
//                            i += pLen - 1
//                            repeated = false
//                        } else {
//                            res += str[i]
//                        }
//                    }
//                }
//            }
//            return res
//        }


//        fun checkRepetition(pLen: Int, str: String): String {
//            var res = "";
//            var repeated = false;
//            var j = 0;
//            var i = 0;
//
//            while (i < str.length) {
//                repeated = true
//                while (j < pLen && (j + i + pLen) < str.length) {
//                    repeated = repeated && (str[j + i] == str[j + i + pLen])
//                    j++
//                }
//                if (j < pLen) {
//                    repeated = false
//                }
//                if (repeated) {
//                    i += pLen - 1;
//                    repeated = false
//                } else {
//                    res += str[i]
//                }
//                i++
//            }
//            return res
//        }
    }
}