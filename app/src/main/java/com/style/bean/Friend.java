package com.style.bean;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Date;

public class Friend implements Serializable {

    public long id;//
    public long ownerId;// 主人id
    public long friendId;// 朋友id
    public String mark; //备注

    public long modifyDate;
    public User user;//好友用户
    public Friend() {
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", friendId=" + friendId +
                ", mark='" + mark + '\'' +
                ", modifyDate=" + modifyDate +
                '}';
    }
}
