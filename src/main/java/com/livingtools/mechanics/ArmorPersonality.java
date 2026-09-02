package com.livingtools.mechanics;

public enum ArmorPersonality {
    STOIC("Estoica", "Soporta el dolor en silencio."),
    COWARDLY("Cobarde", "Te hace correr cuando estás herido."),
    MASOCHISTIC("Masoquista", "Disfruta del dolor, a veces te cura."),
    VENGEFUL("Vengativa", "Devuelve parte del daño recibido."),
    GUARDIAN("Guardiana", "Provoca a los enemigos para proteger a otros."),
    THORNED("Espinosa", "Refleja el 50% del daño recibido."),
    GHOSTLY("Fantasmal", "Probabilidad de esquivar ataques.");

    private final String displayName;
    private final String description;

    ArmorPersonality(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
