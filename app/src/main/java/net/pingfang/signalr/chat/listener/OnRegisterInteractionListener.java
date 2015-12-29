package net.pingfang.signalr.chat.listener;

/**
 * Created by gongguopei87@gmail.com on 2015/8/21.
 */
public interface OnRegisterInteractionListener {
    void validate(String phoneNo);
    void loadCode(String phoneNo);
    void submitCode(String phoneNo,String vc);
    void submitInfo(String... arg);
    void nextPage();
}
