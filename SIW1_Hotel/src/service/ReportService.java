package service;

import model.*;

import java.util.*;

public class ReportService {
    private final BookingService service;

    public ReportService(BookingService service) {
        this.service = service;
    }

    public void printList(String title, Collection<?> items) {
        System.out.println("--- " + title + " ---");
        if (items.isEmpty()) System.out.println("  (empty)");
        int i = 1;
        for (Object o : items) System.out.println("  " + (i++) + ". " + o);
    }

    public void printPriceStatisticsByType() {
        System.out.println("--- Price statistics by type ---");
        Map<String, DoubleSummaryStatistics> stats = new TreeMap<>();
        for (Accommodation a : service.getAccommodations()) {
            stats.computeIfAbsent(a.getClass().getSimpleName(), k -> new DoubleSummaryStatistics())
                 .accept(a.getPricePerNight());
        }
        if (stats.isEmpty()) System.out.println("  (no data)");
        for (Map.Entry<String, DoubleSummaryStatistics> e : stats.entrySet()) {
            DoubleSummaryStatistics s = e.getValue();
            System.out.printf("  %-10s count=%d  min=%.2f  avg=%.2f  max=%.2f%n",
                    e.getKey(), s.getCount(), s.getMin(), s.getAverage(), s.getMax());
        }
    }

    public void printBookingSummary() {
        System.out.println("--- Booking summary ---");
        double revenue = 0;
        for (BookingStatus s : BookingStatus.values()) {
            List<Booking> list = service.findBookingsByStatus(s);
            System.out.println("  " + s + ": " + list.size());
            if (s == BookingStatus.CONFIRMED) {
                for (Booking b : list) revenue += b.getTotalPrice();
            }
        }
        System.out.printf("  Revenue (confirmed): %.2f$%n", revenue);
    }

    public void printGuestSpendingRanking() {
        Map<Guest, Double> spending = new HashMap<>();
        for (Booking b : service.findBookingsByStatus(BookingStatus.CONFIRMED)) {
            spending.merge(b.getGuest(), b.getTotalPrice(), Double::sum);
        }
        List<Map.Entry<Guest, Double>> ranking = new ArrayList<>(spending.entrySet());
        ranking.sort(Map.Entry.<Guest, Double>comparingByValue().reversed());
        System.out.println("--- Guest spending ranking ---");
        if (ranking.isEmpty()) System.out.println("  (no confirmed bookings)");
        int i = 1;
        for (Map.Entry<Guest, Double> e : ranking) {
            System.out.printf("  %d. %s - %.2f$%n", i++, e.getKey(), e.getValue());
        }
    }
}
