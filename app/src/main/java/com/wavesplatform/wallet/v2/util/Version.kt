package com.wavesplatform.wallet.v2.util

class Version(private val version: String?) : Comparable<Version> {

    fun get(): String {
        return this.version ?: ""
    }

    init {
        if (version == null)
            throw IllegalArgumentException("Version can not be null")
        if (!version.matches("[0-9]+(\\.[0-9]+)*".toRegex()))
            throw IllegalArgumentException("Invalid version format")
    }

    override fun compareTo(other: Version): Int {
        val thisParts = this.get().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val otherParts = other.get().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val length = Math.max(thisParts.size, otherParts.size)
        for (i in 0 until length) {
            val thisPart = if (i < thisParts.size) {
                Integer.parseInt(thisParts[i])
            } else {
                0
            }
            val thatPart = if (i < otherParts.size) {
                Integer.parseInt(otherParts[i])
            } else {
                0
            }
            if (thisPart < thatPart) {
                return -1
            }
            if (thisPart > thatPart) {
                return 1
            }
        }
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        return if (this.javaClass != other.javaClass) {
            false
        } else {
            this.compareTo((other as Version?)!!) == 0
        }
    }

    override fun hashCode(): Int {
        return version?.hashCode() ?: 0
    }

    companion object {
        fun needAppUpdate(appVer: String, serverVer: String): Boolean {
            return Version(appVer) < Version(serverVer)
        }
    }
}