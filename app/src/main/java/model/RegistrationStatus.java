package model;

public enum RegistrationStatus {
    /*
     * Kiedy użytkownik ma kaktywną sesję z innym tokenem i innym device id,
     */
    NEW_DEVICE,


    EMAIL_NOT_FOUND,
    DEVICE_MISMATCH,
    TOKEN_MISMATCH,

    /*
     * Sprawdzenie w AppClientService czy nie została przekroczona ilość prób rejestracji w okreslonym czasie
     */
    ALLOWED_REGISTRATION_ATTEMPS_EXCEEDED,

    /*
     * Kiedy użytkownik ma aktywną sesję na tym urządzeniu
     */
    ALREADY_OK,

    ACTIVATION_SENT,

    UNKNOWN_ERROR
}
