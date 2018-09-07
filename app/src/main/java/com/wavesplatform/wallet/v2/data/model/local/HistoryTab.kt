package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HistoryTab(var data: String, var title: String) : Parcelable