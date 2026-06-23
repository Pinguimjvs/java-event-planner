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
 * RELATÓRIO DO PROJETO - SCC0504 (Versão Compacta)
 * -----------------------------------------------
 * Aplicação: Java Event Planner
 *
 * Conceitos de POO aplicados:
 * - Encapsulamento: a classe Event protege seus atributos e expõe acesso controlado.
 * - Separação de responsabilidades (MVC simplificado):
 *   EventManager cuida da lógica e persistência,
 *   enquanto a GUI cuida apenas da interface.
 * - Uso de Enum: EventCategory garante categorias fixas e seguras.
 *
 * Principais desafios:
 * - Tratamento de arquivos corrompidos sem travar o sistema.
 * - Atualização dinâmica da interface Swing.
 * - Construção de calendário dinâmico baseado em datas reais.
 */

public class EventPlannerGUI extends JFrame {

    // Gerenciador responsável por armazenar e manipular eventos
    private EventManager eventManager;

    // Mês atualmente exibido no calendário
    private YearMonth currentMonth;

    // Data selecionada pelo usuário no calendário
    private LocalDate selectedDate;

    // Painel onde o calendário é desenhado
    private JPanel calendarGridPanel;

    // Label que mostra mês e ano atual
    private JLabel monthYearLabel;

    // Lista gráfica de eventos do dia selecionado
    private DefaultListModel<Event> eventListModel;
    private JList<Event> eventList;

    // Label que mostra a data selecionada
    private JLabel selectedDateLabel;

    // Barra inferior de status do sistema
    private JLabel statusBar;

    public EventPlannerGUI(EventManager eventManager) {

        this.eventManager = eventManager;

        // Inicializa com o mês atual
        this.currentMonth = YearMonth.now();

        // Inicializa com o dia atual selecionado
        this.selectedDate = LocalDate.now();

        setupWindow();
        initComponents();
        renderCalendar();
        updateEventList();

        // Mostra lembretes ao iniciar o sistema
        showStartupReminders();

        updateStatus("Sistema pronto. Eventos carregados: " +
                eventManager.getAllEvents().size());
    }

    // Configuração básica da janela principal
    private void setupWindow() {
        setTitle("Java Event Planner");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    // Inicializa todos os componentes da interface
    private void initComponents() {

        // ---------------- PAINEL ESQUERDO (CALENDÁRIO) ----------------
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Cabeçalho do calendário (mês/ano + botões)
        JPanel calendarHeader = new JPanel(new BorderLayout());

        JButton prevBtn = new JButton("◄ Prev");
        JButton nextBtn = new JButton("Next ►");

        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Volta um mês no calendário
        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            renderCalendar();
        });

        // Avança um mês no calendário
        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            renderCalendar();
        });

        JPanel navPanel = new JPanel(new FlowLayout());
        navPanel.add(prevBtn);
        navPanel.add(nextBtn);

        calendarHeader.add(monthYearLabel, BorderLayout.CENTER);
        calendarHeader.add(navPanel, BorderLayout.EAST);

        // Grade do calendário (7 colunas para os dias da semana)
        calendarGridPanel = new JPanel(new GridLayout(0, 7, 5, 5));

        leftPanel.add(calendarHeader, BorderLayout.NORTH);
        leftPanel.add(calendarGridPanel, BorderLayout.CENTER);

        // ---------------- PAINEL DIREITO (EVENTOS) ----------------
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Label da data selecionada
        selectedDateLabel = new JLabel("", SwingConstants.LEFT);
        selectedDateLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Lista de eventos do dia
        eventListModel = new DefaultListModel<>();
        eventList = new JList<>(eventListModel);

        JScrollPane listScrollPane = new JScrollPane(eventList);

        // Painel de ações (CRUD)
        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 5, 0));

        JButton addBtn = new JButton("Adicionar");
        JButton editBtn = new JButton("Editar");
        JButton deleteBtn = new JButton("Deletar");

        // ---------------- BOTÃO ADD ----------------
        addBtn.addActionListener(e -> {

            // Abre diálogo para criar evento
            EventDialog dialog = new EventDialog(this, null, selectedDate);
            dialog.setVisible(true);

            // Se usuário confirmou, adiciona evento
            if (dialog.isConfirmed()) {
                eventManager.addEvent(dialog.getResultEvent());
                renderCalendar();
                updateEventList();
                saveDataSafely();
            }
        });

        // ---------------- BOTÃO EDIT ----------------
        editBtn.addActionListener(e -> {

            Event selected = eventList.getSelectedValue();

            // Validação: precisa selecionar evento
            if (selected == null) {
                JOptionPane.showMessageDialog(this,
                        "Selecione um evento para editar.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Abre diálogo com evento preenchido
            EventDialog dialog = new EventDialog(this, selected, selected.getDate());
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                renderCalendar();
                updateEventList();
                saveDataSafely();
            }
        });

        // ---------------- BOTÃO DELETE ----------------
        deleteBtn.addActionListener(e -> {

            Event selected = eventList.getSelectedValue();

            if (selected == null) {
                JOptionPane.showMessageDialog(this,
                        "Selecione um evento para excluir.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Confirmação antes de apagar
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Excluir '" + selected.getTitle() + "'?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION);

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

        // Divide tela em calendário (esquerda) e eventos (direita)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel);

        splitPane.setDividerLocation(500);

        // Barra de status inferior
        statusBar = new JLabel(" ");

        add(splitPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    // ---------------- RENDERIZA O CALENDÁRIO ----------------
    private void renderCalendar() {

        calendarGridPanel.removeAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthYearLabel.setText(currentMonth.format(formatter).toUpperCase());

        // Cabeçalho dos dias da semana
        String[] daysOfWeek = {"DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB"};
        for (String day : daysOfWeek) {
            calendarGridPanel.add(new JLabel(day, SwingConstants.CENTER));
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);

        // Ajusta posição do primeiro dia no calendário
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue();
        int blanks = (dayOfWeekValue == 7) ? 0 : dayOfWeekValue;

        // Espaços vazios antes do início do mês
        for (int i = 0; i < blanks; i++) {
            calendarGridPanel.add(new JLabel(""));
        }

        int daysInMonth = currentMonth.lengthOfMonth();

        // Cria botões para cada dia do mês
        for (int day = 1; day <= daysInMonth; day++) {

            LocalDate date = currentMonth.atDay(day);
            JButton dayBtn = new JButton();

            List<Event> eventsOnThisDay = eventManager.getEventsByDate(date);

            // Destaca dias com eventos
            if (!eventsOnThisDay.isEmpty()) {
                dayBtn.setText(day + " (" + eventsOnThisDay.size() + "*)");
                dayBtn.setBackground(new Color(220, 235, 250));
            } else {
                dayBtn.setText(String.valueOf(day));
                dayBtn.setBackground(Color.WHITE);
            }

            // Destaca dia selecionado
            if (date.equals(selectedDate)) {
                dayBtn.setBackground(Color.YELLOW);
            }

            // Ao clicar em um dia, atualiza seleção
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

    // Atualiza lista de eventos do dia selecionado
    private void updateEventList() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        eventListModel.clear();

        List<Event> dailyEvents = eventManager.getEventsByDate(selectedDate);

        if (dailyEvents.isEmpty()) {
            selectedDateLabel.setText("Sem eventos em " +
                    selectedDate.format(formatter));
        } else {
            selectedDateLabel.setText("Agenda de " +
                    selectedDate.format(formatter));
        }

        for (Event e : dailyEvents) {
            eventListModel.addElement(e);
        }
    }

    // Atualiza barra de status
    private void updateStatus(String message) {
        statusBar.setText(" " + message);
    }

    // Mostra lembretes ao iniciar o sistema
    private void showStartupReminders() {

        List<Event> reminders = eventManager.getUpcomingReminders();

        if (!reminders.isEmpty()) {

            StringBuilder msg = new StringBuilder("⏰ Lembretes próximos:\n\n");

            for (Event e : reminders) {
                msg.append(" - ").append(e.getTitle()).append("\n");
            }

            JOptionPane.showMessageDialog(this,
                    msg.toString(),
                    "Lembretes",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Salva dados com tratamento de erro
    private void saveDataSafely() {

        try {
            eventManager.saveToFile();
            updateStatus("Dados salvos com sucesso.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Ponto de entrada do programa
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            EventManager em = new EventManager();

            try {
                em.loadFromFile();
            } catch (Exception ex) {
                System.err.println("Nenhum arquivo de dados encontrado.");
            }

            new EventPlannerGUI(em).setVisible(true);
        });
    }
}
