package service;

import model.Accommodation;
import model.Reviewable;

import java.util.Comparator;

public final class AccommodationComparators {
    private AccommodationComparators() { }

    private static final Comparator<Accommodation> NATURAL = Comparator.naturalOrder();

    public static double ratingOf(Accommodation a) {
        return a instanceof Reviewable r ? r.getAverageRating() : 0.0;
    }

    public static final Comparator<Accommodation> BY_PRICE =
            Comparator.comparingDouble(Accommodation::getPricePerNight).thenComparing(NATURAL);

    public static final Comparator<Accommodation> BY_RATING_DESC =
            Comparator.<Accommodation>comparingDouble(AccommodationComparators::ratingOf)
                      .reversed().thenComparing(NATURAL);

    public static final Comparator<Accommodation> BY_CITY_PRICE_NAME =
            Comparator.comparing(Accommodation::getCity, String.CASE_INSENSITIVE_ORDER)
                      .thenComparingDouble(Accommodation::getPricePerNight)
                      .thenComparing(NATURAL);
}
