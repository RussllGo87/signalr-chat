package net.pingfang.signalr.chat.listener;

import net.pingfang.signalr.chat.database.User;

/**
 * Created by gongguopei87@gmail.com on 2015/12/7.
 */
public interface OnItemListInteractionListener {
    void onItemShield(User user);

    void onItemDelete(User user);
}

