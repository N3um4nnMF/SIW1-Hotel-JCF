package service;

import model.Guest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GuestFileStorage {
    private final Path file;

    public GuestFileStorage(String path) {
        this.file = Path.of(path);
    }

    public List<Guest> load() {
        List<Guest> result = new ArrayList<>();
        if (!Files.exists(file)) return result;
        try {
            for (String line : Files.readAllLines(file)) {
                if (line.isBlank()) continue;
                String[] parts = line.split(";");
                try {
                    result.add(new Guest(parts[1], parts[0], Boolean.parseBoolean(parts[2])));
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("skipped invalid line in " + file + ": " + line);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + file, e);
        }
        return result;
    }

    public void save(Collection<Guest> guests) {
        List<String> lines = new ArrayList<>();
        for (Guest g : guests) {
            lines.add(g.getPassport() + ";" + g.getFullName() + ";" + g.isVip());
        }
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.write(file, lines);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write " + file, e);
        }
    }

    public Path getPath() { return file; }
}
