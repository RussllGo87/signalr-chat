package net.pingfang.signalr.chat.listener;

/**
 * Created by gongguopei87@gmail.com on 2015/8/21.
 */
public interface OnRegisterInteractionListener {
    void loadVC(String phoneNo);
    void submitC(String phoneNo,String vc);
    void submitInfo(String nick,String password,String qq);
}
