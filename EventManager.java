import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private List<Event> events;
    private static final String DATA_FILE = "events_data.txt";
    private static final String DELIMITER = "\\|"; 
    private static final String WRITE_DELIMITER = "|";

    public EventManager() {
        this.events = new ArrayList<>();
    }

    // --- Operações CRUD Tradicionais ---
    public void addEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Não é possível adicionar um evento nulo.");
        }
        events.add(event);
    }

    public void deleteEvent(Event event) {
        events.remove(event);
    }

    public List<Event> getAllEvents() {
        return events;
    }

    // Filtragem usando laço 'for'
    public List<Event> getEventsByDate(LocalDate date) {
        List<Event> dayEvents = new ArrayList<>();
        for (Event e : events) {
            if (e.getDate().equals(date)) {
                dayEvents.add(e);
            }
        }
        return dayEvents;
    }

    // --- Lembretes de Inicialização ---
    public List<Event> getUpcomingReminders() {
        LocalDate today = LocalDate.now();
        List<Event> upcoming = new ArrayList<>();

        for (Event e : events) {
            LocalDate reminderDate = e.getDate().minusDays(e.getReminderLeadDays());
            if (reminderDate.equals(today)) {
                upcoming.add(e);
            }
        }
        return upcoming;
    }

    // --- Persistência de Arquivos (I/O) ---
    public void saveToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Event e : events) {
                String horaStr = (e.getTime() != null) ? e.getTime().toString() : "null";
                
                String line = String.join(WRITE_DELIMITER,
                        e.getId(),
                        e.getTitle(),
                        e.getDate().toString(),
                        horaStr,
                        e.getLocation(),
                        e.getDescription().replace("\n", " "), 
                        e.getCategory().name(),
                        String.valueOf(e.getReminderLeadDays())
                );
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public void loadFromFile() throws IOException {
        events.clear();
        File file = new File(DATA_FILE);
        
        if (!file.exists()) {
            return; 
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(DELIMITER);
                
                if (parts.length == 8) {
                    try {
                        String title = parts[1];
                        LocalDate date = LocalDate.parse(parts[2]);
                        
                        // Reconstrói tratando se o horário era nulo (Dia Inteiro)
                        LocalTime time = null;
                        if (!parts[3].equals("null")) {
                            time = LocalTime.parse(parts[3]);
                        }
                        
                        String location = parts[4];
                        String description = parts[5];
                        EventCategory category = EventCategory.valueOf(parts[6]);
                        int reminderDays = Integer.parseInt(parts[7]);

                        Event event;
                        if (time != null) {
                            event = new Event(title, date, time, location, description, category, reminderDays);
                        } else {
                            event = new Event(title, date, location, description, category, reminderDays);
                        }
                        
                        events.add(event);
                    } catch (DateTimeParseException | IllegalArgumentException e) {
                        System.err.println("Warning: Linha corrompida ignorada no arquivo de dados.");
                    }
                }
            }
        }
    }
}
