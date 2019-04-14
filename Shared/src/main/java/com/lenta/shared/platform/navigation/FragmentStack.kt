package com.lenta.shared.platform.navigation

import android.os.Bundle
import android.util.Log


import java.util.HashSet
import java.util.Random
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentStack(private val manager: FragmentManager, private val containerId: Int) {
    private val random: Random = Random(System.currentTimeMillis())
    private var listener: FragmentManager.OnBackStackChangedListener? = null


    fun setOnBackStackChangedListener(listener: FragmentManager.OnBackStackChangedListener) {
        if (this.listener != null) manager.removeOnBackStackChangedListener(this.listener!!)
        this.listener = listener
        manager.addOnBackStackChangedListener(listener)
    }

    /**
     * Pushes a fragment to the top of the stack.
     */
    fun push(fragment: Fragment) {
        val transaction = manager.beginTransaction()
        /*transaction.setCustomAnimations(R.anim.card_slide_left_in,
                R.anim.card_slide_left_out,
                R.anim.card_slide_right_in,
                R.anim.card_slide_right_out);*/
        val tag = setTag(fragment)
        if (peek() != null)
            fragment.arguments!!.putString("_before", peek()!!.arguments!!.getString("_tag"))
        transaction.replace(containerId, fragment, tag)
        transaction.addToBackStack(null)
        transaction.commit()
        executePendingTransactions()
    }


    /**
     * Pushes a fragment to the top of the stack.
     */
    fun pushWithoutAnimation(fragment: Fragment) {
        val transaction = manager.beginTransaction()
        val tag = setTag(fragment)
        if (peek() != null)
            fragment.arguments!!.putString("_before", peek()!!.arguments!!.getString("_tag"))
        transaction.replace(containerId, fragment, tag)
        transaction.addToBackStack(null)
        transaction.commit()
        executePendingTransactions()
    }

    /**
     * Replaces entire stack contents with just one fragment.
     */
    fun replace(fragment: Fragment) {
        popAll()
        manager.beginTransaction()
                .replace(containerId, fragment, setTag(fragment))
                .commit()
        executePendingTransactions()
        if (listener != null) listener!!.onBackStackChanged()
    }

    /**
     * Pops the topmost fragment from the stack.
     */
    fun pop(): Boolean {
        if (manager.backStackEntryCount == 0)
            return false
        manager.popBackStack()
        return true
    }

    /**
     * Pops the topmost fragment from the stack.
     */
    fun pop(fragmentName: String): Boolean {
        if (manager.backStackEntryCount == 0)
            return false
        manager.popBackStack(fragmentName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        return true
    }


    fun popReturnArgs(args: Bundle) {
        val tag = peek()!!.arguments!!.getString("_before", null)
                ?: throw RuntimeException("Can not find link to previous fragment")
        val pre = manager.findFragmentByTag(tag)
                ?: throw RuntimeException("Previous fragment not found")
        val arguments = pre.arguments ?: throw RuntimeException("Can not set arguments")
        removeSystemTags(args)
        arguments.putBundle(SAVE_TAG_FOR_ARGUMENTS, args)
        manager.popBackStack()
    }

    fun popAll() {
        for (i in manager.backStackEntryCount downTo 1) manager.popBackStackImmediate()
        if (peek() != null) manager.beginTransaction().remove(peek()!!).commit()
        executePendingTransactions()
        if (listener != null) listener!!.onBackStackChanged()
    }

    /**
     * Returns the topmost fragment in the stack.
     */
    fun peek(): Fragment? {
        return manager.findFragmentById(containerId)
    }

    private fun setTag(fragment: Fragment): String {
        val tag = random.nextLong().toString() + "" + System.nanoTime()
        if (fragment.arguments == null) {
            fragment.arguments = Bundle()
        }
        fragment.arguments!!.putString("_tag", tag)
        return tag
    }

    private fun executePendingTransactions() {
        try {
            manager.executePendingTransactions()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "executePendingTransactions ", e)
        }

    }

    companion object {
        private val TAG = FragmentStack::class.java.simpleName

        val SAVE_TAG_FOR_ARGUMENTS = "_SAVE_TAG_FOR_ARGUMENTS"

        fun removeSystemTags(bundle: Bundle?) {
            if (bundle == null) return
            val keys = HashSet(bundle.keySet())
            for (key in keys) if (key.startsWith("_")) bundle.remove(key)
        }
    }
}