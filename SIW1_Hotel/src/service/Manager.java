package service;

import model.Booking;

import java.util.Optional;

public class Manager {
    private final String name;
    private int processed = 0;

    public Manager(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("manager name cannot be empty");
        this.name = name;
    }

    public String getName() { return name; }

    public int getProcessed() { return processed; }

    public Optional<Booking> processNext(BookingService service) {
        Optional<Booking> result = service.processNextRequest();
        if (result.isPresent()) processed++;
        return result;
    }

    public int processAllCancellations(BookingService service) {
        int count = 0;
        while (service.pendingCancellations() > 0) {
            if (service.processNextCancellation()) {
                count++;
                processed++;
            }
        }
        return count;
    }
}
