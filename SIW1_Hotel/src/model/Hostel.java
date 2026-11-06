package model;

public class Hostel extends Accommodation {
    private int bedsPerRoom;

    public Hostel(String id, String name, String city, double pricePerNight, int capacity, int bedsPerRoom) {
        super(id, name, city, pricePerNight, capacity);
        setBedsPerRoom(bedsPerRoom);
    }

    public int getBedsPerRoom() { return bedsPerRoom; }

    public void setBedsPerRoom(int bedsPerRoom) {
        if (bedsPerRoom <= 0)
            throw new IllegalArgumentException("beds must be positive");
        this.bedsPerRoom = bedsPerRoom;
    }

    @Override
    public double calculateTotalPrice(int nights) {
        return getPricePerNight() * nights * 0.85;
    }

    @Override
    public String getRoomType() { return "shared dorm"; }

    public void rentLocker() {
        System.out.println("locker rented for personal belongings");
    }
}
