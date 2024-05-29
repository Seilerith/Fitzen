package com.example.lifefit.utils;

public class Validator {
    /**
     * Between 4 and 20 characters
     * Only letters and numbers
     * Not spaces
     */
    public static boolean isUsernameLengthValid(String username) {
        return username.length() >= 4 && username.length() <= 20;
    }

    public static boolean isUsernameCharacterSetValid(String username) {
        String usernamePattern = "^[a-zA-Z0-9]*$";
        return username.matches(usernamePattern);
    }

    public static boolean doesUsernameContainSpaces(String username) {
        return username.contains(" ");
    }

    private static boolean isUsernameValid(String username) {
        String usernamePattern = "^[a-zA-Z0-9]{6,20}$";
        return username.matches(usernamePattern);
    }

    /**
     * Between 6 and 20 characters
     * Only letters, numbers and at least one special character
     * Without spaces
     */
    private static boolean isPasswordValid(String password) {
        String passwordPattern = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=.*[a-zA-Z0-9@#$%^&+=!])([A-Za-z\\d@#$%^&+=!]){8,20}$";
        return password.matches(passwordPattern);
    }

    public static boolean isPasswordLengthValid(String password) {
        return password.length() >= 6 && password.length() <= 20;
    }

    public static boolean isPasswordCharacterSetValid(String password) {
        String passwordPattern = "^[a-zA-Z0-9@#$%^&+=!]*$";
        return password.matches(passwordPattern);
    }

    public static boolean doesPasswordContainSpecialCharacter(String password) {
        String specialCharacterPattern = ".*[@#$%^&+=!].*";
        return password.matches(specialCharacterPattern);
    }

    public static boolean doesPasswordContainSpaces(String password) {
        return password.contains(" ");
    }

    public static boolean isEmailValid(String emailAddress) {
        String uniqueUsernamePattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@";
        String domainNamePattern = "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        String emailPattern = uniqueUsernamePattern + domainNamePattern;
        return emailAddress.matches(emailPattern);
    }

    public static boolean isNameValid(String value) {
        String namePattern = "^[a-zA-Z]*$";
        return value.matches(namePattern);
    }
}
