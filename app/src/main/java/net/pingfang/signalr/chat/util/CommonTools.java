package net.pingfang.signalr.chat.util;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gongguopei87@gmail.com on 2015/8/21.
 */
public class CommonTools {

    public static boolean isPhoneNumber(String phoneNumber) {
        if(!TextUtils.isEmpty(phoneNumber)) {
            String regex="1([\\d]{10})|((\\+[0-9]{2,4})?\\(?[0-9]+\\)?-?)?[0-9]{7,8}";
            Pattern p = Pattern.compile(regex);
            Matcher matcher = p.matcher(phoneNumber);
            return matcher.find();
        } else {
            return false;
        }
    }

    public static boolean isAvailableVc(String vc) {
        if(!TextUtils.isEmpty(vc) && TextUtils.isDigitsOnly(vc) && vc.length() == 6) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAvailableEmail(String email) {
        if(!TextUtils.isEmpty(email)) {
            String regex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
            Pattern p = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
            Matcher matcher = p .matcher(email);
            return matcher.find();
        } else {
            return false;
        }
    }

    public static boolean checkRegParams(String nick,String password,String qq,String email) {
        if(!TextUtils.isEmpty(nick) &&
                !TextUtils.isEmpty(password) && !TextUtils.isEmpty(qq) &&
                TextUtils.isDigitsOnly(qq) && isAvailableEmail(email)) {
            return true;
        } else {
            return false;
        }
    }
}
