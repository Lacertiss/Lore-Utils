package de.lacertis.client;

public enum PlayerArea {
    NONE,
    LIGHTS_OUT,
    ANUAR_GEM,
    SPIRIT_MAZE_1;

    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}