package de.lacertis.loreutils.data;

import java.util.List;
import com.google.gson.annotations.SerializedName;
import de.lacertis.loreutils.pathway.PathwayElement;

public class Pathway {
    @SerializedName("id")
    private String id;

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("elements")
    private List<PathwayElement> elements;

    public Pathway() {
    }

    public Pathway(String id, boolean enabled, List<PathwayElement> elements) {
        this.id = id;
        this.enabled = enabled;
        this.elements = elements;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<PathwayElement> getElements() {
        return elements;
    }

    public void setElements(List<PathwayElement> elements) {
        this.elements = elements;
    }
}