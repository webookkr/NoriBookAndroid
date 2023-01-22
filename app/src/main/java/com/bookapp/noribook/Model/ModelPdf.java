package com.bookapp.noribook.Model;

public class ModelPdf {
    //변수 선언 - firebase에서 받아올 것이므로 그대로
    String uid, id, title, description, date, categoryTitle, url;

    public ModelPdf(){

    }

    public ModelPdf(String uid, String id, String title, String description, String date, String categoryTitle, String url) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.categoryTitle = categoryTitle;
        this.url = url;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
