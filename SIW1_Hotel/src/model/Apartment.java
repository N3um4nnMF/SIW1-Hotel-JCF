package model;

public class Apartment extends Accommodation implements Reviewable {
    private int rooms;
    private double totalRating = 0;
    private int reviewCount = 0;

    public Apartment(String id, String name, String city, double pricePerNight, int capacity, int rooms) {
        super(id, name, city, pricePerNight, capacity);
        setRooms(rooms);
    }

    public int getRooms() { return rooms; }

    public void setRooms(int rooms) {
        if (rooms <= 0)
            throw new IllegalArgumentException("rooms must be positive");
        this.rooms = rooms;
    }

    @Override
    public double calculateTotalPrice(int nights) {
        return getPricePerNight() * nights + 30;
    }

    @Override
    public String getRoomType() { return rooms + "-room apartment"; }

    @Override
    public void addReview(int rating, String comment) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("rating must be 1-5");
        totalRating += rating;
        reviewCount++;
    }

    @Override
    public double getAverageRating() {
        return reviewCount == 0 ? 0 : totalRating / reviewCount;
    }

    public void cookMeal(String dish) {
        System.out.println("cooking " + dish + " in the apartment kitchen");
    }
}
