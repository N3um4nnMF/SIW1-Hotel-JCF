package model;

import exception.DuplicateEntityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Hotel extends Accommodation implements Reviewable {
    private int stars;
    private double totalRating = 0;
    private int reviewCount = 0;
    private final List<Room> rooms = new ArrayList<>();
    private final Set<Guest> guests = new LinkedHashSet<>();

    public Hotel(String id, String name, String city, double pricePerNight, int capacity, int stars) {
        super(id, name, city, pricePerNight, capacity);
        setStars(stars);
    }

    public int getStars() { return stars; }

    public void setStars(int stars) {
        if (stars < 1 || stars > 5)
            throw new IllegalArgumentException("stars must be 1-5");
        this.stars = stars;
    }

    public void addRoom(Room room) {
        if (rooms.contains(room))
            throw new DuplicateEntityException("room " + room.getNumber() + " already exists in " + getName());
        rooms.add(room);
    }

    public boolean removeRoom(int number) {
        return rooms.removeIf(r -> r.getNumber() == number);
    }

    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }

    public boolean addGuest(Guest guest) { return guests.add(guest); }

    public boolean removeGuest(Guest guest) { return guests.remove(guest); }

    public Set<Guest> getGuests() { return Collections.unmodifiableSet(guests); }

    @Override
    public double calculateTotalPrice(int nights) {
        return getPricePerNight() * nights * (1 + stars * 0.02);
    }

    @Override
    public String getRoomType() { return "standard room"; }

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

    public void orderRoomService(String dish) {
        System.out.println("room service ordered: " + dish);
    }

    public void showRooms() {
        System.out.println("rooms of " + getName() + ":");
        if (rooms.isEmpty()) System.out.println("  (no rooms)");
        for (Room r : rooms) System.out.println("  " + r);
    }

    public void showGuests() {
        System.out.println("guests of " + getName() + ":");
        if (guests.isEmpty()) System.out.println("  (no guests)");
        for (Guest g : guests) System.out.println("  " + g);
    }
}
