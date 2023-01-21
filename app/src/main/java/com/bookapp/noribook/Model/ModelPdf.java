package com.bookapp.noribook.Model;

public class ModelPdf {
    //변수 선언 - firebase에서 받아올 것이므로 그대로
    String uid, id, title, description, date, category;

    public ModelPdf(String uid, String id, String title, String description, String date, String category) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.category = category;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
