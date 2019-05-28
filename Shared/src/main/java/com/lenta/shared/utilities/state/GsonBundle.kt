package com.lenta.shared.utilities.state

import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lenta.shared.utilities.state.GsonBundleDelegate.Companion.PREFIX_NAME
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


interface GsonBundle {
    val bundle: Bundle
    val gson: Gson
    fun restoreInstanceStateGsonBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return
        bundle.putAll(savedInstanceState)
        bundle.keySet().filter { !it.startsWith(PREFIX_NAME) }.forEach { bundle.remove(it) }
    }

    fun saveInstanceStateGsonBundle(outState: Bundle?) {
        outState ?: return
        outState.putAll(bundle)
    }
}


class GsonBundleDelegate : GsonBundle {
    override val bundle: Bundle = Bundle()
    override val gson = gsonInstance

    companion object {
        const val PREFIX_NAME = "jsbndle:"
        val gsonInstance: Gson = GsonBuilder().create()
    }
}

inline fun <reified T> state(initial: T): ReadWriteProperty<GsonBundle, T> = State(initial, T::class.java)

class State<T>(private val initial: T, private val clazz: Class<T>) : ReadWriteProperty<GsonBundle, T> {

    override fun getValue(thisRef: GsonBundle, property: KProperty<*>): T {
        val key = PREFIX_NAME + property.name
        return if (!thisRef.bundle.containsKey(key)) {
            initial
        } else {
            thisRef.gson.fromJson(thisRef.bundle.getString(key), clazz)
        }
    }

    override fun setValue(thisRef: GsonBundle, property: KProperty<*>, value: T) {
        val key = PREFIX_NAME + property.name
        thisRef.bundle.putString(key, thisRef.gson.toJson(value))
    }
}