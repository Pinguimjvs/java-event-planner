import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Represents a calendar event with its associated details and reminder settings.
 * Fully encapsulated to adhere to strict OOP principles.
 */
public class Event {
    // Unique identifier for editing and deleting specific events
    private String id; 
    
    private String title;
    private LocalDate date;
    private LocalTime time;
    private String location;
    private String description;
    private EventCategory category;
    
    // Represents how many days before the event the reminder should trigger
    private int reminderLeadDays; 

    /**
     * Constructor for creating a new Event.
     */
    public Event(String title, LocalDate date, LocalTime time, String location, 
                 String description, EventCategory category, int reminderLeadDays) {
        this.id = UUID.randomUUID().toString(); // Auto-generate a unique ID
        this.title = title;
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
        this.category = category;
        this.reminderLeadDays = reminderLeadDays;
    }

    // --- Getters and Setters (Encapsulation) ---

    public String getId() { return id; }
    // Note: No setter for ID to maintain data integrity.

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }

    public int getReminderLeadDays() { return reminderLeadDays; }
    public void setReminderLeadDays(int reminderLeadDays) { this.reminderLeadDays = reminderLeadDays; }

    @Override
    public String toString() {
        return time.toString() + " - " + title + " (" + category.toString() + ")";
    }
}