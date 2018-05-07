package com.wavesplatform.wallet.v2.ui.tutorial

import android.animation.*
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.util.getViewScaleAnimator
import com.wavesplatform.wallet.v2.util.launchActivity
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.SwipeDirection
import kotlinx.android.synthetic.main.activity_tutorial.*
import kotlinx.android.synthetic.main.item_tutorial_1_card.view.*
import pers.victor.ext.dp2px
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject


class TutorialActivity : BaseActivity(), TutorialView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TutorialPresenter
    @Inject
    lateinit var adapter: TutorialAdapter

    @ProvidePresenter
    fun providePresenter(): TutorialPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_tutorial

    override fun onViewReady(savedInstanceState: Bundle?) {
        adapter.addAll(arrayListOf(1, 2, 3, 4, 5, 6, 7))
        card_stack_tutorial.setAdapter(adapter)
        card_stack_tutorial.setCardEventListener(object : CardStackView.CardEventListener {
            override fun onCardDragging(percentX: Float, percentY: Float) {
            }

            override fun onCardSwiped(direction: SwipeDirection?) {
                if (adapter.count == card_stack_tutorial.topIndex) {
                    launchActivity<WelcomeActivity>(withoutAnimation = true)
                }
            }

            override fun onCardReversed() {
            }

            override fun onCardMovedToOrigin() {
            }

            override fun onCardClicked(index: Int) {
            }

        })

        white_block.post({
            card_stack_tutorial.topView.post({
                val animator = getViewScaleAnimator(card_stack_tutorial.topView, white_block, dp2px(10))
                animator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        white_block.animate().alpha(0f).setDuration(350).start()
                        card_stack_tutorial.topView.button_continue.isEnabled = true
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }

                })
                animator.duration = 400
                animator.start()
            })
        })

        adapter.listener = object : TutorialAdapter.OnNextButtonClicked {
            override fun onButtonClicked(button: Button) {
                // fix library bug with fast swipe
                button.isEnabled = false
                swipeLeft()
            }
        }
    }

    private fun swipeLeft() {
        val target = card_stack_tutorial.topView

        val rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", -10f))
        val translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, -2000f))
        val translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, 100f))
        rotation.duration = 500
        translateX.duration = 500
        translateY.duration = 500
        val cardAnimationSet = AnimatorSet()
        cardAnimationSet.playTogether(rotation, translateX, translateY)

        card_stack_tutorial.swipe(SwipeDirection.Left, cardAnimationSet)

        // fix library bug with fast swipe
        runDelayed(600, {
            card_stack_tutorial.topView.button_continue.isEnabled = true
        })
    }

}
