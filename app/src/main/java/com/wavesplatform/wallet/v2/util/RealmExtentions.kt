package com.wavesplatform.wallet.v2.util

import com.vicpin.krealmextensions.*
import com.wavesplatform.wallet.v2.data.database.DBHelper
import io.realm.RealmModel
import io.realm.RealmObject

inline fun <reified T : RealmModel> queryAllUserData(): List<T> {
    DBHelper.getInstance().realmUserData.use { realm ->
        val result = realm.where(T::class.java).findAll()
        return realm.copyFromRealm(result)
    }
}

fun <T : RealmModel> T.saveUserData() {
    DBHelper.getInstance().realmUserData.transaction { realm ->
        if (isAutoIncrementPK()) {
            initPk(realm)
        }
        if (this.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(this) else realm.copyToRealm(this)
    }
}

inline fun <reified D : RealmModel, T : Collection<D>> T.saveAllUserData() {
    if (size > 0) {
        DBHelper.getInstance().realmUserData.transaction { realm ->
            if (first().isAutoIncrementPK()) {
                initPk(realm)
            }
            forEach { if (it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
        }
    }
}

inline fun <reified T : RealmModel> queryFirstUserData(query: Query<T>): T? {
    DBHelper.getInstance().realmUserData.use { realm ->
        val item: T? = realm.where(T::class.java).runQuery(query).findFirst()
        return if (item != null && RealmObject.isValid(item)) realm.copyFromRealm(item) else null
    }
}

inline fun <reified T : RealmModel> deleteUserData(crossinline query: Query<T>) {
    DBHelper.getInstance().realmUserData.transaction {
        it.where(T::class.java).runQuery(query).findAll().deleteAllFromRealm()
    }
}

inline fun <reified T : RealmModel> deleteAllUserData() {
    DBHelper.getInstance().realmUserData.transaction { it.where(T::class.java).findAll().deleteAllFromRealm() }
}