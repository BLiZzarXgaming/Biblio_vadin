package com.example.application.utils;

public class StatusUtils {

    // Item status constants
    public static final class ItemStatus {
        public static final String AVAILABLE = "AVAILABLE";
        public static final String BORROWED = "BORROWED";
        public static final String RESERVED = "RESERVED";
        public static final String UNAVAILABLE = "UNAVAILABLE";
        public static final String LOST = "LOST";
        public static final String DELETED = "DELETED";

        public static String toFrench(String status) {
            switch (status) {
                case AVAILABLE: return "Disponible";
                case BORROWED: return "Emprunté";
                case RESERVED: return "Réservé";
                case UNAVAILABLE: return "Indisponible";
                case LOST: return "Perdu";
                case DELETED: return "Supprimé";
                default: return status;
            }
        }
    }

    // Loan status constants
    public static final class LoanStatus {
        public static final String BORROWED = "BORROWED";
        public static final String RETURNED = "RETURNED";
        public static final String OVERDUE = "OVERDUE";
        public static final String CANCELED = "CANCELED";
        public static final String LOST = "LOST";

        public static String toFrench(String status) {
            switch (status) {
                case BORROWED: return "Emprunté";
                case RETURNED: return "Retourné";
                case OVERDUE: return "En retard";
                case CANCELED: return "Annulé";
                case LOST: return "Perdu";
                default: return status;
            }
        }

        public static final String EMPRUNTE = "Emprunté";
        public static final String RETOURNE = "Retourné";
        public static final String RETARD = "En retard";
        public static final String ANNULE = "Annulé";
        public static final String PERDU = "Perdu";
    }

    public static final class SpecialLimit {
        public static final String ACTIVE = "ACTIVE";
        public static final String INACTIVE = "INACTIVE";

        public static String toFrench(String status) {
            switch (status) {
                case ACTIVE: return "Actif";
                case INACTIVE: return "Inactif";
                default: return status;
            }
        }
    }

    // Reservation status constants
    public static final class ReservationStatus {
        public static final String PENDING = "PENDING";
        public static final String READY = "READY";
        public static final String DONE = "DONE";
        public static final String CANCELLED = "CANCELLED";

        public static String toFrench(String status) {
            switch (status) {
                case PENDING: return "En attente";
                case READY: return "Prêt";
                case CANCELLED: return "Annulée";
                case DONE: return "Terminée";
                default: return status;
            }
        }
    }

    // User status constants
    public static final class UserStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String SUSPENDED = "SUSPENDED";
        public static final String BLOCKED = "BLOCKED";

        public static String toFrench(String status) {
            switch (status) {
                case ACTIVE: return "Actif";
                case SUSPENDED: return "Suspendu";
                case BLOCKED: return "Bloqué";
                default: return status;
            }
        }
    }

    // Document types with translation support
    public static final class DocTypes {
        public static final String BOOK = "book";
        public static final String MAGAZINE = "magazine";
        public static final String BOARD_GAME = "board_game";

        public static String toFrench(String type) {
            switch (type) {
                case BOOK: return "Livre";
                case MAGAZINE: return "Revue";
                case BOARD_GAME: return "Jeu";
                default: return type;
            }
        }

        public static final String LIVRE = "Livre";
        public static final String REVUE = "Revue";
        public static final String JEU = "Jeu";
    }

    public static final class RoleName {
        public static final String ADMIN = "Administrateur";
        public static final String BENEVOLE = "Bénévole";
        public static final String MEMBRE = "Membre";
    }

    public static final class AvailabilityType {
        public static final String EVENT = "EVENT";
        public static final String HEUREOUVERTURE = "HEUREOUVERTURE";
    }

    public static final class AvailabilityStatus {
        public static final String CONFIRMED = "CONFIRMED";
        public static final String DRAFT = "DRAFT";
        public static final String CANCELED = "CANCELED";
    }

    // Private constructor to prevent instantiation
    private StatusUtils() {
        // Utility class should not be instantiated
    }
}
