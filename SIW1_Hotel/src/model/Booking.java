package model;

public class Booking {
    private final String id;
    private final Guest guest;
    private final Accommodation accommodation;
    private final int nights;
    private final double totalPrice;
    private BookingStatus status;

    public Booking(String id, Guest guest, Accommodation accommodation,
                   int nights, BookingStatus status, double totalPrice) {
        this.id = id;
        this.guest = guest;
        this.accommodation = accommodation;
        this.nights = nights;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public String getId() { return id; }
    public Guest getGuest() { return guest; }
    public Accommodation getAccommodation() { return accommodation; }
    public int getNights() { return nights; }
    public double getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Booking b && id.equals(b.id));
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return String.format("%s | %s -> %s | %d nights | %.2f$ | %s",
                id, guest.getFullName(), accommodation.getName(), nights, totalPrice, status);
    }
}
