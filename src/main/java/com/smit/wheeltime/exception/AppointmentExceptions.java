package com.smit.wheeltime.exception;

public class AppointmentExceptions {

    public static void throwInvalidWorkshopException(String message) {
        throw new AppointmentException(message);
    }

    public static void throwBookingException(String message) {
        throw new AppointmentException(message);
    }

    public static class AppointmentException extends RuntimeException {
        public AppointmentException(String message) {
            super(message);
        }
    }
}
