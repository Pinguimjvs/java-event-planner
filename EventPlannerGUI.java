import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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

    // UI Components
    private JPanel calendarGridPanel;
    private JLabel monthYearLabel;
    private DefaultListModel<Event> eventListModel;
    private JList<Event> eventList;
    private JLabel selectedDateLabel;
    private JLabel statusBar; // NEW: Status bar for real-time user feedback

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
        setTitle("Java Event Planner - Premium Edition");
        setSize(950, 600); // Slightly larger to accommodate the status bar and HTML buttons
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        
        // Adds a nice padding around the whole window
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 0, 10)); 
    }

    private void initComponents() {
        // --- LEFT PANEL: THE CALENDAR ---
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Monthly Calendar"));

        // Calendar Header
        JPanel calendarHeader = new JPanel(new BorderLayout());
        JButton prevBtn = new JButton("◄ Prev");
        JButton nextBtn = new JButton("Next ►");
        
        // BONUS: "Today" Button
        JButton todayBtn = new JButton("Today");
        todayBtn.setToolTipText("Jump to current date");
        todayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        prevBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        monthYearLabel.setForeground(new Color(40, 40, 40));

        prevBtn.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); renderCalendar(); });
        nextBtn.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); renderCalendar(); });
        todayBtn.addActionListener(e -> { 
            currentMonth = YearMonth.now(); 
            selectedDate = LocalDate.now(); 
            renderCalendar(); 
            updateEventList(); 
        });

        JPanel navPanel = new JPanel(new FlowLayout());
        navPanel.add(prevBtn);
        navPanel.add(todayBtn);
        navPanel.add(nextBtn);

        calendarHeader.add(monthYearLabel, BorderLayout.CENTER);
        calendarHeader.add(navPanel, BorderLayout.SOUTH);

        calendarGridPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        leftPanel.add(calendarHeader, BorderLayout.NORTH);
        leftPanel.add(calendarGridPanel, BorderLayout.CENTER);

        // --- RIGHT PANEL: THE EVENT LIST ---
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Daily Agenda"));
        rightPanel.setPreferredSize(new Dimension(380, 0));

        selectedDateLabel = new JLabel("", SwingConstants.CENTER);
        selectedDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        selectedDateLabel.setForeground(new Color(0, 102, 204));

        eventListModel = new DefaultListModel<>();
        eventList = new JList<>(eventListModel);
        eventList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        eventList.setSelectionBackground(new Color(0, 120, 215)); // Modern Windows blue
        eventList.setSelectionForeground(Color.WHITE);
        
        // BONUS: Color-coded categories inside the JList
        eventList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Event && !isSelected) {
                    Event ev = (Event) value;
                    switch (ev.getCategory()) {
                        case MEETING: c.setForeground(new Color(0, 102, 204)); break;
                        case BIRTHDAY: c.setForeground(new Color(204, 0, 102)); break;
                        case APPOINTMENT: c.setForeground(new Color(0, 153, 51)); break;
                        default: c.setForeground(Color.DARK_GRAY); break;
                    }
                }
                return c;
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(eventList);

        // Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        JButton addBtn = new JButton("➕ Add");
        JButton editBtn = new JButton("✏️ Edit");
        JButton deleteBtn = new JButton("🗑️ Delete");

        addBtn.setToolTipText("Schedule a new event for the selected date");
        editBtn.setToolTipText("Modify the details of the selected event");
        deleteBtn.setToolTipText("Permanently remove the selected event");
        
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Envia a data selecionada do calendário para o formulário
        addBtn.addActionListener(e -> {
            EventDialog dialog = new EventDialog(this, null, selectedDate);
            dialog.setVisible(true); // App pauses here until dialog is closed
            
            if (dialog.isConfirmed()) {
                eventManager.addEvent(dialog.getResultEvent());
                renderCalendar();
                updateEventList();
                saveDataSafely(); // Automatically save to file
                updateStatus("Event added successfully.");
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
                updateStatus("Event updated successfully.");
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
                updateStatus("Event deleted.");
            }
        });

        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);

        rightPanel.add(selectedDateLabel, BorderLayout.NORTH);
        rightPanel.add(listScrollPane, BorderLayout.CENTER);
        rightPanel.add(actionPanel, BorderLayout.SOUTH);

        // --- STATUS BAR ---
        statusBar = new JLabel(" ");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.setForeground(Color.GRAY);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Renders the calendar grid for the currentMonth.
     */
    private void renderCalendar() {
        calendarGridPanel.removeAll(); // Clear previous buttons

        // Update Header
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthYearLabel.setText(currentMonth.format(formatter).toUpperCase());

        // Add Day of Week labels
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : daysOfWeek) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            calendarGridPanel.add(dayLabel);
        }

        // Calculate blanks before the 1st of the month
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue(); // 1(Mon) to 7(Sun)
        int blanks = (dayOfWeekValue == 7) ? 0 : dayOfWeekValue; 

        for (int i = 0; i < blanks; i++) {
            calendarGridPanel.add(new JLabel("")); // Empty placeholder
        }

        // Add buttons for each day of the month
        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            JButton dayBtn = new JButton();
            dayBtn.setFocusPainted(false);
            dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            dayBtn.setBackground(Color.WHITE);

            // SMART CELLS LOGIC: Render HTML to show dots/counters for events
            List<Event> eventsOnThisDay = eventManager.getEventsByDate(date);
            
            if (!eventsOnThisDay.isEmpty()) {
                dayBtn.setBackground(new Color(230, 245, 255)); // Soft Blue
                dayBtn.setBorder(BorderFactory.createLineBorder(new Color(150, 200, 255), 1));
                // HTML rendering to show the day number and an event counter below it
                dayBtn.setText("<html><center><b><font size='4'>" + day + "</font></b><br><font size='2' color='#0066CC'>• " + eventsOnThisDay.size() + "</font></center></html>");
            } else {
                // Non-breaking space (&nbsp;) ensures all buttons have the exact same height even if empty
                dayBtn.setText("<html><center><b><font size='4'>" + day + "</font></b><br><font size='2'>&nbsp;</font></center></html>");
            }

            if (date.equals(selectedDate)) {
                dayBtn.setBackground(new Color(255, 225, 100)); // Distinct Gold for selected
                dayBtn.setBorder(BorderFactory.createLineBorder(new Color(200, 150, 0), 2));
            }

            // Click listener
            dayBtn.addActionListener(e -> {
                selectedDate = date;
                renderCalendar(); // Re-render to update the highlight colors
                updateEventList(); // Update the right panel
            });

            calendarGridPanel.add(dayBtn);
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }

    /**
     * Updates the right panel to show events for the currently selected date.
     */
    private void updateEventList() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        eventListModel.clear();
        List<Event> dailyEvents = eventManager.getEventsByDate(selectedDate);
        
        // Dynamic Empty State Feedback
        if (dailyEvents.isEmpty()) {
            selectedDateLabel.setText("No events for " + selectedDate.format(formatter));
            selectedDateLabel.setForeground(Color.GRAY);
        } else {
            String plural = dailyEvents.size() == 1 ? "Event" : "Events";
            selectedDateLabel.setText(dailyEvents.size() + " " + plural + " for " + selectedDate.format(formatter));
            selectedDateLabel.setForeground(new Color(0, 102, 204));
        }

        for (Event e : dailyEvents) {
            eventListModel.addElement(e);
        }
    }

    /**
     * Updates the text in the status bar.
     */
    private void updateStatus(String message) {
        statusBar.setText(" " + message);
    }

    /**
     * Checks for upcoming reminders on application startup.
     */
    private void showStartupReminders() {
        List<Event> reminders = eventManager.getUpcomingReminders();
        if (!reminders.isEmpty()) {
            StringBuilder msg = new StringBuilder("⏰ Upcoming events in the next 24 hours:\n\n");
            for (Event e : reminders) {
                msg.append(" • ").append(e.getTitle()).append(" (").append(e.getTime()).append(")\n");
            }
            JOptionPane.showMessageDialog(this, msg.toString(), "Daily Reminders", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Attempts to save data and shows an error dialog if it fails.
     */
    private void saveDataSafely() {
        try {
            eventManager.saveToFile();
            updateStatus("Data saved successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            updateStatus("Error saving data.");
        }
    }

    // --- MAIN METHOD FOR TESTING ---
    public static void main(String[] args) {
        // PERFUME: Sets the application to look like a modern native OS app instead of default Java
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Silently fallback to default if system theme fails
        }

        // Run application smoothly inside the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            EventManager em = new EventManager();
            
            try {
                em.loadFromFile();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Could not load data.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            
            // Starts completely empty if there is no previous save file, as requested for the demo!
            new EventPlannerGUI(em).setVisible(true);
        });
    }
}