package com.bookapp.noribook.Model;

public class ModelFavorite {

    String bookId, bookTitle, timestamp;

    public ModelFavorite(){

    }

    public ModelFavorite(String bookId, String bookTitle, String timestamp) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.timestamp = timestamp;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
