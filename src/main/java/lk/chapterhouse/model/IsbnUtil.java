package lk.chapterhouse.model;

public final class IsbnUtil {
    private IsbnUtil() {
    }

    public static boolean isValid(String digits) {
        if (digits == null) {
            return false;
        }
        if (digits.length() == 13) {
            return isValidIsbn13(digits);
        }
        if (digits.length() == 10) {
            return isValidIsbn10(digits);
        }
        return false;
    }

    public static boolean isValidIsbn13(String digits) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int d = digits.charAt(i) - '0';
            if (d < 0 || d > 9) {
                return false;
            }
            sum += (i % 2 == 0) ? d : d * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return check == (digits.charAt(12) - '0');
    }

    public static boolean isValidIsbn10(String digits) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int d = digits.charAt(i) - '0';
            if (d < 0 || d > 9) {
                return false;
            }
            sum += (10 - i) * d;
        }
        char last = digits.charAt(9);
        int check = last == 'X' || last == 'x' ? 10 : last - '0';
        if (check < 0 || check > 10) {
            return false;
        }
        sum += check;
        return sum % 11 == 0;
    }
}
