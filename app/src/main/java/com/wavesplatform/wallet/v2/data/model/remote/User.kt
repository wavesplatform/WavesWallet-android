package com.wavesplatform.wallet.v2.data.model.remote

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass


@RealmClass
open class User constructor(
    @PrimaryKey
    var id: Long? = 1,
    var name: String? = "test",
    var email: String? = "test@gmail.com"
): RealmModel
