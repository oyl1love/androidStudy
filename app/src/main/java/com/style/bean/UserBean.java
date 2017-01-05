package com.style.bean;

import android.text.TextUtils;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;
import java.util.Date;

@Entity
public class UserBean implements Serializable {
	public static final String ID = "id";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "userName";
    public static final String NICKNAME = "nickName";
	public static final String PASSWORD = "passWord";
	public static final String HEADURL = "headUrl";
	public static final String SIGNATURE = "signature";
	public static final String SEX = "sex";
	public static final String LOCATION = "location";
	public static final String BIRTHDAY = "birthday";
	public static final String TYPE ="type";
	public static final String TELEPHONE = "telephone";
    public static final String CREATE_DATE = "createDate";
    public static final String MODIFY_DATE = "modifyDate";

    @Id(autoincrement = true)
    public int id;//
    public int userId;// 用户id
    public String userName;// 用户名
    public String nickName; //昵称
    public String passWord;// 头像保存路径
    public String telephone;// 手机号
    public String headUrl;// 头像保存路径
    public String signature;// 个性签名
    public String sex;// 性别: M男士，W女士
    public String location;// 位置信息
    public String birthday;// 生日
    public String type;// N-正常用户，P-公众账号
    @Transient
    public Date createDate;
    public long modifyDate;

    public UserBean(){

    }

    public enum Sex{
        MAN("M"), WOMAN("W");

        public String value;

        Sex(String s){
            this.value = s;
        }

        public static boolean isMan(String male){
            if(TextUtils.isEmpty(male)){
                return true;
            }
            if(MAN.value.equals(male)){
                return true;
            }else {
                return false;
            }
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTelephone() {
		return telephone;
	}

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getHeadUrl() {
		return headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getShowName(){
        if(!TextUtils.isEmpty(nickName)){
            return nickName;
        }
        return userName;
    }
}
