package com.wavesplatform.wallet.v1.data.services;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wavesplatform.wallet.v1.data.auth.IncorrectPinException;

import io.reactivex.Observable;

public class PinStoreService {

    private DatabaseReference pincodesReference;

    public PinStoreService() {
        pincodesReference = FirebaseDatabase.getInstance().getReference().child("pincodes");
    }

    public Observable<Boolean> savePasswordByKey(String key, String password, String pin) {
        return Observable.create(emitter ->
                pincodesReference.child(key).removeValue().addOnCompleteListener(task ->
                        pincodesReference.child(key).child(pin).setValue(password, (err, ref) -> {
                            if (err == null) {
                                emitter.onNext(true);
                                emitter.onComplete();
                            } else {
                                emitter.onError(err.toException());
                            }
                        })));
    }

    public Observable<String> readPassword(int nTry, String guid, String pin) {
        Log.e("CCCCOKOKOKOKCC", "err.toException()");
        return Observable.create(emitter -> {
            pincodesReference.child(guid).child("try").child("try" + (nTry + 1)).setValue(pin, (err, ref) -> {
                if (err == null) {
                    pincodesReference.child(guid).child(pin).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snap) {
                            if (snap != null && snap.getValue() != null) {
                                emitter.onNext(snap.getValue().toString());
                                emitter.onComplete();
                            } else {
                                emitter.onError(new IncorrectPinException());
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e("AAAAAA", error.getMessage());
                            emitter.onError(error.toException());
                        }
                    });
                } else {
                    if (err.getCode() == DatabaseError.PERMISSION_DENIED) {
                        emitter.onError(new IncorrectPinException());
                        Log.e("BBBBBB", "Denied");
                    } else {
                        Log.e("CCCCCC", "1", err.toException());
                        emitter.onError(err.toException());
                    }
                }
            }) ;
        });//.compose(RxUtil.applySchedulersToObservable());
    }

}