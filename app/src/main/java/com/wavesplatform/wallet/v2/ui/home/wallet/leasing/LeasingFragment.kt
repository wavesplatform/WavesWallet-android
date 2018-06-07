package com.wavesplatform.wallet.v2.ui.home.wallet.leasing

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.adapter.TestObject
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.adapter.AdapterActiveLeasing
import kotlinx.android.synthetic.main.fragment_leasing.*
import pers.victor.ext.click
import java.util.*
import javax.inject.Inject

class LeasingFragment : BaseFragment(), LeasingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: LeasingPresenter

    @ProvidePresenter
    fun providePresenter(): LeasingPresenter = presenter

    @Inject
    lateinit var adapter: AdapterActiveLeasing

    companion object {

        /**
         * @return LeasingFragment instance
         * */
        fun newInstance(): LeasingFragment {
            return LeasingFragment()
        }
    }

    override fun configLayoutRes(): Int = R.layout.fragment_leasing

    override fun onViewReady(savedInstanceState: Bundle?) {

        container_quick_note.click {
            if (expandable_layout.isExpanded) {
                expandable_layout.collapse()
                image_arrowup.animate()
                        .rotation(180f)
                        .setDuration(500)
                        .start()
            } else {
                expandable_layout.expand()
                image_arrowup.animate()
                        .rotation(0f)
                        .setDuration(500)
                        .start()
            }
        }

        text_active_leasing.click {
            if (expandable_layout_active_leasing.isExpanded) {
                expandable_layout_active_leasing.collapse()
                image_active_leasing.animate()
                        .rotation(180f)
                        .setDuration(500)
                        .start()
            } else {
                expandable_layout_active_leasing.expand()
                image_active_leasing.animate()
                        .rotation(0f)
                        .setDuration(500)
                        .start()
            }
        }

        recycle_active_leasing.layoutManager = LinearLayoutManager(baseActivity)
        recycle_active_leasing.adapter = adapter
        recycle_active_leasing.isNestedScrollingEnabled = false


        adapter.setNewData(arrayListOf(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))

        text_active_leasing.text = getString(R.string.active_now, adapter.data.size.toString())

        view_line_1.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        view_line_2.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        view_line_3.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        view_line_4.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        view_line_5.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }
}
