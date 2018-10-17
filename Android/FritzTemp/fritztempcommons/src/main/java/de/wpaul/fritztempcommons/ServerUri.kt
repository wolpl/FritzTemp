package de.wpaul.fritztempcommons

class ServerUri(uri: String) {
    val minimized: String

    init {
        var min = uri
        if (min.startsWith("https://")) min = min.drop(8)
        if (min.startsWith("http://")) min = min.drop(7)
        min = min.split(":")[0]
        minimized = min
    }

    val full: String
        get() = "http://$minimized:8080"

}