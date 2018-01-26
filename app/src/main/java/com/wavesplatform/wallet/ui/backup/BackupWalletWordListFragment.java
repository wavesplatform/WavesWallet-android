package com.wavesplatform.wallet.ui.backup;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.data.access.AccessState;
import com.wavesplatform.wallet.databinding.FragmentBackupWordListBinding;
import com.wavesplatform.wallet.ui.auth.PinEntryActivity;
import com.wavesplatform.wallet.util.annotations.Thunk;

import static android.app.Activity.RESULT_OK;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.KEY_VALIDATED_PASSWORD;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.KEY_VALIDATING_PIN_FOR_RESULT;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.REQUEST_CODE_VALIDATE_PIN;

public class BackupWalletWordListFragment extends Fragment {

    @Thunk FragmentBackupWordListBinding binding;
    @Thunk Animation animEnterFromRight;
    @Thunk Animation animEnterFromLeft;
    private Animation animExitToLeft;
    private Animation animExitToRight;

    int currentWordIndex = 0;
    @Thunk String[] mnemonic;
    @Thunk String word;
    private String of;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_backup_word_list, container, false);

        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && supportActionBar != null) {
            supportActionBar.setElevation(0F);
        }

        word = getResources().getString(R.string.backup_word);
        of = getResources().getString(R.string.backup_of);

        animExitToLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.exit_to_left);
        animEnterFromRight = AnimationUtils.loadAnimation(getActivity(), R.anim.enter_from_right);

        animExitToRight = AnimationUtils.loadAnimation(getActivity(), R.anim.exit_to_right);
        animEnterFromLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.enter_from_left);

        loadMnemonic();

        setupNextWordAction();

        setupPreviousWordAction();

        return binding.getRoot();
    }

    private void startWords() {
        if (currentWordIndex == mnemonic.length) {
            currentWordIndex = 0;
        }
        binding.tvCurrentWord.setText(word + " " + (currentWordIndex + 1) + " " + of + " 15");
        binding.tvPressReveal.setText(mnemonic[currentWordIndex]);
        binding.nextWordAction.setEnabled(true);
    }


    private void setupNextWordAction() {
        binding.nextWordAction.setOnClickListener(v -> {

            if (currentWordIndex >= 0) {
                binding.previousWordAction.setVisibility(View.VISIBLE);
            } else {
                binding.previousWordAction.setVisibility(View.GONE);
            }

            if (currentWordIndex < mnemonic.length - 1) {

                animExitToLeft.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        binding.tvPressReveal.setText("");
                        binding.tvCurrentWord.setText(word + " " + (currentWordIndex + 1) + " " + of + " 15");
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // No-op
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        binding.cardLayout.startAnimation(animEnterFromRight);
                        binding.tvPressReveal.setText(mnemonic[currentWordIndex]);
                    }
                });

                binding.cardLayout.startAnimation(animExitToLeft);

            }

            currentWordIndex++;

            if (currentWordIndex == mnemonic.length) {

                currentWordIndex = 0;

                Fragment fragment = BackupWalletVerifyFragment.createFragment(mnemonic);

                getFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.content_frame, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {

                if (currentWordIndex == mnemonic.length - 1) {
                    binding.nextWordAction.setText(getResources().getString(R.string.backup_done));
                } else {
                    binding.nextWordAction.setText(getResources().getString(R.string.backup_next_word));
                }
            }
        });
    }

    public void onBackPressed() {
        mnemonic = null;
    }

    private void setupPreviousWordAction() {
        binding.previousWordAction.setOnClickListener(v1 -> {

            binding.nextWordAction.setText(getResources().getString(R.string.backup_next_word));

            if (currentWordIndex == 1) {
                binding.previousWordAction.setVisibility(View.GONE);
            }

            animExitToRight.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    binding.tvPressReveal.setText("");
                    binding.tvCurrentWord.setText(word + " " + (currentWordIndex + 1) + " " + of + " 15");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // No-op
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    binding.cardLayout.startAnimation(animEnterFromLeft);
                    binding.tvPressReveal.setText(mnemonic[currentWordIndex]);
                }
            });

            binding.cardLayout.startAnimation(animExitToRight);

            currentWordIndex--;
        });
    }

    public void loadMnemonic() {
        String seed = AccessState.getInstance().getSeedStr();
        if (seed != null) {
            mnemonic = seed.split("\\s");
            startWords();
        } else {
            requestPinDialog();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_VALIDATE_PIN && resultCode == RESULT_OK && data != null
                && data.getStringExtra(KEY_VALIDATED_PASSWORD) != null) {
            String seed = AccessState.getInstance().getSeedStr();
            if (seed != null) {
                mnemonic = seed.split("\\s");
                startWords();
            }
        }
    }


    private void requestPinDialog() {
        Intent intent = new Intent(getActivity(), PinEntryActivity.class);
        intent.putExtra(KEY_VALIDATING_PIN_FOR_RESULT, true);
        startActivityForResult(intent, REQUEST_CODE_VALIDATE_PIN);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}