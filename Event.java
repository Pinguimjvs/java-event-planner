import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

// Classe que representa um evento do sistema
// Os atributos são privados para proteger os dados do objeto.
// O acesso e a alteração devem ser feitos pelos getters e setters,
// permitindo validações e mantendo o encapsulamento.
public class Event {
    private String id; 
    private String title;
    private LocalDate date;  
    private LocalTime time;
    private String location;
    private String description;
    private EventCategory category;
    private int reminderLeadDays;  // Quantos dias antes o lembrete deve aparecer

    // Método privado auxiliar para centralizar a validação (Garante o Encapsulamento)
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("O título do evento não pode ser vazio.");
        }
    }

    // CONSTRUTOR 1: Para eventos com hora marcada
    public Event(String title, LocalDate date, LocalTime time, String location, 
                 String description, EventCategory category, int reminderLeadDays) {
        validateTitle(title);
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
        this.category = category;
        this.reminderLeadDays = reminderLeadDays;
    }

    // CONSTRUTOR 2: Para eventos de Dia Inteiro (Sem horário)
    public Event(String title, LocalDate date, String location, 
                 String description, EventCategory category, int reminderLeadDays) {
        this(title, date, null, location, description, category, reminderLeadDays);
    }

    // --- Getters e Setters ---
    // implementam o encapsulamento, controlando o acesso aos atributos da classe.
    
    public String getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { 
        validateTitle(title);
        this.title = title; 
    }

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

    // Sobrescreve o método toString para retornar uma versão
    // formatada do evento, utilizada na exibição da JList
    @Override
    public String toString() {
        String horaStr = (time != null) ? time.toString() : "Dia Inteiro";
        return "[" + category + "] " + horaStr + " - " + title + " (" + location + ")";
    }
}
