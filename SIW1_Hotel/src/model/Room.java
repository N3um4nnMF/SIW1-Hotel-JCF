package model;

public class Room {
    private final int number;
    private final String type;
    private final double price;

    public Room(int number, String type, double price) {
        if (number <= 0) throw new IllegalArgumentException("room number must be positive");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("room type cannot be empty");
        if (price <= 0) throw new IllegalArgumentException("room price must be positive");
        this.number = number;
        this.type = type;
        this.price = price;
    }

    public int getNumber() { return number; }
    public String getType() { return type; }
    public double getPrice() { return price; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Room r && number == r.number);
    }

    @Override
    public int hashCode() { return Integer.hashCode(number); }

    @Override
    public String toString() {
        return "room " + number + " (" + type + ") - " + price + "$";
    }
}
