package com.wavesplatform.wallet.v1.ui.transactions;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.wavesplatform.wallet.R;

public class SubmitTransactionsUtils {
    public static void playAudio(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null && audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            MediaPlayer mp;
            mp = MediaPlayer.create(context, R.raw.beep);
            mp.setOnCompletionListener(mp1 -> {
                mp1.reset();
                mp1.release();
            });
            mp.start();
        }
    }

}
