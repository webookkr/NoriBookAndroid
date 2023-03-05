package com.bookapp.noribook.Model;

public class ModelSub {

    String uid, subTitle, date, bookTitle, url;

    String subNumber, subId;
    long viewCount;
    long recommend;


    public ModelSub(){

    }

    public ModelSub(String uid, String subTitle, String date, String bookTitle, String url, String subNumber, String subId, long viewCount, long recommend) {
        this.uid = uid;
        this.subTitle = subTitle;
        this.date = date;
        this.bookTitle = bookTitle;
        this.url = url;
        this.subNumber = subNumber;
        this.subId = subId;
        this.viewCount = viewCount;
        this.recommend = recommend;
    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getRecommend() {
        return recommend;
    }

    public void setRecommend(long recommend) {
        this.recommend = recommend;
    }


}
