package net.pingfang.signalr.chat.listener;

/**
 * Created by gongguopei87@gmail.com on 2015/11/30.
 */
public interface OnBuddyFragmentInteractionListener extends OnFragmentInteractionListener, OnItemListInteractionListener {
    void loadTop();
    void loadBottom();
}
