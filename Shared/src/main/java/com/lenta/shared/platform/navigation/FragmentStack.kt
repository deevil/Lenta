package com.lenta.shared.platform.navigation


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.lenta.shared.R
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf
import java.util.*
import javax.inject.Inject

class FragmentStack(
        private val manager: FragmentManager,
        private val containerId: Int
) {
    private val random: Random = Random(System.currentTimeMillis())
    private var listener: FragmentManager.OnBackStackChangedListener? = null

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    var defaultAnimation: CustomAnimation = CustomAnimation.horizontal


    fun setOnBackStackChangedListener(listener: FragmentManager.OnBackStackChangedListener) {
        this.listener?.let(manager::removeOnBackStackChangedListener)
        this.listener = listener
        manager.addOnBackStackChangedListener(listener)
    }

    /**
     * Pushes a fragment to the top of the stack.
     */
    fun push(fragment: Fragment, customAnimation: CustomAnimation? = null, disableAnimations: Boolean = false) {
        analyticsHelper.onNewScreen(fragment.implementationOf(CoreFragment::class.java))
        val transaction = manager.beginTransaction()
        if (!disableAnimations) {
            val animation = customAnimation ?: defaultAnimation
            transaction.setCustomAnimations(
                    animation.enter,
                    animation.exit,
                    animation.popEnter,
                    animation.popExit)
        }

        val tag = setTag(fragment)
        peek()?.let { currentFragment ->
            currentFragment.arguments?.getString(KEY_FRAGMENT_TAG)?.let { keyTag ->
                fragment.arguments?.putString(KEY_FRAGMENT_BEFORE, keyTag)
            }
        }

        transaction.replace(containerId, fragment, tag)
        transaction.addToBackStack(fragment::class.simpleName)
        transaction.commit()
        executePendingTransactions()
    }


    /**
     * Replaces entire stack contents with just one fragment.
     */
    fun replace(fragment: Fragment) {
        analyticsHelper.onNewScreen(fragment.implementationOf(CoreFragment::class.java))
        popAll()
        val transaction = manager.beginTransaction()
        transaction.replace(containerId, fragment, setTag(fragment))
        transaction.addToBackStack(fragment::class.simpleName)
        transaction.commit()
        executePendingTransactions()
        listener?.onBackStackChanged()
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
     * @param fragmentName Fragment::class.simpleName
     */
    fun pop(fragmentName: String?): Boolean {
        if (fragmentName == null || manager.backStackEntryCount == 0)
            return false
        manager.popBackStack(fragmentName, 0)
        return true
    }


    fun popReturnArgs(args: Bundle) {
        val tag = peek()?.arguments?.getString(KEY_FRAGMENT_BEFORE, null)
                ?: throw RuntimeException("Can not find link to previous fragment")
        val pre = manager.findFragmentByTag(tag)
                ?: throw RuntimeException("Previous fragment not found")
        val arguments = pre.arguments ?: throw RuntimeException("Can not set arguments")
        removeSystemTags(args)
        arguments.putBundle(SAVE_TAG_FOR_ARGUMENTS, args)
        manager.popBackStack()
    }

    fun popAll() {
        for (i in 0..manager.backStackEntryCount) {
            manager.popBackStack()
        }
        executePendingTransactions()
        listener?.onBackStackChanged()
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
        fragment.arguments?.putString(KEY_FRAGMENT_TAG, tag)
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
        private const val TAG = "FragmentStack"
        private const val KEY_FRAGMENT_TAG = "_tag"
        private const val KEY_FRAGMENT_BEFORE = "_before"
        private const val KEY_PREFIX = "_"

        const val SAVE_TAG_FOR_ARGUMENTS = "_SAVE_TAG_FOR_ARGUMENTS"

        fun removeSystemTags(bundle: Bundle?) {
            if (bundle == null) return
            val keys = HashSet(bundle.keySet())
            for (key in keys) if (key.startsWith(KEY_PREFIX)) bundle.remove(key)
        }
    }
}

data class CustomAnimation(
        val enter: Int,
        val exit: Int,
        val popEnter: Int,
        val popExit: Int
) {
    companion object {
        val horizontal: CustomAnimation by lazy {
            CustomAnimation(
                    R.anim.card_slide_left_in,
                    R.anim.card_slide_left_out,
                    R.anim.card_slide_right_in,
                    R.anim.card_slide_right_out)
        }

        val vertical: CustomAnimation by lazy {
            CustomAnimation(
                    R.anim.card_slide_top_in,
                    R.anim.card_slide_top_out,
                    R.anim.card_slide_bottom_in,
                    R.anim.card_slide_bottom_out)
        }
    }
}