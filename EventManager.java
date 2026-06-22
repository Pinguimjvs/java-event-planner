import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

// Classe responsável por gerenciar todos os eventos do sistema
// Faz operações de adicionar, remover, filtrar e salvar/carregar dados
public class EventManager {
    private List<Event> events;
    private static final String DATA_FILE = "events_data.txt";
    private static final String DELIMITER = "\\|"; 
    private static final String WRITE_DELIMITER = "|";

    public EventManager() {
        this.events = new ArrayList<>();
    }

    // Adiciona um novo evento na lista
    public void addEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Não é possível adicionar um evento nulo.");
        }
        events.add(event);
    }
    // Remove um evento da lista
    public void deleteEvent(Event event) {
        events.remove(event);
    }
    // Retorna todos os eventos cadastrados
    public List<Event> getAllEvents() {
        return events;
    }

   // Retorna apenas os eventos de uma data específica
    public List<Event> getEventsByDate(LocalDate date) {
        List<Event> dayEvents = new ArrayList<>();
        for (Event e : events) {
            if (e.getDate().equals(date)) {
                dayEvents.add(e);
            }
        }
        return dayEvents;
    }

    // Retorna eventos que estão próximos de disparar lembrete
    public List<Event> getUpcomingReminders() {
        LocalDate today = LocalDate.now();
        List<Event> upcoming = new ArrayList<>();

        for (Event e : events) {
            // Calcula a data em que o lembrete deve aparecer
            LocalDate reminderDate = e.getDate().minusDays(e.getReminderLeadDays());
            // Se a data do lembrete for hoje, adiciona na lista
            if (reminderDate.equals(today)) {
                upcoming.add(e);
            }
        }
        return upcoming;
    }

    // Persistência de Arquivos (I/O) - salva todos os eventos em um arquivo de texto
    public void saveToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Event e : events) {
                // Converte horário para texto (ou "null" se não existir)
                String horaStr = (e.getTime() != null) ? e.getTime().toString() : "null";

                 // Converte o objeto Event em uma linha de texto separada por "|"
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
                // Escreve a linha no arquivo
                writer.write(line);
                writer.newLine();
            }
        }
    }
    // Carrega os eventos salvos no arquivo de texto
    public void loadFromFile() throws IOException {
        
        // Limpa a lista antes de carregar novos dados
        events.clear();
        File file = new File(DATA_FILE);

        // Se o arquivo não existir, não faz nada
        if (!file.exists()) {
            return; 
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            // Lê o arquivo linha por linha
            while ((line = reader.readLine()) != null) {
                // Ignora linhas vazias
                if (line.trim().isEmpty()) continue;

                // Divide a linha em partes usando o separador "|"
                String[] parts = line.split(DELIMITER);

                // Garante que a linha tem todos os campos esperados
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
                        // Cria o evento dependendo se tem horário ou não
                        if (time != null) {
                            event = new Event(title, date, time, location, description, category, reminderDays);
                        } else {
                            event = new Event(title, date, location, description, category, reminderDays);
                        }
                        // Adiciona o evento na lista
                        events.add(event);
                    } catch (DateTimeParseException | IllegalArgumentException e) {
                        // Ignora linhas corrompidas para não quebrar o programa
                        System.err.println("Warning: Linha corrompida ignorada no arquivo de dados.");
                    }
                }
            }
        }
    }
}
