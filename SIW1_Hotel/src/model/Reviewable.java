package model;

public interface Reviewable {
    void addReview(int rating, String comment);
    double getAverageRating();
}
