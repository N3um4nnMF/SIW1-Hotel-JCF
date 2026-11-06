package service;

import exception.DuplicateEntityException;
import exception.EntityNotFoundException;
import model.*;

import java.util.*;
import java.util.function.Predicate;

public class BookingService {
    private final List<Accommodation> accommodations = new ArrayList<>();
    private final Map<String, Accommodation> accommodationById = new HashMap<>();
    private final Map<String, List<Accommodation>> accommodationsByCity = new HashMap<>();

    private final Set<Guest> guests = new LinkedHashSet<>();
    private final Map<String, Guest> guestByPassport = new HashMap<>();

    private final List<Booking> bookings = new ArrayList<>();
    private final Map<String, Booking> bookingById = new HashMap<>();
    private final Map<BookingStatus, List<Booking>> bookingsByStatus = new EnumMap<>(BookingStatus.class);
    private final Map<String, List<Booking>> bookingsByGuest = new HashMap<>();

    private final Queue<BookingRequest> requests = new PriorityQueue<>(BookingRequest.ORDER);
    private final Queue<String> cancellations = new ArrayDeque<>();

    private final GuestFileStorage storage;
    private long requestSeq = 0;
    private int bookingSeq = 0;

    public BookingService(String guestsFile) {
        storage = new GuestFileStorage(guestsFile);
        for (Guest g : storage.load()) {
            if (guests.add(g)) guestByPassport.put(g.getPassport(), g);
        }
    }

    private static <K, V> void addTo(Map<K, List<V>> index, K key, V value) {
        index.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    private static <K, V> void removeFrom(Map<K, List<V>> index, K key, V value) {
        List<V> list = index.get(key);
        if (list == null) return;
        list.remove(value);
        if (list.isEmpty()) index.remove(key);
    }

    private static String cityKey(String city) {
        return city.trim().toLowerCase();
    }

    public void addAccommodation(Accommodation a) {
        if (a == null) throw new IllegalArgumentException("accommodation must not be null");
        if (accommodationById.containsKey(a.getId()))
            throw new DuplicateEntityException("accommodation with id " + a.getId() + " already exists");
        accommodations.add(a);
        accommodationById.put(a.getId(), a);
        addTo(accommodationsByCity, cityKey(a.getCity()), a);
    }

    public Accommodation getAccommodation(String id) {
        Accommodation a = accommodationById.get(id);
        if (a == null) throw new EntityNotFoundException("accommodation not found: " + id);
        return a;
    }

    public void updateAccommodation(String id, String name, String city, double price) {
        Accommodation a = getAccommodation(id);
        String oldName = a.getName(), oldCity = a.getCity();
        double oldPrice = a.getPricePerNight();
        try {
            a.setName(name);
            a.setCity(city);
            a.setPricePerNight(price);
        } catch (IllegalArgumentException e) {
            a.setName(oldName);
            a.setCity(oldCity);
            a.setPricePerNight(oldPrice);
            throw e;
        }
        if (!cityKey(oldCity).equals(cityKey(a.getCity()))) {
            removeFrom(accommodationsByCity, cityKey(oldCity), a);
            addTo(accommodationsByCity, cityKey(a.getCity()), a);
        }
    }

    public void removeAccommodation(String id) {
        Accommodation a = getAccommodation(id);
        for (Booking b : new ArrayList<>(bookings)) {
            if (b.getAccommodation().equals(a)) removeBooking(b.getId());
        }
        requests.removeIf(r -> r.getAccommodation().equals(a));
        accommodations.remove(a);
        accommodationById.remove(id);
        removeFrom(accommodationsByCity, cityKey(a.getCity()), a);
    }

    public void registerGuest(Guest g) {
        if (g == null) throw new IllegalArgumentException("guest must not be null");
        if (!guests.add(g))
            throw new DuplicateEntityException("guest with passport " + g.getPassport() + " already exists");
        guestByPassport.put(g.getPassport(), g);
        storage.save(guests);
    }

    public Guest getGuest(String passport) {
        Guest g = passport == null ? null : guestByPassport.get(passport.trim().toUpperCase());
        if (g == null) throw new EntityNotFoundException("guest not found: " + passport);
        return g;
    }

    public void renameGuest(String passport, String newName) {
        getGuest(passport).setFullName(newName);
        storage.save(guests);
    }

    public void removeGuest(String passport) {
        Guest g = getGuest(passport);
        for (Booking b : new ArrayList<>(bookingsByGuest.getOrDefault(g.getPassport(), List.of()))) {
            removeBooking(b.getId());
        }
        requests.removeIf(r -> r.getGuest().equals(g));
        guests.remove(g);
        guestByPassport.remove(g.getPassport());
        storage.save(guests);
    }

    public Booking getBooking(String id) {
        Booking b = bookingById.get(id);
        if (b == null) throw new EntityNotFoundException("booking not found: " + id);
        return b;
    }

    private void addBooking(Booking b) {
        bookings.add(b);
        bookingById.put(b.getId(), b);
        addTo(bookingsByStatus, b.getStatus(), b);
        addTo(bookingsByGuest, b.getGuest().getPassport(), b);
    }

    private void changeStatus(Booking b, BookingStatus newStatus) {
        removeFrom(bookingsByStatus, b.getStatus(), b);
        b.setStatus(newStatus);
        addTo(bookingsByStatus, newStatus, b);
    }

    private void release(Booking b) {
        b.getAccommodation().cancelBooking();
        if (b.getAccommodation() instanceof Hotel h) h.removeGuest(b.getGuest());
    }

    public void removeBooking(String id) {
        Booking b = getBooking(id);
        if (b.getStatus() == BookingStatus.CONFIRMED) release(b);
        bookings.remove(b);
        bookingById.remove(id);
        removeFrom(bookingsByStatus, b.getStatus(), b);
        removeFrom(bookingsByGuest, b.getGuest().getPassport(), b);
        cancellations.remove(id);
    }

    public BookingRequest submitRequest(String passport, String accommodationId, int nights) {
        if (nights <= 0) throw new IllegalArgumentException("nights must be positive");
        BookingRequest r = new BookingRequest(++requestSeq, getGuest(passport), getAccommodation(accommodationId), nights);
        requests.offer(r);
        return r;
    }

    public void requestCancellation(String bookingId) {
        getBooking(bookingId);
        cancellations.offer(bookingId);
    }

    public Optional<Booking> processNextRequest() {
        BookingRequest req = requests.poll();
        if (req == null) return Optional.empty();
        Accommodation a = req.getAccommodation();
        boolean ok = a.book(req.getNights());
        Booking b = new Booking("B-" + (++bookingSeq), req.getGuest(), a, req.getNights(),
                ok ? BookingStatus.CONFIRMED : BookingStatus.REJECTED,
                ok ? a.calculateTotalPrice(req.getNights()) : 0);
        addBooking(b);
        if (ok && a instanceof Hotel h) h.addGuest(req.getGuest());
        return Optional.of(b);
    }

    public boolean processNextCancellation() {
        String id = cancellations.poll();
        Booking b = id == null ? null : bookingById.get(id);
        if (b == null || b.getStatus() != BookingStatus.CONFIRMED) return false;
        release(b);
        changeStatus(b, BookingStatus.CANCELLED);
        return true;
    }

    public int pendingRequests() { return requests.size(); }

    public int pendingCancellations() { return cancellations.size(); }

    public Optional<BookingRequest> peekNextRequest() { return Optional.ofNullable(requests.peek()); }

    public List<Accommodation> findByCity(String city) {
        if (city == null) return List.of();
        return List.copyOf(accommodationsByCity.getOrDefault(cityKey(city), List.of()));
    }

    public List<Accommodation> filter(Predicate<Accommodation> criteria) {
        List<Accommodation> result = new ArrayList<>();
        for (Accommodation a : accommodations) {
            if (criteria.test(a)) result.add(a);
        }
        return result;
    }

    public List<Accommodation> findByMaxPrice(double max) {
        return filter(a -> a.getPricePerNight() <= max);
    }

    public List<Accommodation> findByMinRating(double min) {
        return filter(a -> AccommodationComparators.ratingOf(a) >= min);
    }

    public List<Guest> findGuestsByName(String part) {
        String p = part == null ? "" : part.toLowerCase();
        List<Guest> result = new ArrayList<>();
        for (Guest g : guests) {
            if (g.getFullName().toLowerCase().contains(p)) result.add(g);
        }
        return result;
    }

    public List<Accommodation> sorted(Comparator<? super Accommodation> comparator) {
        List<Accommodation> copy = new ArrayList<>(accommodations);
        copy.sort(comparator);
        return copy;
    }

    public List<Booking> findBookingsByStatus(BookingStatus status) {
        return List.copyOf(bookingsByStatus.getOrDefault(status, List.of()));
    }

    public List<Booking> findBookingsOfGuest(String passport) {
        return List.copyOf(bookingsByGuest.getOrDefault(getGuest(passport).getPassport(), List.of()));
    }

    public List<Accommodation> getAccommodations() { return Collections.unmodifiableList(accommodations); }

    public Set<Guest> getGuests() { return Collections.unmodifiableSet(guests); }

    public List<Booking> getBookings() { return Collections.unmodifiableList(bookings); }
}
