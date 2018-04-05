package com.wavesplatform.wallet.v1.crypto;

import org.whispersystems.curve25519.OpportunisticCurve25519Provider;

import java.lang.reflect.Constructor;

import static com.wavesplatform.wallet.v1.request.TransferTransactionRequest.SignatureLength;

public class CryptoProvider {

    private static OpportunisticCurve25519Provider provider;
    public static OpportunisticCurve25519Provider get() {
        if (provider == null) {
            Constructor<OpportunisticCurve25519Provider> constructor = null;
            try {
                constructor =OpportunisticCurve25519Provider.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                provider = constructor.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Couldn't create crypto provider", e);
            }

        }
        return provider;
    }

    public static byte[] sign(byte[] key, byte[] msg) {
        return get().calculateSignature(CryptoProvider.get().getRandom(SignatureLength), key, msg);
    }
}
