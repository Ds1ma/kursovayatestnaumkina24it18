package password;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

public class PasswordGeneratorApp extends JFrame {

    private static final Color BG      = new Color(0xF5F5F5);
    private static final Color PRIMARY = new Color(0x1976D2);
    private static final Color SUCCESS = new Color(0x388E3C);

    private final JTextField lengthField;
    private final JCheckBox  cbLower, cbUpper, cbDigits, cbSpecial;
    private final JLabel     passwordLabel;
    private final JLabel     strengthLabel;
    private final JTextArea  historyArea;
    private final LinkedList<String> history = new LinkedList<>();

    public PasswordGeneratorApp() {
        super("Генератор паролей");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 720);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Заголовок
        JLabel title = new JLabel("🔐 Генератор паролей");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(16));

        // Длина
        root.add(makeLabel("Длина пароля (4–64):"));
        lengthField = new JTextField("12");
        lengthField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        root.add(lengthField);
        root.add(Box.createVerticalStrut(8));

        // Чекбоксы
        cbLower   = new JCheckBox("Строчные буквы (a-z)", true);
        cbUpper   = new JCheckBox("Прописные буквы (A-Z)", true);
        cbDigits  = new JCheckBox("Цифры (0-9)", true);
        cbSpecial = new JCheckBox("Спецсимволы (!@#$...)", false);
        for (JCheckBox cb : new JCheckBox[]{cbLower, cbUpper, cbDigits, cbSpecial}) {
            cb.setBackground(BG);
            root.add(cb);
        }
        root.add(Box.createVerticalStrut(12));

        // Кнопка генерации
        JButton generateBtn = makeButton("Сгенерировать пароль", PRIMARY);
        generateBtn.addActionListener(this::onGenerate);
        root.add(generateBtn);
        root.add(Box.createVerticalStrut(12));

        // Вывод пароля
        root.add(makeLabel("Ваш пароль:"));
        passwordLabel = new JLabel("—");
        passwordLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(passwordLabel);
        root.add(Box.createVerticalStrut(6));

        // Надёжность
        strengthLabel = new JLabel(" ");
        strengthLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        strengthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(strengthLabel);
        root.add(Box.createVerticalStrut(8));

        // Копирование
        JButton copyBtn = makeButton("📋 Копировать", SUCCESS);
        copyBtn.addActionListener(this::onCopy);
        root.add(copyBtn);
        root.add(Box.createVerticalStrut(16));

        // История
        root.add(makeLabel("История (последние 5):"));
        historyArea = new JTextArea(5, 20);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(historyArea);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        root.add(scroll);

        add(root);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton makeButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return b;
    }

    // ==== Главный обработчик — здесь вся обработка исключений ====
    private void onGenerate(ActionEvent e) {
        try {
            String input = lengthField.getText();

            // 1. Пустой ввод
            if (input == null || input.trim().isEmpty()) {
                throw new IllegalArgumentException("Поле длины не может быть пустым");
            }

            // 2. Нечисловой ввод → NumberFormatException
            int length = Integer.parseInt(input.trim());

            // 3. Некорректная длина / не выбрано ни одного набора → IllegalArgumentException
            String password = PasswordGenerator.generate(
                    length,
                    cbLower.isSelected(),
                    cbUpper.isSelected(),
                    cbDigits.isSelected(),
                    cbSpecial.isSelected()
            );

            // Вывод
            passwordLabel.setText(password);

            // Оценка надёжности
            PasswordStrength strength = PasswordGenerator.estimateStrength(password);
            strengthLabel.setText("Надёжность: " + strength.getLabel());
            strengthLabel.setForeground(Color.decode(strength.getColor()));

            // История
            history.addFirst(password);
            while (history.size() > 5) history.removeLast();
            updateHistory();

        } catch (NumberFormatException ex) {
            showError("Неверный формат", "Введите целое число в поле длины");
            lengthField.requestFocus();
        } catch (IllegalArgumentException ex) {
            showError("Ошибка", ex.getMessage());
            lengthField.requestFocus();
        } catch (Exception ex) {
            showError("Неизвестная ошибка", ex.getMessage());
        }
    }

    private void onCopy(ActionEvent e) {
        String pwd = passwordLabel.getText();
        if (pwd == null || "—".equals(pwd) || pwd.trim().isEmpty()) {
            showError("Ошибка", "Сначала сгенерируйте пароль");
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(pwd), null);
        JOptionPane.showMessageDialog(this,
                "Пароль скопирован в буфер обмена",
                "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateHistory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            sb.append(i + 1).append(". ").append(history.get(i)).append('\n');
        }
        historyArea.setText(sb.toString().trim());
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { /* Л&F не критичен */ }
            new PasswordGeneratorApp().setVisible(true);
        });
    }
}