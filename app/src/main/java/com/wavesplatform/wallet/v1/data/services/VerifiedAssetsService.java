package com.wavesplatform.wallet.v1.data.services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wavesplatform.wallet.v1.data.auth.IncorrectPinException;

import java.util.Map;

import io.reactivex.Observable;

public class VerifiedAssetsService {

    private DatabaseReference verifiedAssetsReference;

    public VerifiedAssetsService() {
        verifiedAssetsReference = FirebaseDatabase.getInstance().getReference().child("verified-assets");
    }

    public Observable<Map<String,String>> getAllVerifiedAssets() {
        return Observable.create(emitter -> {
            verifiedAssetsReference.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snap) {
                            if (snap != null && snap.getValue() != null) {
                                emitter.onNext((Map<String, String>) snap.getValue());
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
        });
//                .map(stringObjectAMap -> ((Map<String, String>) stringObjectAMap).entrySet())
//                .flatMapIterable(entries -> entries)
//                .map(e -> new VerifiedAsset(e.getKey(), e.getValue()))
//                .toList().toObservable();
    }

}