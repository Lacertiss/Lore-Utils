package de.lacertis.client;

public enum PlayerArea {
    NONE,
    LIGHTS_OUT,
    ANUAR_GEM;

    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}