package com.bookapp.noribook.Model;

public class ModelBook {
    //변수 선언 - firebase에서 받아올 것이므로 그대로
    String uid, id, title, description, date, categoryTitle, url, categoryId;
    long viewCount, viewCountMinus;

    long recommendCount, recommendCountMinus;

    boolean favorite;

    boolean featured;

    public ModelBook(){

    }


    public ModelBook(String uid, String id, String title, String description, String date, String categoryTitle, String url, String categoryId, long viewCount, long viewCountMinus, long recommendCount, long recommendCountMinus, boolean favorite, boolean featured) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.categoryTitle = categoryTitle;
        this.url = url;
        this.categoryId = categoryId;
        this.viewCount = viewCount;
        this.viewCountMinus = viewCountMinus;
        this.recommendCount = recommendCount;
        this.recommendCountMinus = recommendCountMinus;
        this.favorite = favorite;
        this.featured = featured;
    }

    public long getViewCountMinus() {
        return viewCountMinus;
    }

    public void setViewCountMinus(long viewCountMinus) {
        this.viewCountMinus = viewCountMinus;
    }

    public long getRecommendCountMinus() {
        return recommendCountMinus;
    }

    public void setRecommendCountMinus(long recommendCountMinus) {
        this.recommendCountMinus = recommendCountMinus;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getRecommendCount() {
        return recommendCount;
    }

    public void setRecommendCount(long recommendCount) {
        this.recommendCount = recommendCount;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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
