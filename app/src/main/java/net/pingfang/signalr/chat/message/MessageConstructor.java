package net.pingfang.signalr.chat.message;

import net.pingfang.signalr.chat.util.CommonTools;

/**
 * Created by gongguopei87@gmail.com on 2015/10/9.
 */
public class MessageConstructor {

    public static String constructTxtMessage(String uid, String nickname, String portrait, String buddyUid, String content) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{");
        stringBuffer.append("\"");
        stringBuffer.append("Sender");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append(uid);
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("SenderName");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(nickname);
        stringBuffer.append("\"");
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("SenderPortrait");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(portrait);
        stringBuffer.append("\"");
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("Receiver");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append(buddyUid);
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("MessageType");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append("Text");
        stringBuffer.append("\"");
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("Contents");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(content);
        stringBuffer.append("\"");
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("SendTime");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(CommonTools.TimeConvertString());
        stringBuffer.append("\"");
        stringBuffer.append("}");

        return stringBuffer.toString();
    }

    public static String constructFileMessage(String uid, String nickname, String portrait, String buddyUid, String messageType,String fileExtension, String fileBody) {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("{");
        stringBuffer.append("\"");
        stringBuffer.append("Sender");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append(uid);
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("SenderName");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(nickname);
        stringBuffer.append("\"");
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("SenderPortrait");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(portrait);
        stringBuffer.append("\"");
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("Receiver");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append(buddyUid);
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("MessageType");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(messageType);
        stringBuffer.append("\"");
        stringBuffer.append(",");

        stringBuffer.append("\"");
        stringBuffer.append("fileExtension");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(fileExtension);
        stringBuffer.append("\"");
        stringBuffer.append(",");

        stringBuffer.append("\"");
        stringBuffer.append("Contents");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(fileBody);
        stringBuffer.append("\"");

        stringBuffer.append(",");

        stringBuffer.append("\"");
        stringBuffer.append("SendTime");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(CommonTools.TimeConvertString());
        stringBuffer.append("\"");

        stringBuffer.append("}");

        return stringBuffer.toString();
    }

    public static String constructOfflineMsgReq(String from, String to) {
        return constructOfflineMsgReq(from,to,0,0);
    }

    public static String constructOfflineMsgReq(String from, String to, int pageNo, int rows) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{");
        stringBuffer.append("\"");
        stringBuffer.append("Sender");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(from);
        stringBuffer.append("\"");
        stringBuffer.append(",");
        stringBuffer.append("\"");
        stringBuffer.append("Receiver");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append("\"");
        stringBuffer.append(to);
        stringBuffer.append("\"");
        if(pageNo != 0) {
            stringBuffer.append(",");
            stringBuffer.append("\"");
            stringBuffer.append("Page");
            stringBuffer.append("\"");
            stringBuffer.append(":");
            stringBuffer.append("\"");
            stringBuffer.append(pageNo);
            stringBuffer.append("\"");
        }

        if(rows != 0) {
            stringBuffer.append(",");
            stringBuffer.append("\"");
            stringBuffer.append("Rows");
            stringBuffer.append("\"");
            stringBuffer.append(":");
            stringBuffer.append("\"");
            stringBuffer.append(rows);
            stringBuffer.append("\"");
        }

        stringBuffer.append("}");

        return stringBuffer.toString();
    }
}
