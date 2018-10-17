package de.wpaul.fritztempcommons

class Event<T> {
    private val listeners = mutableListOf<(T) -> Unit>()

    operator fun plusAssign(listener: (T) -> Unit) {
        listeners.add(listener)
    }

    operator fun minusAssign(listener: (T) -> Unit) {
        listeners.remove(listener)
    }

    operator fun invoke(data: T) {
        listeners.forEach { it(data) }
    }
}

operator fun Event<Unit>.invoke() {
    invoke(Unit)
}