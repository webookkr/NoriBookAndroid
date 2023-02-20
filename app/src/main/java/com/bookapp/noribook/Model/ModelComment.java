package com.bookapp.noribook.Model;

public class ModelComment {

    //variable
    String comment, date, subNumber,subTitle,uid;

    //empty
    public ModelComment(){

    }

    public ModelComment(String comment, String date, String subNumber, String subTitle, String uid) {
        this.comment = comment;
        this.date = date;
        this.subNumber = subNumber;
        this.subTitle = subTitle;
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubNumber() {
        return subNumber;
    }

    public void setSubNumber(String subNumber) {
        this.subNumber = subNumber;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
