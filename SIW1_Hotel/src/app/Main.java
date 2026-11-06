package app;

import exception.DuplicateEntityException;
import exception.EntityNotFoundException;
import model.*;
import service.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    private static final String GUESTS_FILE = "data/guests.txt";
    private static final String BAD_FILE = "data/test_invalid.txt";
    private static final boolean RESET_DEMO_DATA = true;

    private static void title(String text) {
        System.out.println();
        System.out.println("========================================");
        System.out.println(" " + text);
        System.out.println("========================================");
    }

    private static void printFile(String label) throws IOException {
        System.out.println("--- " + label + " (" + GUESTS_FILE + ") ---");
        List<String> lines = Files.readAllLines(Path.of(GUESTS_FILE));
        if (lines.isEmpty()) System.out.println("  (empty file)");
        for (String line : lines) System.out.println("  " + line);
    }

    public static void main(String[] args) throws IOException {
        if (RESET_DEMO_DATA) Files.deleteIfExists(Path.of(GUESTS_FILE));

        BookingService service = new BookingService(GUESTS_FILE);
        ReportService report = new ReportService(service);
        Manager manager = new Manager("Aigerim");

        title("1. SETUP: registries and contracts (Block A)");
        Hotel grand = new Hotel("H-001", "grand plaza", "almaty", 120, 4, 5);
        grand.addRoom(new Room(101, "single", 100));
        grand.addRoom(new Room(102, "double", 150));
        grand.addRoom(new Room(201, "suite", 300));
        Hotel silk = new Hotel("H-002", "Silk Road", "almaty", 90, 3, 3);
        Hostel nomad = new Hostel("HS-001", "nomad backpackers", "bishkek", 15, 20, 6);
        Hostel cosmos = new Hostel("HS-002", "Cosmos Hostel", "almaty", 12, 30, 8);
        Apartment loft = new Apartment("A-001", "skyline loft", "astana", 80, 4, 2);
        for (Accommodation a : List.of(grand, silk, nomad, cosmos, loft)) service.addAccommodation(a);

        service.registerGuest(new Guest("ivan petrov", "AB123456"));
        service.registerGuest(new Guest("anna sidorova", "CD789012"));
        service.registerGuest(new Guest("dana nurlanova", "EF345678", true));
        service.registerGuest(new Guest("marat sadykov", "GH901234"));

        System.out.println("Accommodations: " + service.getAccommodations().size()
                + ", guests: " + service.getGuests().size());
        grand.showRooms();
        try {
            service.registerGuest(new Guest("IVAN P.", "ab123456"));
        } catch (DuplicateEntityException e) {
            System.out.println("Duplicate guest rejected: " + e.getMessage());
        }
        try {
            grand.addRoom(new Room(101, "other", 999));
        } catch (DuplicateEntityException e) {
            System.out.println("Duplicate room rejected: " + e.getMessage());
        }
        Guest copy = new Guest("x", "AB123456");
        System.out.println("equals: " + copy.equals(service.getGuest("AB123456"))
                + ", same hashCode: " + (copy.hashCode() == service.getGuest("AB123456").hashCode()));

        title("2. FAST LOOKUP & INDEXING (Block B)");
        System.out.println("By id H-002: " + service.getAccommodation("H-002"));
        System.out.println("Guest by passport (lower case input): " + service.getGuest("cd789012"));
        report.printList("Index by city: almaty", service.findByCity("almaty"));

        title("3. SORTING & FILTERS (Block C)");
        grand.addReview(5, "Great stay!");
        grand.addReview(4, "Nice breakfast");
        silk.addReview(3, "Noisy street");
        loft.addReview(5, "Cozy loft");
        report.printList("Natural order (Comparable)", service.sorted(null));
        report.printList("Comparator: BY_PRICE", service.sorted(AccommodationComparators.BY_PRICE));
        report.printList("Comparator: BY_RATING_DESC", service.sorted(AccommodationComparators.BY_RATING_DESC));
        report.printList("Comparator: BY_CITY_PRICE_NAME", service.sorted(AccommodationComparators.BY_CITY_PRICE_NAME));
        report.printList("Filter: price <= 90", service.findByMaxPrice(90));
        report.printList("Filter: rating >= 4", service.findByMinRating(4));
        report.printList("Filter: guests with 'an' in name", service.findGuestsByName("an"));

        title("4. ROLES: GUEST submits, MANAGER polls (Block D)");
        System.out.println("[GUEST] requests:");
        System.out.println("  " + service.submitRequest("CD789012", "A-001", 3));
        System.out.println("  " + service.submitRequest("AB123456", "H-001", 3));
        System.out.println("  " + service.submitRequest("GH901234", "HS-001", 2));
        System.out.println("  " + service.submitRequest("EF345678", "A-001", 2));
        System.out.println("Pending: " + service.pendingRequests()
                + ", next in line: " + service.peekNextRequest().orElseThrow());

        System.out.println("\n[MANAGER " + manager.getName() + "] processing:");
        while (service.pendingRequests() > 0) {
            manager.processNext(service).ifPresent(b -> System.out.println("  -> " + b));
        }
        System.out.println("Poll on empty queue: " + service.processNextRequest().isPresent());
        grand.showGuests();

        System.out.println("\n[GUEST] ivan asks to cancel his booking:");
        Booking ivanBooking = service.findBookingsOfGuest("AB123456").get(0);
        service.requestCancellation(ivanBooking.getId());
        System.out.println("[MANAGER] cancelled: " + manager.processAllCancellations(service));
        System.out.println("  -> " + ivanBooking);
        grand.showGuests();

        title("5. SYNCHRONIZED CRUD");
        service.updateAccommodation("H-002", "Silk Road", "shymkent", 95);
        report.printList("city = almaty after moving H-002", service.findByCity("almaty"));
        report.printList("city = shymkent", service.findByCity("shymkent"));
        try {
            service.updateAccommodation("H-002", "Hacked", "Moscow", -5);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid update rolled back: " + e.getMessage()
                    + " -> " + service.getAccommodation("H-002"));
        }

        System.out.println("\nRemove guest marat (confirmed booking at nomad backpackers):");
        System.out.println("  hostel booked before: " + nomad.isBooked());
        service.removeGuest("GH901234");
        System.out.println("  hostel booked after: " + nomad.isBooked()
                + ", guests=" + service.getGuests().size() + ", bookings=" + service.getBookings().size());

        service.submitRequest("AB123456", "HS-002", 4);
        System.out.println("\nPending before removing HS-002: " + service.pendingRequests());
        service.removeAccommodation("HS-002");
        System.out.println("Pending after: " + service.pendingRequests()
                + ", accommodations=" + service.getAccommodations().size());

        title("6. FILE STORAGE OF CLIENTS");
        printFile("File after removing marat");
        service.renameGuest("CD789012", "anna sidorova-kim");
        printFile("File after renaming anna");
        service.registerGuest(new Guest("timur akhmetov", "IJ112233"));
        printFile("File after adding timur");

        System.out.println("\nRestart: a new BookingService reads the same file");
        BookingService restarted = new BookingService(GUESTS_FILE);
        report.printList("Guests loaded from file", restarted.getGuests());
        System.out.println("Loaded VIP flag kept: " + restarted.getGuest("EF345678").isVip());

        Files.write(Path.of(BAD_FILE), List.of("KL445566;olga ivanova;false", "broken line", "MN778899;;true"));
        System.out.println("\nTest file with 1 valid and 2 invalid lines:");
        System.out.println("Guests loaded: " + new BookingService(BAD_FILE).getGuests().size());
        Files.deleteIfExists(Path.of(BAD_FILE));
        System.out.println("Missing file -> guests loaded: " + new BookingService(BAD_FILE).getGuests().size());

        title("7. EDGE CASES & EXCEPTIONS");
        try { service.getAccommodation("X-999"); }
        catch (EntityNotFoundException e) { System.out.println("Missing key     -> " + e.getMessage()); }
        try { service.submitRequest("AB123456", "H-001", 0); }
        catch (IllegalArgumentException e) { System.out.println("Invalid nights  -> " + e.getMessage()); }
        try { service.submitRequest("ZZ000000", "H-001", 1); }
        catch (EntityNotFoundException e) { System.out.println("Unknown guest   -> " + e.getMessage()); }
        try { service.requestCancellation("B-404"); }
        catch (EntityNotFoundException e) { System.out.println("Unknown booking -> " + e.getMessage()); }
        try { service.addAccommodation(new Hotel("H-001", "Clone", "x", 1, 1, 1)); }
        catch (DuplicateEntityException e) { System.out.println("Duplicate id    -> " + e.getMessage()); }
        try { new Guest("bad;name", "QQ000001"); }
        catch (IllegalArgumentException e) { System.out.println("Bad guest name  -> " + e.getMessage()); }
        System.out.println("Unknown city     -> " + service.findByCity("paris"));
        System.out.println("Null city        -> " + service.findByCity(null));
        System.out.println("Cancel queue empty -> " + service.processNextCancellation());

        title("8. ANALYTICS & REPORTS");
        report.printPriceStatisticsByType();
        report.printBookingSummary();
        report.printGuestSpendingRanking();
        report.printList("All bookings", service.getBookings());
    }
}
