package com.wavesplatform.wallet.v2.data.manager

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import io.reactivex.Observable

class PinStoreService {

    private val pincodesReference = FirebaseDatabase.getInstance().reference.child(PINCODES)

    fun writePassword(guid: String, passCode: String, keyPassword: String): Observable<Boolean> {
        return Observable.create { emitter ->
            pincodesReference
                    .child(guid)
                    .removeValue()
                    .addOnCompleteListener { task ->
                        pincodesReference
                                .child(guid)
                                .child(passCode)
                                .setValue(keyPassword) { err, ref ->
                                    if (err == null) {
                                        emitter.onNext(true)
                                        emitter.onComplete()
                                    } else {
                                        emitter.onError(err.toException())
                                    }
                                }
                    }
        }
    }

    fun readPassword(guid: String, passCode: String, tryCount: Int): Observable<String> {
        return Observable.create { emitter ->
            pincodesReference
                    .child(guid)
                    .child(TRY)
                    .child(TRY + (tryCount + 1))
                    .setValue(passCode) { err, ref ->
                        if (err == null) {
                            pincodesReference
                                    .child(guid)
                                    .child(passCode)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snap: DataSnapshot) {
                                            if (snap.value != null) {
                                                emitter.onNext(snap.value!!.toString())
                                                emitter.onComplete()
                                            } else {
                                                emitter.onError(IncorrectPinException())
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            emitter.onError(error.toException())
                                        }
                                    })
                        } else {
                            if (err.code == DatabaseError.PERMISSION_DENIED) {
                                emitter.onError(IncorrectPinException())
                            } else {
                                emitter.onError(err.toException())
                            }
                        }
                    }
        }
    }

    inner class IncorrectPinException : RuntimeException()

    companion object {

        private const val TRY = "try"
        private const val PINCODES = "pincodes"
    }
}