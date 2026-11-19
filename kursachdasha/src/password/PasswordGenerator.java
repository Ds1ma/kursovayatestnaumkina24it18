package password;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {

    public static final int MIN_LENGTH = 4;
    public static final int MAX_LENGTH = 64;

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS    = "0123456789";
    private static final String SPECIAL   = "!@#$%^&*()-_=+[]{};:,.<>?";

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() { /* утилитный класс */ }

    public static String generate(int length, boolean useLower, boolean useUpper,
                                  boolean useDigits, boolean useSpecial) {
        // 1. Обработка «пустого ввода»
        if (length <= 0) {
            throw new IllegalArgumentException("Длина пароля не может быть пустой или равной 0");
        }
        // 2. Диапазон
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Длина пароля должна быть от " + MIN_LENGTH + " до " + MAX_LENGTH + " символов");
        }
        // 3. Наборы символов
        List<String> sets = new ArrayList<>();
        if (useLower)  sets.add(LOWERCASE);
        if (useUpper)  sets.add(UPPERCASE);
        if (useDigits) sets.add(DIGITS);
        if (useSpecial) sets.add(SPECIAL);

        if (sets.isEmpty()) {
            throw new IllegalArgumentException("Выберите хотя бы один набор символов");
        }

        // Гарантируем по одному символу из каждого выбранного набора
        StringBuilder password = new StringBuilder(length);
        for (String set : sets) {
            password.append(set.charAt(RANDOM.nextInt(set.length())));
        }

        // Добиваем до нужной длины
        String allChars = String.join("", sets);
        while (password.length() < length) {
            password.append(allChars.charAt(RANDOM.nextInt(allChars.length())));
        }

        // Перемешиваем
        List<Character> chars = new ArrayList<>();
        for (char c : password.toString().toCharArray()) chars.add(c);
        Collections.shuffle(chars, RANDOM);

        StringBuilder result = new StringBuilder(length);
        for (char c : chars) result.append(c);
        return result.toString();
    }

    public static PasswordStrength estimateStrength(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return PasswordStrength.WEAK;
        }
        boolean hasLower   = password.matches(".*[a-z].*");
        boolean hasUpper   = password.matches(".*[A-Z].*");
        boolean hasDigit   = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");

        int types = (hasLower ? 1 : 0) + (hasUpper ? 1 : 0)
                + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);

        if (password.length() >= 12 && types >= 3) return PasswordStrength.STRONG;
        if (password.length() >= 8  && types >= 2) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }
}