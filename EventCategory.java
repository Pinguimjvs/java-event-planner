// Representam as categorias possíveis para os eventos
public enum EventCategory {
    MEETING("Meeting"),
    BIRTHDAY("Birthday"),
    APPOINTMENT("Appointment"),
    OTHER("Other");

    // final garante que o valor de displayName não pode ser alterado depois de definido,
    // mantendo as categorias consistentes e seguras.
    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
