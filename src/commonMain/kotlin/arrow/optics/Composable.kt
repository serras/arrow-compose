package arrow.optics

import androidx.compose.runtime.Composable

@Composable
fun <T, A> T.on(o: Optional<T, A>, block: @Composable (A) -> Unit): T {
    o.getOrNull(this)?.let(block)
    return this
}

@Composable
fun <T, A> T.forEach(o: Fold<T, A>, block: @Composable (A) -> Unit): Unit =
    o.getAll(this).forEach(block)
