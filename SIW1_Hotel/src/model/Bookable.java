package model;

public interface Bookable {
    boolean book(int nights);
    void cancelBooking();
    boolean isBooked();
}
