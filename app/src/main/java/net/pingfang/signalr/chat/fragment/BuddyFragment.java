package net.pingfang.signalr.chat.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.activity.ChatActivity;
import net.pingfang.signalr.chat.adapter.ListCursorAdapter;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.listener.OnFragmentInteractionListener;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BuddyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BuddyFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = 0x01;

    private ListView list_user;

    private ListCursorAdapter listCursorAdapter;

    private OnFragmentInteractionListener mListener;

    SharedPreferencesHelper sharedPreferencesHelper;

    public static BuddyFragment newInstance(OnFragmentInteractionListener mListener) {
        BuddyFragment fragment = new BuddyFragment();
        fragment.mListener = mListener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_buddy, container, false);
        list_user = (ListView) view.findViewById(R.id.list_user);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listCursorAdapter = new ListCursorAdapter(getContext(),null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        list_user.setAdapter(listCursorAdapter);
        list_user.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListCursorAdapter.UserHolder userHolder = (ListCursorAdapter.UserHolder) view.getTag();

                mListener.updateMessageList(userHolder.getNickname(), userHolder.getUid(),userHolder.getPortrait(), "");

                Intent intent = new Intent();
                intent.setClass(getContext(), ChatActivity.class);
                intent.putExtra("name", userHolder.getNickname());
                intent.putExtra("uid", userHolder.getUid());
                startActivity(intent);
            }
        });

        String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_SYS_CURRENT_UID,uid);

        getLoaderManager().initLoader(LOADER_ID,args,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String uid = args.getString(AppConstants.KEY_SYS_CURRENT_UID);

        Uri baseUri = AppContract.UserEntry.CONTENT_URI;

        String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " != ?";
        String[] selectionArgs = new String[]{uid};

        return new CursorLoader(getContext(),baseUri,null,selection,selectionArgs,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listCursorAdapter.swapCursor(null);
    }
}
