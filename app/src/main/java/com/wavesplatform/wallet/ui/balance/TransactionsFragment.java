package com.wavesplatform.wallet.ui.balance;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.tooltip.Tooltip;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.data.datamanagers.AddressBookManager;
import com.wavesplatform.wallet.data.rxjava.RxUtil;
import com.wavesplatform.wallet.databinding.FragmentTransactionsBinding;
import com.wavesplatform.wallet.payload.ExchangeTransaction;
import com.wavesplatform.wallet.payload.IssueTransaction;
import com.wavesplatform.wallet.payload.PaymentTransaction;
import com.wavesplatform.wallet.payload.ReissueTransaction;
import com.wavesplatform.wallet.payload.Transaction;
import com.wavesplatform.wallet.payload.TransferTransaction;
import com.wavesplatform.wallet.ui.backup.BackupWalletActivity;
import com.wavesplatform.wallet.ui.home.MainActivity;
import com.wavesplatform.wallet.ui.home.SecurityPromptDialog;
import com.wavesplatform.wallet.ui.home.TransactionSelectedListener;
import com.wavesplatform.wallet.ui.transactions.ExchangeTransactionActivity;
import com.wavesplatform.wallet.ui.transactions.IssueDetailActivity;
import com.wavesplatform.wallet.ui.transactions.ReissueDetailActivity;
import com.wavesplatform.wallet.ui.transactions.TransactionDetailActivity;
import com.wavesplatform.wallet.ui.transactions.UnknownDetailActivity;
import com.wavesplatform.wallet.util.DateUtil;
import com.wavesplatform.wallet.util.ListUtil;
import com.wavesplatform.wallet.util.PrefsUtil;
import com.wavesplatform.wallet.util.ViewUtils;
import com.wavesplatform.wallet.util.annotations.Thunk;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment implements TransactionsViewModel.DataListener, TransactionSelectedListener {

    @Thunk static final String TAG = TransactionsFragment.class.getSimpleName();

    public static final String ACTION_INTENT = "com.wavesplatform.wallet.ui.TransactionsFragment.REFRESH";
    public static final String KEY_TRANSACTION_LIST_POSITION = "transaction_list_position";
    public int balanceBarHeight;
    private BalanceHeaderAdapter accountsAdapter;
    @Thunk Communicator comm;
    // Accounts list
    @Thunk AppCompatSpinner accountSpinner;
    // Tx list
    @Thunk
    TransactionsListAdapter transactionAdapter;
    private Activity context;
    private DateUtil dateUtil;

    @Thunk
    FragmentTransactionsBinding binding;
    @Thunk
    TransactionsViewModel viewModel;
    @Thunk AppBarLayout appBarLayout;

    private int detailTransactionPosition = -1;

    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(ACTION_INTENT) && getActivity() != null) {
                binding.swipeContainer.setRefreshing(true);
                viewModel.updateAccountList();
                viewModel.updateBalanceAndTransactionList(intent, accountSpinner.getSelectedItemPosition());
                transactionAdapter.onTransactionsUpdated(viewModel.getTransactionList());
                binding.swipeContainer.setRefreshing(false);
                binding.rvTransactions.getAdapter().notifyDataSetChanged();
                // Check backup status on receiving funds
                viewModel.onViewReady();
                binding.rvTransactions.scrollToPosition(0);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        dateUtil = new DateUtil(context);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_transactions, container, false);
        viewModel = new TransactionsViewModel(context, this);
        binding.setViewModel(viewModel);

        setHasOptionsMenu(true);

        balanceBarHeight = (int) getResources().getDimension(R.dimen.balance_bar_height);

        setupViews();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.onViewReady();
    }

    private void setAccountSpinner() {
        appBarLayout = (AppBarLayout) context.findViewById(R.id.appbar_layout);
        ((AppCompatActivity) context).setSupportActionBar((Toolbar) context.findViewById(R.id.toolbar_general));

        if (viewModel.getActiveAccountAndAddressList().size() > 1) {
            accountSpinner.setVisibility(View.VISIBLE);
        } else if (viewModel.getActiveAccountAndAddressList().size() > 0) {
            accountSpinner.setSelection(0);
            accountSpinner.setVisibility(View.GONE);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            viewModel.updateBalanceAndTransactionList(null, accountSpinner.getSelectedItemPosition());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        comm.resetNavigationDrawer();

        IntentFilter filter = new IntentFilter(ACTION_INTENT);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);

        if (detailTransactionPosition == -1) {
            viewModel.updateAccountList();
            viewModel.updateBalanceAndTransactionList(null, accountSpinner.getSelectedItemPosition());

            binding.rvTransactions.clearOnScrollListeners();
            binding.rvTransactions.addOnScrollListener(new CollapseActionbarScrollListener() {
                @Override
                public void onMoved(int distance) {
                    setToolbarOffset(distance);
                }
            });

            if (transactionAdapter != null) {
                transactionAdapter.notifyDataSetChanged();
            }

            if (accountsAdapter != null) {
                accountsAdapter.notifyDataSetChanged();
            }
        } else {
            detailTransactionPosition = -1;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    /**
     * Deprecated, but necessary to prevent casting issues on <API21
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        comm = (Communicator) activity;
    }

    @Override
    public void showBackupPromptDialog(boolean showNeverAgain) {
        SecurityPromptDialog securityPromptDialog = SecurityPromptDialog.newInstance(
                R.string.security_centre_backup_title,
                getString(R.string.security_centre_backup_message),
                R.drawable.bad_backup,
                R.string.security_centre_backup_positive_button,
                true,
                showNeverAgain
        );

        securityPromptDialog.setPositiveButtonListener(v -> {
            securityPromptDialog.dismiss();
            if (securityPromptDialog.isChecked()) {
                viewModel.neverPromptBackup();
            }
            Intent intent = new Intent(getActivity(), BackupWalletActivity.class);
            startActivity(intent);
        });

        securityPromptDialog.setNegativeButtonListener(v -> {
            securityPromptDialog.dismiss();
            if (securityPromptDialog.isChecked()) {
                viewModel.neverPromptBackup();
            }
        });

        if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
            securityPromptDialog.showDialog(getActivity().getSupportFragmentManager());
        }
    }

    @Override
    public void showSendStatistics() {
        SecurityPromptDialog securityPromptDialog = SecurityPromptDialog.newInstance(
                R.string.statistics_send_title,
                getString(R.string.statistics_send_message),
                R.drawable.bad_backup,
                R.string.yes,
                true,
                false);

        securityPromptDialog.setPositiveButtonListener(v -> {
            viewModel.allowSendStats(true);
            securityPromptDialog.dismiss();
        });

        securityPromptDialog.setNegativeButtonListener(v -> {
            viewModel.allowSendStats(false);
            securityPromptDialog.dismiss();
        });

        if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
            securityPromptDialog.showDialog(getActivity().getSupportFragmentManager());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void setTopBalance(Spannable spannable) {
        binding.balance.setText(spannable);
    }

    /**
     * Position is offset to account for first item being "All Wallets". If returned result is -1,
     * {@link com.wavesplatform.wallet.ui.send.SendFragment} and {@link
     * com.wavesplatform.wallet.ui.receive.ReceiveFragment} can safely ignore and choose the defaults
     * instead.
     */
    public int getSelectedAccountPosition() {
        int position = accountSpinner.getSelectedItemPosition();
        if (position >= accountSpinner.getCount() - 1) {
            // End of list is imported addresses, ignore
            position = 0;
        }

        return position - 1;
    }

    private void setupViews() {
        binding.noTransactionMessage.noTxMessage.setVisibility(View.GONE);

        accountSpinner = binding.accountsSpinner;
        viewModel.updateAccountList();

        setupAccountsSpinner();

        transactionAdapter = new TransactionsListAdapter(viewModel.getTransactionList(),
                dateUtil, AddressBookManager.get().getAll());
        transactionAdapter.setTxListClickListener(this::goToTransactionDetail);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        binding.rvTransactions.setHasFixedSize(true);
        binding.rvTransactions.setLayoutManager(layoutManager);
        binding.rvTransactions.setAdapter(transactionAdapter);

        binding.swipeContainer.setProgressViewEndTarget(false, (int) ViewUtils.convertDpToPixel(72 + 20, getActivity()));
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                binding.swipeContainer.setRefreshing(true);
                NodeManager.get().loadBalancesAndTransactions()
                        .compose(RxUtil.applySchedulersToCompletable())
                        .subscribe(() -> {
                            viewModel.updateAccountList();
                            viewModel.updateBalanceAndTransactionList(null, accountSpinner.getSelectedItemPosition());
                            binding.swipeContainer.setRefreshing(false);
                        }, err -> {
                            Log.e(TAG, "onRefresh", err);
                        });

            }
        });
        binding.swipeContainer.setColorSchemeResources(R.color.blockchain_receive_green,
                R.color.blockchain_blue,
                R.color.blockchain_send_red);

        binding.ivRefreshInfo.setOnClickListener(v -> showRefreshInfo());
        binding.balance.setOnClickListener(v -> binding.accountsSpinner.performClick());
    }

    private void setupAccountsSpinner() {
        accountsAdapter = new BalanceHeaderAdapter(
                context,
                R.layout.spinner_balance_header,
                viewModel.getActiveAccountAndAddressList());

        //accountsAdapter.setDropDownViewResource(R.layout.item_balance_account_dropdown);
        accountSpinner.setAdapter(accountsAdapter);
        accountSpinner.setOnTouchListener((v, event) -> event.getAction() == MotionEvent.ACTION_UP && ((MainActivity) getActivity()).getDrawerOpen());
        accountSpinner.post(() -> accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //Refresh balance header and tx list
                viewModel.updateBalanceAndTransactionList(null, accountSpinner.getSelectedItemPosition());
                binding.rvTransactions.scrollToPosition(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // No-op
            }
        }));
    }

    @Thunk
    void setToolbarOffset(int distance) {
        binding.balanceLayout.setTranslationY(-distance);
        if (distance > 1) {
            ViewUtils.setElevation(appBarLayout, ViewUtils.convertDpToPixel(5F, getActivity()));
        } else {
            ViewUtils.setElevation(appBarLayout, 0F);
        }
    }

    @Thunk
    void goToTransactionDetail( Transaction tx, int position) {
        Intent intent;
        if (tx instanceof PaymentTransaction)
            intent = new Intent(getActivity(), TransactionDetailActivity.class);
        else if (tx instanceof TransferTransaction)
            intent = new Intent(getActivity(), TransactionDetailActivity.class);
        else if (tx instanceof IssueTransaction)
            intent = new Intent(getActivity(), IssueDetailActivity.class);
        else if (tx instanceof ReissueTransaction)
            intent = new Intent(getActivity(), ReissueDetailActivity.class);
        else if (tx instanceof ExchangeTransaction)
            intent = new Intent(getActivity(), ExchangeTransactionActivity.class);
        else
            intent = new Intent(getActivity(), UnknownDetailActivity.class);

        intent.putExtra(KEY_TRANSACTION_LIST_POSITION, position);
        detailTransactionPosition = position;
        startActivity(intent);
    }

    @Override
    public void onRefreshAccounts() {
        //TODO revise
        if (accountSpinner != null)
            setAccountSpinner();

        context.runOnUiThread(() -> {
            if (accountsAdapter != null) accountsAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onAccountSizeChange() {
        if (accountSpinner != null)
            accountSpinner.setSelection(0);
    }

    @Override
    public void onRefreshBalanceAndTransactions() {
        // Notify adapter of change, let DiffUtil work out what needs changing
        List<Transaction> newTransactions = new ArrayList<>();
        ListUtil.addAllIfNotNull(newTransactions, viewModel.getTransactionList());
        transactionAdapter.onTransactionsUpdated(newTransactions);
        binding.balanceLayout.post(() -> setToolbarOffset(0));

        //Display help text to user if no transactionList on selected account/address
        if (viewModel.getTransactionList().size() > 0) {
            binding.rvTransactions.setVisibility(View.VISIBLE);
            binding.noTransactionMessage.noTxMessage.setVisibility(View.GONE);
        } else {
            binding.rvTransactions.setVisibility(View.GONE);
            binding.noTransactionMessage.noTxMessage.setVisibility(View.VISIBLE);
        }

        if (isAdded() && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //Fix for padding bug related to Android 4.1
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getResources().getDisplayMetrics());
            binding.balance.setPadding((int) px, 0, 0, 0);
        }

        accountsAdapter.notifyDataSetChanged();
        binding.rvTransactions.scrollToPosition(0);
    }

    private void showRefreshInfo() {
        new Tooltip.Builder(binding.ivRefreshInfo)
                .setText(getString(R.string.refresh_info))
                .setCancelable(true)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.toast_warning_background))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.toast_warning_text))
                .setPadding(ViewUtils.convertDpToPixel(8, getContext()))
                .setCornerRadius(ViewUtils.convertDpToPixel(8, getContext()))
                .setOnClickListener(Tooltip::dismiss)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.destroy();
    }

    @Override
    public void onScrollToTop() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            binding.rvTransactions.smoothScrollToPosition(0);
        }
    }

    public interface Communicator {

        void resetNavigationDrawer();

    }

    abstract class CollapseActionbarScrollListener extends RecyclerView.OnScrollListener {

        private int mToolbarOffset = 0;

        CollapseActionbarScrollListener() {
            // Empty Constructor
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if ((mToolbarOffset < balanceBarHeight && dy > 0) || (mToolbarOffset > 0 && dy < 0)) {
                mToolbarOffset += dy;
            }

            clipToolbarOffset();
            onMoved(mToolbarOffset);
        }

        private void clipToolbarOffset() {
            if (mToolbarOffset > balanceBarHeight) {
                mToolbarOffset = balanceBarHeight;
            } else if (mToolbarOffset < 0) {
                mToolbarOffset = 0;
            }
        }

        public abstract void onMoved(int distance);
    }
}