package com.example.application.setupBD;

import com.example.application.entity.Availability;
import com.example.application.entity.Role;
import com.example.application.entity.User;
import com.example.application.repository.AvailabilityRepositoryV2;
import com.example.application.repository.RoleRepository;
import com.example.application.repository.UserRepositoryV2;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {


    @Autowired
    private AvailabilityRepositoryV2 availabilityRepository;

    @Autowired
    private UserRepositoryV2 userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialisation des données

        // Roles
        Role membre = roleRepository.save(new Role(1L, "Membre"));
        Role benevole = roleRepository.save(new Role(2L, "Bénévole"));
        Role administrateur = roleRepository.save(new Role(3L, "Administrateur"));


        PasswordEncoder passwordEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "";
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return false;
            }
        };

        //Long id, String firstName, String lastName, String username, String email, Instant emailVerifiedAt, String status, String password, String phoneNumber, String cellNumber, Boolean isChild, Role role, Instant dateOfBirth
        User userM1 = userRepository.save(new User(1L, "PrenomMembre1", "NomMembre1", "UsernameMembre1", "membre1@email.com", null, "active", passwordEncoder.encode("allo1234"), "1234567890", "1234567890", false, membre, Instant.now()));
        User userM2 = userRepository.save(new User(2L, "PrenomMembre2", "NomMembre2", "UsernameMembre2", "membre2@email.com", null, "active", passwordEncoder.encode("allo1234"), "1234567890", "1234567890", false, membre, Instant.now()));
        User userB1 = userRepository.save(new User(3L, "PrenomBenevole1", "NomBenevole1", "UsernameBenevole1", "Benevole1@email.com", null, "active", passwordEncoder.encode("allo1234"), "1234567890", "1234567890", false, benevole, Instant.now()));
        User userB2 = userRepository.save(new User(4L, "PrenomBenevole2", "NomBenevole2", "UsernameBenevole2", "Benevole2@email.com", null, "active", passwordEncoder.encode("allo1234"), "1234567890", "1234567890", false, benevole, Instant.now()));
        User userA1 = userRepository.save(new User(5L, "PrenomAdministrateur1", "NomAdministrateur1", "UsernameAdministrateur1", "Administrateur1@email.com", null, "active", passwordEncoder.encode("allo1234"), "1234567890", "1234567890", false, administrateur, Instant.now()));
        User userA2 = userRepository.save(new User(6L, "PrenomAdministrateur2", "NomAdministrateur2", "UsernameAdministrateur2", "Administrateur2@email.com", null, "active", passwordEncoder.encode("allo1234"), "1234567890", "1234567890", false, administrateur, Instant.now()));


        LocalDate date = LocalDate.now().minusDays(1);
        LocalTime time = LocalTime.parse("12:00:00");
        // Confirmed, Pending, Cancelled

        LocalDate currentDate = LocalDate.now(); // ZoneId.of("America/Montreal")
        LocalDate endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        LocalDate endmOfMonthOver1 = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).plusDays(1);

        availabilityRepository.save(new Availability(1L, userB1 , "titre1",  currentDate, time, 480, "details", "réunion", "Confirmed"));
        availabilityRepository.save(new Availability(2L, userB1 , "titre2",  currentDate, time, 480, "details", "heureOuverture", "Confirmed"));
        availabilityRepository.save(new Availability(3L, userB1 , "titre3",  currentDate, time, 480, "details", "fermeture", "Confirmed"));

        availabilityRepository.save(new Availability(4L, userB1 , "titre4",  currentDate, time, 480, "details", "réunion", "Pending"));
        availabilityRepository.save(new Availability(5L, userB1 , "titre5",  currentDate, time, 480, "details", "heureOuverture", "Pending"));
        availabilityRepository.save(new Availability(6L, userB1 , "titre6",  currentDate, time, 480, "details", "fermeture", "Pending"));

        availabilityRepository.save(new Availability(7L, userB1 , "titre7",  currentDate, time, 480, "details", "réunion", "Cancelled"));
        availabilityRepository.save(new Availability(8L, userB1 , "titre8",  currentDate, time, 480, "details", "heureOuverture", "Cancelled"));
        availabilityRepository.save(new Availability(9L, userB1 , "titre9",  currentDate, time, 480, "details", "fermeture", "Cancelled"));

        availabilityRepository.save(new Availability(10L, userB1 , "titre10",  date, time, 480, "details", "réunion", "Confirmed"));
        availabilityRepository.save(new Availability(11L, userB1 , "titre11",  date, time, 480, "details", "heureOuverture", "Confirmed"));
        availabilityRepository.save(new Availability(12L, userB1 , "titre12",  date, time, 480, "details", "fermeture", "Confirmed"));

        availabilityRepository.save(new Availability(13L, userB1 , "titre10",  endOfMonth, time, 480, "details", "heureOuverture", "Confirmed"));
        availabilityRepository.save(new Availability(14L, userB1 , "titre11",  endOfMonth, time, 480, "details", "heureOuverture", "Confirmed"));
        availabilityRepository.save(new Availability(15L, userB1 , "titre12",  endOfMonth, time, 480, "details", "heureOuverture", "Confirmed"));

        availabilityRepository.save(new Availability(16L, userB1 , "titre10",  endmOfMonthOver1, time, 480, "details", "heureOuverture", "Confirmed"));
        availabilityRepository.save(new Availability(17L, userB1 , "titre11",  endmOfMonthOver1, time, 480, "details", "heureOuverture", "Confirmed"));
        availabilityRepository.save(new Availability(18L, userB1 , "titre12",  endmOfMonthOver1, time, 480, "details", "heureOuverture", "Confirmed"));
        // Initialisation des données
    }
}
