package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.injection.scope.PerFragment
import com.wavesplatform.wallet.v2.ui.home.dex.DexFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryFragment
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment
import com.wavesplatform.wallet.v2.ui.home.profile.ProfileFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.WalletFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.LeasingFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun walletFragment(): WalletFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun dexFragment(): DexFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun historyFragment(): HistoryFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun profileFragment(): ProfileFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun assetsFragment(): AssetsFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun leasingFragment(): LeasingFragment


    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun historyDateItemFragment(): HistoryDateItemFragment
}
