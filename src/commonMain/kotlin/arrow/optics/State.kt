package arrow.optics

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State

fun <T, A> State<T>.apply(g: Getter<T, A>): State<A> = object : State<A> {
    override val value: A
        get() = g.get(this@apply.value)
}

fun <T, A> MutableState<T>.apply(lens: Lens<T, A>): MutableState<A> = object : MutableState<A> {
    override var value: A
        get() = lens.get(this@apply.value)
        set(newValue) {
            this@apply.value = lens.set(this@apply.value, newValue)
        }

    override fun component1(): A = value
    override fun component2(): (A) -> Unit = { value = it }
}
