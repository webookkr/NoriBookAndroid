package com.bookapp.noribook.Model;

public class ModelComment {

    //variable
    String comment, date, subNumber, uid, bookTitle, id;

    //empty
    public ModelComment() {

    }

    public ModelComment(String comment, String date, String subNumber, String uid, String bookTitle, String id) {
        this.comment = comment;
        this.date = date;
        this.subNumber = subNumber;
        this.uid = uid;
        this.bookTitle = bookTitle;
        this.id = id;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
