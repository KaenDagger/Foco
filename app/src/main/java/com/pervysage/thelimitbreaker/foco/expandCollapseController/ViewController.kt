package com.pervysage.thelimitbreaker.foco.expandCollapseController

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView

abstract class ViewController<N, R, P>(private val listView: MyListView) {

    private val TAG = "ViewController"


    private var mRemoveObserver = 0

    private var translateCoords = IntArray(2)

    private lateinit var onExpand: () -> Unit

    private lateinit var onCollapse: () -> Unit

    fun setOnExpandListener(l: () -> Unit) {
        onExpand = l
    }

    fun setOnCollapseListener(l: () -> Unit) {
        onCollapse = l
    }

    private fun getAnimation(view: View, translateTop: Float, translateBottom: Float): Animator {

        val top = view.top
        val bottom = view.bottom
        val endTop = (top + translateTop).toInt()
        val endBottom = (bottom + translateBottom).toInt()

        val translationTop = PropertyValuesHolder.ofInt("top", top, endTop)
        val translationBottom = PropertyValuesHolder.ofInt("bottom", bottom, endBottom)

        val anim = ObjectAnimator.ofPropertyValuesHolder(view, translationTop, translationBottom)
        anim.duration = 250
        return anim
    }

    private fun getExpandedTopBottom(top: Int, bottom: Int, yDelta: Int): IntArray {
        var yTranslateTop = 0
        var yTranslateBottom = yDelta

        val height = bottom - top

        val isOverTop = top < 0
        val isBelowBottom = top + height + yDelta > (listView.height-200)
        if (isOverTop) {
            yTranslateTop = top
            yTranslateBottom = yDelta - yTranslateTop
        } else if (isBelowBottom) {
            val deltaBelow = top + height + yDelta - (listView.height-200)
            yTranslateTop = if (top - deltaBelow < 0) top else deltaBelow
            yTranslateBottom = yDelta - yTranslateTop
        }

        return intArrayOf(yTranslateTop, yTranslateBottom)
    }

    abstract fun bindExtraData(headHolder: N, bodyHolder: R, modelObj: P)

    abstract fun workInExpand(headHolder: N, bodyHolder: R, modelObj: P)

    abstract fun workInCollapse(headHolder: N, bodyHolder: R, modelObj: P)

    open fun setUp(headHolder: N, bodyHolder: R, modelObj: P, pos: Int = 0) {
        if (modelObj is ExpandableObj) {
            if (modelObj.isExpanded)
                workInExpand(headHolder, bodyHolder, modelObj)
            else
                workInCollapse(headHolder, bodyHolder, modelObj)
        } else
            throw Exception("Model Object must extend Expandable Object")
    }

    fun expand(viewToExpand: View, headHolder: N, bodyHolder: R, modelObj: P, viewToCollapse: View? = null, collapseObj: P? = null) {

        listView.isScrollEnabled=false

        var oldTop = 0
        var oldBottom = 0
        val oldCoordinates = HashMap<View, IntArray>()

        val prepareExpand = {
            mRemoveObserver = 1
            oldTop = viewToExpand.top
            oldBottom = viewToExpand.bottom
            val childCount = listView.childCount
            for (i in 0 until childCount) {
                val v = listView.getChildAt(i)
                Log.d(TAG,"before expand ${v.findViewById<TextView>(com.pervysage.thelimitbreaker.foco.R.id.tvPlaceTitle).text}")

                oldCoordinates[v] = intArrayOf(v.top, v.bottom)
            }
            bindExtraData(headHolder, bodyHolder, modelObj)
            workInExpand(headHolder, bodyHolder, modelObj)
        }

        if (collapseObj != null) (collapseObj as ExpandableObj).isExpanded = false

        if (viewToCollapse != null) {
            val collapseHead = viewToCollapse.getTag(com.pervysage.thelimitbreaker.foco.R.id.HEAD_KEY) as N
            val collapseBody = viewToCollapse.getTag(com.pervysage.thelimitbreaker.foco.R.id.BODY_KEY) as R
            workInCollapse(collapseHead, collapseBody, collapseObj!!)
        } else {
            prepareExpand()
        }

        viewToExpand.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                return when (mRemoveObserver) {
                    0 -> {
                        prepareExpand()
                        listView.requestLayout()
                        false
                    }
                    1 -> {

                        mRemoveObserver = 2
                        val newTop = viewToExpand.top
                        val newBottom = viewToExpand.bottom
                        val oldHeight = oldBottom - oldTop
                        val newHeight = newBottom - newTop
                        val delta = newHeight - oldHeight

                        translateCoords = getExpandedTopBottom(oldTop, oldBottom, delta)
                        listView.requestLayout()
                        false
                    }
                    else -> {

                        mRemoveObserver = 0
                        viewToExpand.viewTreeObserver.removeOnPreDrawListener(this)

                        val animations = ArrayList<Animator>()

                        val yTranslateTop = translateCoords[0]
                        val yTranslateBottom = translateCoords[1]
                        val index = listView.indexOfChild(viewToExpand)
                        for (v in oldCoordinates.keys) {

                                val i = listView.indexOfChild(v)
                                val oldChildTopBottom = oldCoordinates[v]
                                v.top = oldChildTopBottom!![0]
                                v.bottom = oldChildTopBottom[1]
                                if (v != viewToExpand) {
                                    val delta =
                                            if (i > index){
                                                yTranslateBottom
                                            }
                                            else {
                                                -yTranslateTop
                                            }
                                    animations.add(getAnimation(v, delta * 1f, delta * 1f))
                                }

                        }

                        val selViewTop = viewToExpand.top
                        val selViewBottom = viewToExpand.bottom
                        val selViewEndTop = selViewTop + (-yTranslateTop)
                        val selViewEndBottom = selViewBottom + yTranslateBottom

                        val expandTopAnim = ObjectAnimator.ofInt(
                                viewToExpand,
                                "top",
                                selViewTop, selViewEndTop
                        )


                        expandTopAnim.duration = 250
                        val expandBottomAnim = ObjectAnimator.ofInt(
                                viewToExpand,
                                "bottom",
                                selViewBottom, selViewEndBottom
                        )
                        expandBottomAnim.duration = 250
                        animations.add(expandTopAnim)
                        animations.add(expandBottomAnim)
                        val animatorSet = AnimatorSet()
                        animatorSet.playTogether(animations)
                        animatorSet.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {// TODO Not Implemented
                            }

                            override fun onAnimationEnd(animation: Animator?) {


                                listView.isScrollEnabled=true
                                (modelObj as ExpandableObj).isExpanded = true


                            }

                            override fun onAnimationCancel(animation: Animator?) {// TODO Not Implemented
                            }

                            override fun onAnimationStart(animation: Animator?) {// TODO Not Implemented
                            }

                        })
                        animatorSet.start()
                        true
                    }
                }
            }

        })
    }

    fun collapse(viewToCollapse: View, headHolder: N, bodyHolder: R, modelObj: P) {

        listView.isScrollEnabled=false
        val oldTop = viewToCollapse.top
        val oldBottom = viewToCollapse.bottom



        val oldCoordinates = HashMap<View, IntArray>()
        for (i in 0 until listView.childCount) {
            val child = listView.getChildAt(i)
            val oldTopBottom = IntArray(2)
            oldTopBottom[0] = child!!.top
            oldTopBottom[1] = child.bottom
            oldCoordinates[child] = oldTopBottom
        }

        workInCollapse(headHolder, bodyHolder, modelObj)

        viewToCollapse.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (mRemoveObserver == 0) {
                    mRemoveObserver = 1

                    val newTop = viewToCollapse.top
                    val newBottom = viewToCollapse.bottom

                    val oldHeight = oldBottom - oldTop
                    val newHeight = newBottom - newTop
                    translateCoords[0] = newTop - oldTop
                    translateCoords[1] = newBottom - oldBottom
                    listView.requestLayout()
                    return false
                }
                mRemoveObserver = 0
                viewToCollapse.viewTreeObserver.removeOnPreDrawListener(this)

                val animations = ArrayList<Animator>()

                val yTranslateTop = translateCoords[0]
                val yTranslateBottom = translateCoords[1]

                val index = listView.indexOfChild(viewToCollapse)

                for (i in 0 until listView.childCount) {
                    val v = listView.getChildAt(i)

                    val oldTopBottom = oldCoordinates[v]
                    if (oldTopBottom != null) {
                        v.top = oldTopBottom[0]
                        v.bottom = oldTopBottom[1]
                    } else {
                        val delta = if (i > index) -yTranslateBottom else yTranslateTop

                        v.top = if (v.top < 0) v.top - delta else v.top + delta
                        v.bottom = if (v.top < 0) v.bottom - delta else v.bottom + delta
                    }
                    if (v != viewToCollapse) {

                        val diff = if (i > index) yTranslateBottom else yTranslateTop
                        animations.add(getAnimation(v, diff * 1f, diff * 1f))
                    }
                }
                val selViewTop = viewToCollapse.top
                val selViewBottom = viewToCollapse.bottom
                val selViewEndTop = selViewTop + yTranslateTop
                val selViewEndBottom = selViewBottom + yTranslateBottom

                val collapseTopAnim = ObjectAnimator.ofInt(
                        viewToCollapse,
                        "top",
                        selViewTop, selViewEndTop
                )


                collapseTopAnim.duration = 150
                val collapseBottomAnim = ObjectAnimator.ofInt(
                        viewToCollapse,
                        "bottom",
                        selViewBottom, selViewEndBottom
                )
                collapseBottomAnim.duration = 150
                animations.add(collapseTopAnim)
                animations.add(collapseBottomAnim)

                val animatorSet = AnimatorSet()
                animatorSet.playTogether(animations)
                animatorSet.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {// TODO Not Implemented
                    }

                    override fun onAnimationEnd(animation: Animator?) {

                        listView.isScrollEnabled=true
                        (modelObj as ExpandableObj).isExpanded = false
                        onCollapse()

                    }

                    override fun onAnimationCancel(animation: Animator?) {// TODO Not Implemented
                    }

                    override fun onAnimationStart(animation: Animator?) {// TODO Not Implemented
                    }

                })
                animatorSet.start()
                return true
            }

        })
    }
}