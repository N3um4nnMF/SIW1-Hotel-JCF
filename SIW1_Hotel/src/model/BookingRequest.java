package model;

import java.util.Comparator;

public class BookingRequest {
    public static final Comparator<BookingRequest> ORDER =
            Comparator.comparingInt(BookingRequest::getPriority)
                      .thenComparingLong(BookingRequest::getSequence);

    private final long sequence;
    private final Guest guest;
    private final Accommodation accommodation;
    private final int nights;

    public BookingRequest(long sequence, Guest guest, Accommodation accommodation, int nights) {
        this.sequence = sequence;
        this.guest = guest;
        this.accommodation = accommodation;
        this.nights = nights;
    }

    public long getSequence() { return sequence; }
    public Guest getGuest() { return guest; }
    public Accommodation getAccommodation() { return accommodation; }
    public int getNights() { return nights; }
    public int getPriority() { return guest.isVip() ? 0 : 1; }

    @Override
    public String toString() {
        return "request#" + sequence + (guest.isVip() ? " [VIP] " : " ") + guest.getFullName()
                + " -> " + accommodation.getName() + " (" + nights + " nights)";
    }
}
