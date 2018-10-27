package de.wpaul.fritztempcommons

class ServerUri(base: String, private val suffix: String = "") {
    val minimizedBase: String

    init {
        var min = base
        if (min.startsWith("https://")) min = min.drop(8)
        if (min.startsWith("http://")) min = min.drop(7)
        min = min.split(":")[0]
        minimizedBase = min
    }

    val full: String
        get() = "http://$minimizedBase:8080$suffix"

}