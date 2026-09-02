package com.livingtools.mechanics;

public enum Personality {
    NORMAL("Normal", "Una herramienta común y corriente."),
    GLUTTONOUS("Glotona", "A veces se come los items que minas."),
    HEROIC("Heroica", "Brilla con valentía en combate."),
    LAZY("Perezosa", "A veces se niega a trabajar."),
    LUCKY("Afortunada", "A veces encuentra cosas extra."),
    SARCASTIC("Sarcástica", "Siempre tiene un comentario ácido."),
    SHY("Tímida", "Se asusta fácilmente."),
    AGGRESSIVE("Agresiva", "Quiere destruir todo.");

    private final String displayName;
    private final String description;

    Personality(String displayName, String description) {
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
