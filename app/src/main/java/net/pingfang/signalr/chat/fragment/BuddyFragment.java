package net.pingfang.signalr.chat.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.pingfang.signalr.chat.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BuddyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BuddyFragment extends Fragment {

    ListView list_user;

    public static BuddyFragment newInstance() {
        BuddyFragment fragment = new BuddyFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_buddy, container, false);
        list_user = (ListView) view.findViewById(R.id.list_user);

        return inflater.inflate(R.layout.fragment_buddy, container, false);
    }


}
