package net.arwix.gastro.client.domain

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

class InnerFragmentStateViewModel : ViewModel() {

    @PublishedApi
    internal val map = mutableMapOf<Class<out Fragment>, InnerFragmentState>()

    inline fun <reified T : InnerFragmentState> setState(
        clazz: Class<out Fragment>,
        reduce: (state: T) -> T
    ) {
        val s = map[clazz]
        require(s is T)
        map[clazz] = reduce(s)
    }

    inline fun <reified T : InnerFragmentState> getState(clazz: Class<out Fragment>): T {
        val s = map[clazz]
        require(s is T)
        return s
    }

    abstract class InnerFragmentState


}