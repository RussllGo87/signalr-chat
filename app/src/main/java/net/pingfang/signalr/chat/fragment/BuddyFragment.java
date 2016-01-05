package net.pingfang.signalr.chat.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.activity.ChatActivity;
import net.pingfang.signalr.chat.adapter.BuddyListCursorAdapter;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.listener.OnBuddyFragmentInteractionListener;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BuddyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BuddyFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = 0x01;
    SharedPreferencesHelper sharedPreferencesHelper;
    MessageReceiver receiver;
    private SwipyRefreshLayout swipe_refresh_layout;
    private ListView list_user;
    private BuddyListCursorAdapter listCursorAdapter;
    private OnBuddyFragmentInteractionListener mListener;
    private Handler mDelivery = new Handler(Looper.getMainLooper());

    private int currentPage = 1;

    public static BuddyFragment newInstance(OnBuddyFragmentInteractionListener mListener) {
        BuddyFragment fragment = new BuddyFragment();
        fragment.mListener = mListener;
        return fragment;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getContext());
        registerReceiver();
    }

    public void registerReceiver() {
        receiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalApplication.ACTION_INTENT_UPDATE_ONLINE_LIST);
        getContext().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_buddy, container, false);
        swipe_refresh_layout = (SwipyRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                boolean loadNextPage = !(direction == SwipyRefreshLayoutDirection.TOP);
                if (loadNextPage) {
                    currentPage = currentPage + 1;
                    mListener.loadBottom();
                } else {
                    currentPage = 1;
                    mListener.loadTop();
                }

                mDelivery.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipe_refresh_layout.setRefreshing(false);
                    }
                }, 8000);
            }
        });
        list_user = (ListView) view.findViewById(R.id.list_user);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list_user.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BuddyListCursorAdapter.ViewDataHolder holder = (BuddyListCursorAdapter.ViewDataHolder) view.getTag();

                User user = new User(holder.getUid(), holder.getNickname(), holder.getPortraitUrl(), holder.getRemark(), holder.getGender(),
                        holder.getMsgListStatus(), holder.getNearbyStatus(), 0, holder.getDistance());

                Intent intent = new Intent();
                intent.setClass(getContext(), ChatActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_SYS_CURRENT_UID,uid);

        getLoaderManager().initLoader(LOADER_ID, args, this);
    }

    @Override
    public void onStop() {
        super.onStop();

        getLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(receiver != null) {
            getContext().unregisterReceiver(receiver);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String uid = args.getString(AppConstants.KEY_SYS_CURRENT_UID);

        Uri baseUri = AppContract.UserEntry.CONTENT_URI;
        String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " != ? " +
                "AND " +
                AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST + " = ? ";
        String[] selectionArgs = new String[]{uid, String.valueOf(User.USER_STATUS_NEARBY_LIST_IN)};
        String sortOrder = AppContract.UserEntry.COLUMN_NAME_DISTANCE + " ASC ";

        return new CursorLoader(getContext(), baseUri, null, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listCursorAdapter = new BuddyListCursorAdapter(getContext(),data, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        list_user.setAdapter(listCursorAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listCursorAdapter.swapCursor(null);
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            swipe_refresh_layout.setRefreshing(false);

            String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
            Bundle args = new Bundle();
            args.putString(AppConstants.KEY_SYS_CURRENT_UID,uid);
            getLoaderManager().restartLoader(LOADER_ID, args, BuddyFragment.this);
        }
    }
}
