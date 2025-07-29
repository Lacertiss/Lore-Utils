package de.lacertis.loreutils;

public enum PlayerArea {
    LIGHTS_OUT,
    ANUAR_GEM,
    INGENUITY;

    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}