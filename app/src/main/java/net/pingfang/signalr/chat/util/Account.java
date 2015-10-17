package net.pingfang.signalr.chat.util;

/**
 * Created by gongguopei87@gmail.com on 2015/10/17.
 */
public class Account {

    private int id,exp;
    private String username, phone,nickname,portrait,qq,email,address;
    public Account() {
        super();
        // TODO Auto-generated constructor stub
    }
    public Account(int id, int exp, String username, String phone,
                 String nickname, String portrait, String qq, String email,
                 String address) {
        super();
        this.id = id;
        this.exp = exp;
        this.username = username;
        this.phone = phone;
        this.nickname = nickname;
        this.portrait = portrait;
        this.qq = qq;
        this.email = email;
        this.address = address;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getExp() {
        return exp;
    }
    public void setExp(int exp) {
        this.exp = exp;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getPortrait() {
        return portrait;
    }
    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }
    public String getQq() {
        return qq;
    }
    public void setQq(String qq) {
        this.qq = qq;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}
