package de.lacertis.loreutils.pathway;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import de.lacertis.loreutils.data.Pathway;

public class PathwayBuilder {
    private String id;
    private List<PathwayElement> elements = new ArrayList<>();

    public PathwayBuilder() {
    }

    public PathwayBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public PathwayBuilder addElement(PathwayElement element) {
        elements.add(element);
        return this;
    }

    public Optional<Pathway> build() {
        if (id == null || id.isBlank() || elements.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Pathway(id, true, elements));
    }
}