import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PROJECT REPORT - SCC0504 (Compact Version)
 * -----------------------------------------
 * Application: Java Event Planner
 * * OOP Concepts Applied:
 * - Encapsulation: The Event model strictly hides its fields and exposes only getters/setters.
 * - Separation of Concerns (MVC Pattern): EventManager acts as the Controller/Persistence layer, 
 * ensuring the GUI (View) does not directly handle file I/O or business logic.
 * - Enums: EventCategory is used to enforce type safety for event types.
 * * Challenges Solved:
 * - Handling missing or corrupted data files gracefully without crashing the application.
 * - Managing Java Swing's single-threaded nature by ensuring I/O happens safely and 
 * the UI is updated reactively.
 * - Creating a dynamic calendar grid that recalculates blanks and highlights dates based 
 * on the EventManager's state.
 */

public class EventPlannerGUI extends JFrame {

    private EventManager eventManager;
    private YearMonth currentMonth;
    private LocalDate selectedDate;

    private JPanel calendarGridPanel;
    private JLabel monthYearLabel;
    private DefaultListModel<Event> eventListModel;
    private JList<Event> eventList;
    private JLabel selectedDateLabel;
    private JLabel statusBar;

    public EventPlannerGUI(EventManager eventManager) {
        this.eventManager = eventManager;
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();

        setupWindow();
        initComponents();
        renderCalendar();
        updateEventList();
        
        showStartupReminders();
        updateStatus("System ready. Loaded " + eventManager.getAllEvents().size() + " events.");
    }

    private void setupWindow() {
        setTitle("Java Event Planner");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        // --- PAINEL ESQUERDO: CALENDÁRIO ---
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel calendarHeader = new JPanel(new BorderLayout());
        JButton prevBtn = new JButton("◄ Prev");
        JButton nextBtn = new JButton("Next ►");
        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 16));

        prevBtn.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); renderCalendar(); });
        nextBtn.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); renderCalendar(); });

        JPanel navPanel = new JPanel(new FlowLayout());
        navPanel.add(prevBtn);
        navPanel.add(nextBtn);

        calendarHeader.add(monthYearLabel, BorderLayout.CENTER);
        calendarHeader.add(navPanel, BorderLayout.EAST);

        calendarGridPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        leftPanel.add(calendarHeader, BorderLayout.NORTH);
        leftPanel.add(calendarGridPanel, BorderLayout.CENTER);

        // --- PAINEL DIREITO: LISTA DE EVENTOS ---
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        selectedDateLabel = new JLabel("", SwingConstants.LEFT);
        selectedDateLabel.setFont(new Font("Arial", Font.BOLD, 14));

        eventListModel = new DefaultListModel<>();
        eventList = new JList<>(eventListModel);
        

        JScrollPane listScrollPane = new JScrollPane(eventList);

        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");

        addBtn.addActionListener(e -> {
            EventDialog dialog = new EventDialog(this, null, selectedDate);
            dialog.setVisible(true); 
            if (dialog.isConfirmed()) {
                eventManager.addEvent(dialog.getResultEvent());
                renderCalendar();
                updateEventList();
                saveDataSafely(); 
            }
        });

        editBtn.addActionListener(e -> {
            Event selected = eventList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select an event to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            EventDialog dialog = new EventDialog(this, selected, selected.getDate());
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                renderCalendar();
                updateEventList();
                saveDataSafely();
            }
        });

        deleteBtn.addActionListener(e -> {
            Event selected = eventList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select an event to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete '" + selected.getTitle() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                eventManager.deleteEvent(selected);
                renderCalendar();
                updateEventList();
                saveDataSafely();
            }
        });

        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);

        rightPanel.add(selectedDateLabel, BorderLayout.NORTH);
        rightPanel.add(listScrollPane, BorderLayout.CENTER);
        rightPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(500);

        statusBar = new JLabel(" ");
        add(splitPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void renderCalendar() {
        calendarGridPanel.removeAll(); 

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthYearLabel.setText(currentMonth.format(formatter).toUpperCase());

        String[] daysOfWeek = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (String day : daysOfWeek) {
            calendarGridPanel.add(new JLabel(day, SwingConstants.CENTER));
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue(); 
        int blanks = (dayOfWeekValue == 7) ? 0 : dayOfWeekValue; 

        for (int i = 0; i < blanks; i++) {
            calendarGridPanel.add(new JLabel("")); 
        }

        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            JButton dayBtn = new JButton();
            
            List<Event> eventsOnThisDay = eventManager.getEventsByDate(date);
            
            if (!eventsOnThisDay.isEmpty()) {
                dayBtn.setText(day + " (" + eventsOnThisDay.size() + "*)");
                dayBtn.setBackground(new Color(220, 235, 250)); // Destaca com fundo azul claro 
            } else {
                dayBtn.setText(String.valueOf(day));
                dayBtn.setBackground(Color.WHITE);
            }

            if (date.equals(selectedDate)) {
                dayBtn.setBackground(Color.YELLOW); // Amarelo simples para o dia selecionado
            }

            dayBtn.addActionListener(e -> {
                selectedDate = date;
                renderCalendar(); 
                updateEventList(); 
            });

            calendarGridPanel.add(dayBtn);
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }

    private void updateEventList() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        eventListModel.clear();
        List<Event> dailyEvents = eventManager.getEventsByDate(selectedDate);
        
        if (dailyEvents.isEmpty()) {
            selectedDateLabel.setText("No events for " + selectedDate.format(formatter));
        } else {
            selectedDateLabel.setText("Agenda for " + selectedDate.format(formatter));
        }

        for (Event e : dailyEvents) {
            eventListModel.addElement(e);
        }
    }

    private void updateStatus(String message) {
        statusBar.setText(" " + message);
    }

    private void showStartupReminders() {
        List<Event> reminders = eventManager.getUpcomingReminders();
        if (!reminders.isEmpty()) {
            StringBuilder msg = new StringBuilder("⏰ Reminders for the next 24 hours:\n\n");
            for (Event e : reminders) {
                msg.append(" - ").append(e.getTitle()).append("\n");
            }
            JOptionPane.showMessageDialog(this, msg.toString(), "Daily Reminders", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveDataSafely() {
        try {
            eventManager.saveToFile();
            updateStatus("Data saved successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EventManager em = new EventManager();
            try {
                em.loadFromFile();
            } catch (Exception ex) {
                System.err.println("Nenhum arquivo de dados carregado.");
            }
            new EventPlannerGUI(em).setVisible(true);
        });
    }
}
