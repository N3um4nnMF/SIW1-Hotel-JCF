package model;

public class Guest {
    private final String passport;
    private String fullName;
    private final boolean vip;

    public Guest(String fullName, String passport) {
        this(fullName, passport, false);
    }

    public Guest(String fullName, String passport, boolean vip) {
        if (passport == null || passport.isBlank())
            throw new IllegalArgumentException("passport cannot be empty");
        this.passport = passport.trim().toUpperCase();
        this.vip = vip;
        setFullName(fullName);
    }

    public String getFullName() { return fullName; }
    public String getPassport() { return passport; }
    public boolean isVip() { return vip; }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("full name cannot be empty");
        if (fullName.contains(";"))
            throw new IllegalArgumentException("full name cannot contain ';'");
        this.fullName = fullName.trim();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Guest g && passport.equals(g.passport));
    }

    @Override
    public int hashCode() { return passport.hashCode(); }

    @Override
    public String toString() {
        return fullName + " (" + passport + ")" + (vip ? " [VIP]" : "");
    }
}
