package com.wavesplatform.wallet.v2.data.model.local

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
            if (password.matches(Regex("/(.*[0-9].*[0-9].*[0-9])/"))) score += 5

            //password has 2 symbols
            if (password.matches(Regex("/(.*[!,@#$%^&*?_~].*[!,@#$%^&*?_~])/"))) score += 5

            //password has Upper and Lower chars
            if (password.matches(Regex("/([a-z].*[A-Z])|([A-Z].*[a-z])/"))) score += 10

            //password has number and chars
            if (password.matches(Regex("/([a-zA-Z])/")) && password.matches(Regex("/([0-9])/"))) score += 15
            //
            //password has number and symbol
            if (password.matches(Regex("/([!,@#$%^&*?_~])/")) && password.matches(Regex("/([0-9])/"))) score += 15

            //password has char and symbol
            if (password.matches(Regex("/([!,@#$%^&*?_~])/")) && password.matches(Regex("/([a-zA-Z])/"))) score += 15

            //password is just a numbers or chars
            if (password.matches(Regex("/[^\\w+$/]")) || password.matches(Regex("/[^\\d+$/]"))) score -= 10

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
    }
}