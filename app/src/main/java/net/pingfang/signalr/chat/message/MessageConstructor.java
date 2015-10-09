package net.pingfang.signalr.chat.message;

import net.pingfang.signalr.chat.util.CommonTools;

/**
 * Created by gongguopei87@gmail.com on 2015/10/9.
 */
public class MessageConstructor {

    public static String constructTxtMessage(String uid, String buddyUid, String content) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{");
        stringBuffer.append("\"");
        stringBuffer.append("Sender");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append(uid);
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

    public static String constructFileMessage(String uid, String buddyUid, String messageType,String fileExtension, String fileBody) {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("{");
        stringBuffer.append("\"");
        stringBuffer.append("Sender");
        stringBuffer.append("\"");
        stringBuffer.append(":");
        stringBuffer.append(uid);
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
}
