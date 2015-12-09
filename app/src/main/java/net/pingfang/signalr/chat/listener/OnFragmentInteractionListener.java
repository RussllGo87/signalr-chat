package net.pingfang.signalr.chat.listener;

import net.pingfang.signalr.chat.database.User;

/**
 * Created by gongguopei87@gmail.com on 2015/8/15.
 */
public interface OnFragmentInteractionListener {
    void loadAccountInfo();
    void shield(User user);

    void onMsgItemLongClick(int position, User user);
}
