package com.baofeng.aone.userdata;

public class UserInfo {

    private String phoneNumber;
    private String serailNumber;
    private String username;
    private String password;
    private String token;
    public UserInfo(){

    }

    public UserInfo(String phoneNumber, String serailNumber) {
        this.phoneNumber = phoneNumber;
        this.serailNumber = serailNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSerailNumber() {
        return serailNumber;
    }

    public void setSerailNumber(String serailNumber) {
        this.serailNumber = serailNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
