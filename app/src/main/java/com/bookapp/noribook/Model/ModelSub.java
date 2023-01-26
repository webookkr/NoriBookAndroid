package com.bookapp.noribook.Model;

public class ModelSub {

    ModelSub(){

    }

    String uid, subNumber, subTitle, date, bookTitle, url;

    long viewCount;
    long recommendCount;

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

    public long getRecommendCount() {
        return recommendCount;
    }

    public void setRecommendCount(long recommendCount) {
        this.recommendCount = recommendCount;
    }

    public ModelSub(String uid, String subNumber, String subTitle, String date, String bookTitle, String url, long viewCount, long recommendCount) {
        this.uid = uid;
        this.subNumber = subNumber;
        this.subTitle = subTitle;
        this.date = date;
        this.bookTitle = bookTitle;
        this.url = url;
        this.viewCount = viewCount;
        this.recommendCount = recommendCount;



    }
}
