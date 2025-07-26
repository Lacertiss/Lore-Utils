package de.lacertis.loreutils.pathway;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import de.lacertis.loreutils.data.Pathway;

public class PathwayConfig {
    private List<Pathway> pathways = new ArrayList<>();

    public PathwayConfig() {
    }

    public List<Pathway> getPathways() {
        return pathways;
    }

    public void add(Pathway pathway) {
        pathways.add(pathway);
    }

    public void remove(String id) {
        pathways.removeIf(p -> p.getId().equals(id));
    }

    public Optional<Pathway> get(String id) {
        return pathways.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }
}