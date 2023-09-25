package arrow.fx.coroutines

import androidx.lifecycle.ViewModel
import arrow.core.identity
import arrow.fx.coroutines.continuations.AcquireStep
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext

fun <A> ViewModel.resourceScope(
    block: ResourceScope.() -> A
): A = block(ViewModelResourceScope(this))

class ViewModelResourceScope(
    private val model: ViewModel
): ResourceScope {
    override suspend fun <A> install(acquire: suspend AcquireStep.() -> A, release: suspend (A, ExitCase) -> Unit): A {
        val context = coroutineContext
        return bracketCase({
            val resource = acquire(AcquireStep)
            model.addCloseable { runBlocking(context) { release(resource, ExitCase.Completed) } }
            resource
        }, ::identity, { resource, ex ->
            // Only if ExitCase.Failure, or ExitCase.Cancelled during acquire we cancel
            // Otherwise we've saved the finalizer, and it will be called from somewhere else.
            if (ex != ExitCase.Completed) {
                runBlocking(context) { release(resource, ex) }
            }
        })
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <A> Resource<A>.bind(): A = when (this) {
        is Resource.Allocate<A> -> install({ acquire() }, release)
        is Resource.Bind<*, A> -> {
            val inner = source.bind()
            val g = f as (Any?) -> Resource<A>
            g(inner).bind()
        }
        is Resource.Defer<A> -> resource().bind()
        is Resource.Dsl -> dsl(this@ViewModelResourceScope)
    }
}
