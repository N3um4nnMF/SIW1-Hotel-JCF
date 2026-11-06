package model;

public abstract class Accommodation implements Comparable<Accommodation>, Bookable {
    private final String id;
    private String name;
    private String city;
    private double pricePerNight;
    private int capacity;
    private boolean booked;

    public Accommodation(String id, String name, String city, double pricePerNight, int capacity) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("id cannot be empty");
        this.id = id.trim();
        setName(name);
        setCity(city);
        setPricePerNight(pricePerNight);
        setCapacity(capacity);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public double getPricePerNight() { return pricePerNight; }
    public int getCapacity() { return capacity; }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name cannot be empty");
        this.name = name;
    }

    public void setCity(String city) {
        if (city == null || city.isBlank())
            throw new IllegalArgumentException("city cannot be empty");
        this.city = city;
    }

    public void setPricePerNight(double pricePerNight) {
        if (pricePerNight <= 0)
            throw new IllegalArgumentException("price must be positive");
        this.pricePerNight = pricePerNight;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
    }

    @Override
    public boolean book(int nights) {
        if (nights <= 0)
            throw new IllegalArgumentException("nights must be positive");
        if (booked) return false;
        booked = true;
        return true;
    }

    @Override
    public void cancelBooking() { booked = false; }

    @Override
    public boolean isBooked() { return booked; }

    public void displayInfo() {
        System.out.printf("[%s] %s in %s | %.2f$/night | up to %d guests%n",
                id, name, city, pricePerNight, capacity);
    }

    public abstract double calculateTotalPrice(int nights);

    public abstract String getRoomType();

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Accommodation a && id.equals(a.id));
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public int compareTo(Accommodation other) {
        int byName = name.compareToIgnoreCase(other.name);
        return byName != 0 ? byName : id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s (%s, %.2f$)", id, getClass().getSimpleName(), name, city, pricePerNight);
    }
}
