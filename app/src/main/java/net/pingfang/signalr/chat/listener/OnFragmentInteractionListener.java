package net.pingfang.signalr.chat.listener;

/**
 * Created by gongguopei87@gmail.com on 2015/8/15.
 */
public interface OnFragmentInteractionListener {

    void loadMessage();
    void loadAccountInfo();
    void onFragmentInteraction(String name,String uid);
}
