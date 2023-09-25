package arrow.optics

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

fun <T, A> SharedFlow<T>.apply(g: Getter<T, A>): SharedFlow<A> = object : SharedFlow<A> {
    override suspend fun collect(collector: FlowCollector<A>): Nothing =
        this@apply.collect { collector.emit(g.get(it)) }

    override val replayCache: List<A>
        get() = this@apply.replayCache.map { g.get(it) }
}

fun <T, A> StateFlow<T>.apply(g: Getter<T, A>): StateFlow<A> = object : StateFlow<A> {
    override val value: A
        get() = g.get(this@apply.value)

    override suspend fun collect(collector: FlowCollector<A>): Nothing =
        this@apply.collect { collector.emit(g.get(it)) }

    override val replayCache: List<A>
        get() = this@apply.replayCache.map { g.get(it) }
}

fun <T, A> MutableStateFlow<T>.apply(lens: Lens<T, A>): MutableStateFlow<A> = object : MutableStateFlow<A> {
    override var value: A
        get() = lens.get(this@apply.value)
        set(newValue) {
            this@apply.value = lens.set(this@apply.value, newValue)
        }

    override suspend fun collect(collector: FlowCollector<A>): Nothing =
        this@apply.collect { collector.emit(lens.get(it)) }

    override fun compareAndSet(expect: A, update: A): Boolean {
        val expectT = lens.set(this@apply.value, expect)
        val updateT = lens.set(this@apply.value, update)
        return compareAndSet(expectT, updateT)
    }

    override fun tryEmit(value: A): Boolean =
        this@apply.tryEmit(lens.set(this@apply.value, value))

    override suspend fun emit(value: A): Unit =
        this@apply.emit(lens.set(this@apply.value, value))

    override val subscriptionCount: StateFlow<Int>
        get() = this@apply.subscriptionCount

    override val replayCache: List<A>
        get() = this@apply.replayCache.map { lens.get(it) }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        this@apply.resetReplayCache()
    }
}
