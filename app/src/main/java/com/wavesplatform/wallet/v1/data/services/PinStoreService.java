package com.wavesplatform.wallet.v1.data.services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wavesplatform.wallet.v1.data.auth.IncorrectPinException;

import io.reactivex.Observable;

public class PinStoreService {

    private static final String TRY = "try";
    private static final String PINCODES = "pincodes";
    private DatabaseReference pincodesReference;

    public PinStoreService() {
        pincodesReference = FirebaseDatabase.getInstance().getReference().child(PINCODES);
    }

    public Observable<Boolean> writePassword(String guid, String passCode, String keyPassword) {
        return Observable.create(emitter ->
                pincodesReference
                        .child(guid)
                        .removeValue()
                        .addOnCompleteListener(task ->
                                pincodesReference
                                        .child(guid)
                                        .child(passCode)
                                        .setValue(keyPassword, (err, ref) -> {
                                            if (err == null) {
                                                emitter.onNext(true);
                                                emitter.onComplete();
                                            } else {
                                                emitter.onError(err.toException());
                                            }
                                        })));
    }

    public Observable<String> readPassword(String guid, String passCode, int tryCount) {
        return Observable.create(emitter ->
                pincodesReference
                        .child(guid)
                        .child(TRY)
                        .child(TRY + (tryCount + 1))
                        .setValue(passCode, (err, ref) -> {
                            if (err == null) {
                                pincodesReference
                                        .child(guid)
                                        .child(passCode)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                emitter.onError(error.toException());
                                            }
                                        });
                            } else {
                                if (err.getCode() == DatabaseError.PERMISSION_DENIED) {
                                    emitter.onError(new IncorrectPinException());
                                } else {
                                    emitter.onError(err.toException());
                                }
                            }
                        }));
    }
}