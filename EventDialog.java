import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * A modal dialog used for both Creating and Editing events.
 * Handles user input validation and object instantiation.
 */
public class EventDialog extends JDialog {

    private JTextField titleField;
    private JTextField dateField;
    private JTextField timeField;
    private JTextField locationField;
    private JTextArea descriptionArea;
    private JComboBox<EventCategory> categoryComboBox;
    private JSpinner reminderSpinner;

    private boolean confirmed = false;
    private Event resultEvent = null;
    private Event eventToEdit; // Will be null if creating a new event

    /**
     * Constructor for the Event Dialog.
     * @param parent The main application frame to block while this dialog is open.
     * @param eventToEdit The event to populate fields with (pass null for a new event).
     * @param defaultDate The default date to show in the date field if creating a new event.
     */
    public EventDialog(Frame parent, Event eventToEdit, LocalDate defaultDate) {
        super(parent, eventToEdit == null ? "Add New Event" : "Edit Event", true);
        this.eventToEdit = eventToEdit;
        
        setupUI();
        
        if (eventToEdit != null) {
            populateFields(eventToEdit);
        } else {
            // Default date to the selected date on the calendar if creating a new event
            dateField.setText(defaultDate.toString());
            timeField.setText(LocalTime.now().withSecond(0).withNano(0).toString());
        }
        
        pack();
        setLocationRelativeTo(parent);
    }

    private void setupUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // --- Initialize Components ---
        titleField = new JTextField(20);
        dateField = new JTextField(10);
        dateField.setToolTipText("Format: YYYY-MM-DD");
        timeField = new JTextField(10);
        timeField.setToolTipText("Format: HH:MM");
        locationField = new JTextField(20);
        
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        
        categoryComboBox = new JComboBox<>(EventCategory.values());
        
        // Spinner for Reminder Lead Days (0 to 30 days)
        reminderSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 30, 1));

        // --- Add Components to Form ---
        int row = 0;
        addFormRow(formPanel, gbc, "Title:", titleField, row++);
        addFormRow(formPanel, gbc, "Date (YYYY-MM-DD):", dateField, row++);
        addFormRow(formPanel, gbc, "Time (HH:MM):", timeField, row++);
        addFormRow(formPanel, gbc, "Location:", locationField, row++);
        addFormRow(formPanel, gbc, "Category:", categoryComboBox, row++);
        addFormRow(formPanel, gbc, "Reminder (Days Before):", reminderSpinner, row++);
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(descScroll, gbc);
        row++;

        // --- Buttons Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> attemptSave());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        // Add to Dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String label, Component comp, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(comp, gbc);
    }

    private void populateFields(Event event) {
        titleField.setText(event.getTitle());
        dateField.setText(event.getDate().toString());
        timeField.setText(event.getTime().toString());
        locationField.setText(event.getLocation());
        descriptionArea.setText(event.getDescription());
        categoryComboBox.setSelectedItem(event.getCategory());
        reminderSpinner.setValue(event.getReminderLeadDays());
    }

    /**
     * Validates input fields and creates/updates the event if valid.
     * Handles exceptions to prevent the application from crashing.
     */
    private void attemptSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(dateField.getText().trim());
            time = LocalTime.parse(timeField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date or time format.\nUse YYYY-MM-DD for date and HH:MM for time.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String location = locationField.getText().trim();
        String description = descriptionArea.getText().trim();
        EventCategory category = (EventCategory) categoryComboBox.getSelectedItem();
        int reminderDays = (int) reminderSpinner.getValue();

        if (eventToEdit == null) {
            // Create a brand new event
            resultEvent = new Event(title, date, time, location, description, category, reminderDays);
        } else {
            // Update the existing event references
            eventToEdit.setTitle(title);
            eventToEdit.setDate(date);
            eventToEdit.setTime(time);
            eventToEdit.setLocation(location);
            eventToEdit.setDescription(description);
            eventToEdit.setCategory(category);
            eventToEdit.setReminderLeadDays(reminderDays);
            resultEvent = eventToEdit;
        }

        confirmed = true;
        dispose(); // Close the dialog
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Event getResultEvent() {
        return resultEvent;
    }
}