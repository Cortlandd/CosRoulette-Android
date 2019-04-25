package com.makeuproulette.android

import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.gtomato.android.ui.transformer.WheelViewTransformer
import com.gtomato.android.ui.widget.CarouselView


class CarouselFragment : Fragment() {

    var carousel: CarouselView? = null
    var cylinder: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var v = inflater.inflate(R.layout.fragment_carousel, container, false)

        carousel = v.findViewById(R.id.carousel)
        cylinder = v.findViewById(R.id.cylinder)

        carousel?.transformer = WheelViewTransformer()
        carousel?.adapter = CarouselAdapter()
        carousel?.isInfinite = true
        carousel?.gravity = Gravity.CENTER_HORIZONTAL
        //carousel?.isScrollingAlignToViews = false

        carousel?.setOnScrollListener(object: CarouselView.OnScrollListener() {

            var rotateAnimation = RotateAnimation(
                    0f,//float: Rotation offset to apply at the start of the animation.
                    360f,//float: Rotation offset to apply at the end of the animation.
                    Animation.RELATIVE_TO_SELF,//int: Specifies how pivotXValue should be interpreted
                    0.5f,//float: The X coordinate of the point about which the object is being rotated
                    Animation.RELATIVE_TO_SELF,//int: Specifies how pivotYValue should be interpreted
                    0.5f//float: The Y coordinate of the point about which the object is being rotated
            )

            override fun onScrollBegin(carouselView: CarouselView?) {
                super.onScrollBegin(carouselView)

                var scroll = carouselView?.currentOffset
                var rotPercentage = scroll!! / carouselView?.adapter!!.itemCount

                var r = cylinder?.rotation

//                rotateAnimation.duration = 300
//                rotateAnimation.repeatCount = INFINITE
//                rotateAnimation.interpolator = AccelerateDecelerateInterpolator()
//                cylinder!!.startAnimation(rotateAnimation)

                cylinder?.animate()!!.rotationBy(((2 * Math.PI -rotPercentage)).toFloat())

            }

            override fun onScrollEnd(carouselView: CarouselView?) {
                super.onScrollEnd(carouselView)
                //cylinder!!.clearAnimation()
            }

        })

        return v
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

}
