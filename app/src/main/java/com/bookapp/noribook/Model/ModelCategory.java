package com.bookapp.noribook.Model;

public class ModelCategory {

    String id, category, date, uid ;

    public ModelCategory(){

    }

    public ModelCategory(String id, String category, String date, String uid) {
        this.id = id;
        this.category = category;
        this.date = date;
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
