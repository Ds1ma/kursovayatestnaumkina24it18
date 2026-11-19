package password;
public enum PasswordStrength {
    WEAK("Слабый", "#D32F2F"),
    MEDIUM("Средний", "#F57C00"),
    STRONG("Сильный", "#388E3C");

    private final String label;
    private final String color;

    PasswordStrength(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() { return label; }
    public String getColor() { return color; }
}