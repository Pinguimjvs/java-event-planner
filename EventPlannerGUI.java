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

    // UI Components
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
        setTitle("Java Event Planner - Ultra Premium Edition");
        setSize(1000, 650); // Maior para respirar melhor
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE); // Fundo limpo
    }

    private void initComponents() {
        // --- LEFT PANEL: THE CALENDAR ---
        JPanel leftPanel = new JPanel(new BorderLayout(15, 15));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 10));

        // Modern Header for Left Panel
        JLabel leftHeader = new JLabel("📅 Monthly Calendar");
        leftHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftHeader.setForeground(new Color(30, 30, 30));

        JPanel calendarHeader = new JPanel(new BorderLayout());
        calendarHeader.setBackground(Color.WHITE);
        
        JButton prevBtn = createFlatButton("◄ Prev", new Color(240, 240, 240), Color.DARK_GRAY);
        JButton nextBtn = createFlatButton("Next ►", new Color(240, 240, 240), Color.DARK_GRAY);
        JButton todayBtn = createFlatButton("Today", new Color(220, 235, 255), new Color(0, 102, 204));
        todayBtn.setToolTipText("Jump to current date");

        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        monthYearLabel.setForeground(new Color(60, 60, 60));

        prevBtn.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); renderCalendar(); });
        nextBtn.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); renderCalendar(); });
        todayBtn.addActionListener(e -> { 
            currentMonth = YearMonth.now(); 
            selectedDate = LocalDate.now(); 
            renderCalendar(); 
            updateEventList(); 
        });

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        navPanel.setBackground(Color.WHITE);
        navPanel.add(prevBtn);
        navPanel.add(todayBtn);
        navPanel.add(nextBtn);

        calendarHeader.add(leftHeader, BorderLayout.NORTH);
        calendarHeader.add(Box.createVerticalStrut(15), BorderLayout.CENTER); // Spacing
        
        JPanel monthNavWrapper = new JPanel(new BorderLayout());
        monthNavWrapper.setBackground(Color.WHITE);
        monthNavWrapper.add(monthYearLabel, BorderLayout.CENTER);
        monthNavWrapper.add(navPanel, BorderLayout.SOUTH);
        calendarHeader.add(monthNavWrapper, BorderLayout.SOUTH);

        calendarGridPanel = new JPanel(new GridLayout(0, 7, 8, 8)); // Maior respiro na grade
        calendarGridPanel.setBackground(Color.WHITE);
        
        leftPanel.add(calendarHeader, BorderLayout.NORTH);
        leftPanel.add(calendarGridPanel, BorderLayout.CENTER);

        // --- RIGHT PANEL: THE EVENT LIST ---
        JPanel rightPanel = new JPanel(new BorderLayout(15, 15));
        rightPanel.setBackground(new Color(250, 250, 252)); // Cinza ultra leve para contrastar com o branco
        rightPanel.setBorder(new EmptyBorder(20, 10, 20, 20));

        // Modern Header for Right Panel
        JLabel rightHeader = new JLabel("📋 Daily Agenda");
        rightHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        rightHeader.setForeground(new Color(30, 30, 30));

        selectedDateLabel = new JLabel("", SwingConstants.LEFT);
        selectedDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectedDateLabel.setForeground(new Color(0, 102, 204));
        
        JPanel rightHeaderWrapper = new JPanel(new BorderLayout());
        rightHeaderWrapper.setBackground(new Color(250, 250, 252));
        rightHeaderWrapper.add(rightHeader, BorderLayout.NORTH);
        rightHeaderWrapper.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        rightHeaderWrapper.add(selectedDateLabel, BorderLayout.SOUTH);

        eventListModel = new DefaultListModel<>();
        eventList = new JList<>(eventListModel);
        eventList.setBackground(new Color(250, 250, 252));
        eventList.setSelectionBackground(new Color(230, 240, 255)); 
        eventList.setSelectionForeground(Color.BLACK);
        
        // MODERN CARD RENDERER: Transforma a lista num design de cartões com Emojis
        eventList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Event) {
                    Event ev = (Event) value;
                    label.setBorder(new EmptyBorder(12, 10, 12, 10)); // Padding do cartão
                    
                    String icon = "📌";
                    String hexColor = "#555555";
                    switch (ev.getCategory()) {
                        case MEETING: icon = "💼"; hexColor = "#0066CC"; break;
                        case BIRTHDAY: icon = "🎂"; hexColor = "#CC0066"; break;
                        case APPOINTMENT: icon = "🩺"; hexColor = "#009933"; break;
                    }
                    
                    // Renderização HTML para o visual de "Card"
                    label.setText("<html><div style='width: 250px;'>" +
                                  "<b style='color:" + hexColor + "; font-size: 11px;'>" + icon + " " + ev.getCategory() + "</b><br>" +
                                  "<span style='font-size: 14px; color: #333333;'><b>" + ev.getTime() + "</b> - " + ev.getTitle() + "</span>" +
                                  "</div></html>");
                }
                return label;
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(eventList);
        listScrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230))); // Borda sutil

        // Action Buttons (Solid Colors)
        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        actionPanel.setBackground(new Color(250, 250, 252));
        
        JButton addBtn = createFlatButton("➕ Add", new Color(0, 120, 215), Color.WHITE);
        JButton editBtn = createFlatButton("✏️ Edit", new Color(240, 140, 0), Color.WHITE);
        JButton deleteBtn = createFlatButton("🗑️ Delete", new Color(220, 50, 50), Color.WHITE);

        addBtn.addActionListener(e -> {
            EventDialog dialog = new EventDialog(this, null, selectedDate);
            dialog.setVisible(true); 
            if (dialog.isConfirmed()) {
                eventManager.addEvent(dialog.getResultEvent());
                renderCalendar();
                updateEventList();
                saveDataSafely(); 
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

        rightPanel.add(rightHeaderWrapper, BorderLayout.NORTH);
        rightPanel.add(listScrollPane, BorderLayout.CENTER);
        rightPanel.add(actionPanel, BorderLayout.SOUTH);

        // SPLIT PANE: Permite redimensionar as áreas
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(600); // Espaço inicial
        splitPane.setDividerSize(3); // Linha super fina e moderna
        splitPane.setBorder(null);

        // --- STATUS BAR ---
        statusBar = new JLabel(" ");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.setForeground(new Color(120, 120, 120));
        statusBar.setBorder(new EmptyBorder(5, 10, 5, 10));

        add(splitPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Helper to create modern flat buttons without borders.
     */
    private JButton createFlatButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private void renderCalendar() {
        calendarGridPanel.removeAll(); 

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthYearLabel.setText(currentMonth.format(formatter).toUpperCase());

        String[] daysOfWeek = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (String day : daysOfWeek) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            dayLabel.setForeground(new Color(150, 150, 150));
            calendarGridPanel.add(dayLabel);
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
            dayBtn.setFocusPainted(false);
            dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            dayBtn.setBackground(new Color(250, 250, 250)); // Cinza ultra claro

            List<Event> eventsOnThisDay = eventManager.getEventsByDate(date);
            
            if (!eventsOnThisDay.isEmpty()) {
                dayBtn.setBackground(new Color(230, 245, 255)); 
                dayBtn.setBorder(BorderFactory.createLineBorder(new Color(150, 200, 255), 1));
                dayBtn.setText("<html><center><b><font size='5' color='#1E1E1E'>" + day + "</font></b><br><font size='3' color='#0066CC'>• " + eventsOnThisDay.size() + "</font></center></html>");
            } else {
                dayBtn.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240), 1));
                dayBtn.setText("<html><center><b><font size='5' color='#555555'>" + day + "</font></b><br><font size='3'>&nbsp;</font></center></html>");
            }

            if (date.equals(selectedDate)) {
                dayBtn.setBackground(new Color(255, 240, 180)); 
                dayBtn.setBorder(BorderFactory.createLineBorder(new Color(230, 180, 0), 2));
            }

            // Usability: Hover Effect
            dayBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!date.equals(selectedDate)) {
                        dayBtn.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!date.equals(selectedDate)) {
                        if (!eventsOnThisDay.isEmpty()) {
                            dayBtn.setBorder(BorderFactory.createLineBorder(new Color(150, 200, 255), 1));
                        } else {
                            dayBtn.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240), 1));
                        }
                    }
                }
            });

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd"); // Formato mais legível ex: "Monday, Jun 25"
        eventListModel.clear();
        List<Event> dailyEvents = eventManager.getEventsByDate(selectedDate);
        
        if (dailyEvents.isEmpty()) {
            selectedDateLabel.setText("No events for " + selectedDate.format(formatter));
            selectedDateLabel.setForeground(new Color(150, 150, 150));
        } else {
            selectedDateLabel.setText("Agenda for " + selectedDate.format(formatter));
            selectedDateLabel.setForeground(new Color(50, 50, 50));
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
            StringBuilder msg = new StringBuilder("⏰ Upcoming events in the next 24 hours:\n\n");
            for (Event e : reminders) {
                msg.append(" • ").append(e.getTitle()).append(" (").append(e.getTime()).append(")\n");
            }
            JOptionPane.showMessageDialog(this, msg.toString(), "Daily Reminders", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveDataSafely() {
        try {
            eventManager.saveToFile();
            updateStatus("Data saved successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            updateStatus("Error saving data.");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback
        }

        SwingUtilities.invokeLater(() -> {
            EventManager em = new EventManager();
            try {
                em.loadFromFile();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Could not load data.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            new EventPlannerGUI(em).setVisible(true);
        });
    }
}