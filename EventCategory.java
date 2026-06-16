/**
 * Represents the allowed categories for an event.
 */
public enum EventCategory {
    MEETING("Meeting"),
    BIRTHDAY("Birthday"),
    APPOINTMENT("Appointment"),
    OTHER("Other");

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}