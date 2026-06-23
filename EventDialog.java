import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Janela de diálogo usada para criar e editar eventos.
 * Responsável por coletar os dados do usuário e validar as informações.
 */
public class EventDialog extends JDialog {

    private JTextField titleField;
    private JTextField dateField;
    private JTextField timeField;
    private JTextField locationField;
    private JTextArea descriptionArea;
    private JComboBox<EventCategory> categoryComboBox;
    private JSpinner reminderSpinner;

    private boolean confirmed = false; // Indica se o usuário confirmou a ação (salvar)
    private Event resultEvent = null; // Evento final gerado ou atualizado
    private Event eventToEdit; // Evento que está sendo editado (null se for criação)

    /**
     * Construtor da janela de evento.
     * parent: janela principal (bloqueia enquanto este diálogo estiver aberto)
     * eventToEdit: evento existente para edição (ou null para criar novo)
     * defaultDate: data padrão quando for criação de novo evento
     */
    public EventDialog(Frame parent, Event eventToEdit, LocalDate defaultDate) {
        super(parent, eventToEdit == null ? "Add New Event" : "Edit Event", true);
        this.eventToEdit = eventToEdit;

        // Monta toda a interface
        setupUI();

        // Se estiver editando, preenche os campos
        if (eventToEdit != null) {
            populateFields(eventToEdit);
        } else {
             // Se for novo evento, define data e hora padrão
            dateField.setText(defaultDate.toString());
            timeField.setText(LocalTime.now().withSecond(0).withNano(0).toString());
        }

        // Ajusta tamanho automaticamente e centraliza a janela
        pack();
        setLocationRelativeTo(parent);
    }

    // Monta toda a interface gráfica do formulário
    private void setupUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Inicialização dos componentes do formulário
        titleField = new JTextField(20);
        dateField = new JTextField(10);
        dateField.setToolTipText("Format: YYYY-MM-DD");
        timeField = new JTextField(10);
        timeField.setToolTipText("Format: HH:MM");
        locationField = new JTextField(20);
        
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true); // Quebra linha automaticamente
        descriptionArea.setWrapStyleWord(true); // Evita cortar palavras
        
        // Faz o campo de descrição ter barra de rolagem, permitindo escrever 
        // textos longos sem perder conteúdo na interface.
        JScrollPane descScroll = new JScrollPane(descriptionArea); 

        // Cria um menu suspenso preenchido com todas as categorias do enum EventCategory
        categoryComboBox = new JComboBox<>(EventCategory.values());
        
        // Controle numérico para definir dias de lembrete (0 a 30)
        reminderSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 30, 1));

        // -Adiciona os campos na tela
        int row = 0;
        addFormRow(formPanel, gbc, "Título:", titleField, row++);
        addFormRow(formPanel, gbc, "Data (YYYY-MM-DD):", dateField, row++);
        addFormRow(formPanel, gbc, "Horário (HH:MM):", timeField, row++);
        addFormRow(formPanel, gbc, "Local:", locationField, row++);
        addFormRow(formPanel, gbc, "Categoria:", categoryComboBox, row++);
        addFormRow(formPanel, gbc, "Lembrete (Dias Antes):", reminderSpinner, row++);

        // Campo de descrição ocupa mais espaço
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(descScroll, gbc);
        row++;

        // Painel de botões
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
    
    // Preenche os campos com os dados de um evento existente
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
    * Valida os dados e cria ou atualiza o evento.
    * Evita que valores inválidos quebrem o programa.
    */
    private void attemptSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O título não pode ficar em branco.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        LocalTime time;
        // Validação de data e hora
        try {
            date = LocalDate.parse(dateField.getText().trim());
            time = LocalTime.parse(timeField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato inválido de data ou horário.\nUse YYYY-MM-DD para data e HH:MM para horário.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String location = locationField.getText().trim();
        String description = descriptionArea.getText().trim();
        EventCategory category = (EventCategory) categoryComboBox.getSelectedItem();
        int reminderDays = (int) reminderSpinner.getValue();

        if (eventToEdit == null) {
            // Se não há evento, cria novo
            resultEvent = new Event(title, date, time, location, description, category, reminderDays);
        } else {
            // Caso exista, edita os campos do evento
            eventToEdit.setTitle(title);
            eventToEdit.setDate(date);
            eventToEdit.setTime(time);
            eventToEdit.setLocation(location);
            eventToEdit.setDescription(description);
            eventToEdit.setCategory(category);
            eventToEdit.setReminderLeadDays(reminderDays);
            resultEvent = eventToEdit;
        }
        // Marca como confirmado e fecha a janela
        confirmed = true;
        dispose(); 
    }
    // Indica se o usuário salvou o evento
    public boolean isConfirmed() {
        return confirmed;
    }
    // Retorna o evento criado ou editado
    public Event getResultEvent() {
        return resultEvent;
    }
}
