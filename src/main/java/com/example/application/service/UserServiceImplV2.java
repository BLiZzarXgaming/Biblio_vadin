package com.example.application.service;

import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.Mapper.UserMapper;
import com.example.application.repository.UserRepositoryV2;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.utils.DateUtils;
import com.example.application.utils.StatusUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserServiceImplV2 implements UserServiceV2 {
    private static final Logger LOGGER = Logger.getLogger(UserServiceImplV2.class.getName());

    private final UserRepositoryV2 userRepository;
    private final UserMapper userMapper;

    public UserServiceImplV2(UserRepositoryV2 userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toDto);
    }

    @Override
    public UserDto save(UserDto user) {
        return userMapper.toDto(userRepository.save(userMapper.toEntity(user)));
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDto findUserByUsernameAndPassword(String username) {
        return userMapper.toDto(userRepository.findUserByUsernameAndPassword(username));
    }

    // Implémentation des méthodes de statistiques avec utilisation réelle des
    // repositories

    @Override
    public Map<String, Long> countUsersByRole() {
        Map<String, Long> usersByRole = new HashMap<>();
        List<UserDto> allUsers = findAll();

        // Compter le nombre d'utilisateurs par rôle
        usersByRole.put("Membres", allUsers.stream()
                .filter(user -> user.getRole() != null && StatusUtils.RoleName.MEMBRE.equals(user.getRole().getName()))
                .count());

        usersByRole.put("Bénévoles", allUsers.stream()
                .filter(user -> user.getRole() != null && StatusUtils.RoleName.BENEVOLE.equals(user.getRole().getName()))
                .count());

        usersByRole.put("Administrateurs", allUsers.stream()
                .filter(user -> user.getRole() != null && StatusUtils.RoleName.BENEVOLE.equals(user.getRole().getName()))
                .count());

        return usersByRole;
    }

    @Override
    public int countTotalUsers() {
        return (int) userRepository.count();
    }

    @Override
    public int countNewUsersThisMonth() {
        try {
            // Récupérer les données du mois courant
            Instant firstDayOfMonth = LocalDate.now().withDayOfMonth(1)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant();
            return (int) userRepository.countUsersCreatedSince(firstDayOfMonth.toEpochMilli());
        } catch (Exception e) {
            LOGGER.warning("Erreur lors du comptage des nouveaux utilisateurs: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public double calculateActiveUsersPercentage() {
        try {
            int totalUsers = countTotalUsers();
            if (totalUsers == 0)
                return 0.0;

            // Récupérer les utilisateurs actifs depuis un an
            Instant oneYearAgo = LocalDate.now().minusYears(1)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant();
            long activeUsers = userRepository.countActiveUsersSince(oneYearAgo.toEpochMilli());

            return (double) activeUsers / totalUsers * 100;
        } catch (Exception e) {
            LOGGER.warning("Erreur lors du calcul du pourcentage d'utilisateurs actifs: " + e.getMessage());
            return 0.0;
        }
    }

    @Override
    public String getMostActiveUser() {
        try {
            // Récupérer les données d'un an en arrière
            Instant oneYearAgo = LocalDate.now().minusYears(1)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant();
            List<Object[]> results = userRepository.findMostActiveUsersSince(oneYearAgo.toEpochMilli());

            if (results != null && !results.isEmpty()) {
                Object[] mostActive = results.get(0);
                if (mostActive.length >= 3) {
                    // Extraire les informations: [username, firstName, lastName, loanCount]
                    String firstName = (mostActive[1] != null) ? mostActive[1].toString() : "";
                    String lastName = (mostActive[2] != null) ? mostActive[2].toString() : "";

                    long loanCount = 0;
                    if (mostActive.length >= 4 && mostActive[3] != null) {
                        loanCount = ((Number) mostActive[3]).longValue();
                    }

                    return firstName + " " + lastName + " (" + loanCount + " emprunts)";
                } else if (mostActive.length >= 1 && mostActive[0] != null) {
                    // Fallback: username uniquement
                    return mostActive[0].toString();
                }
            }

            return "Aucun utilisateur actif trouvé";
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de la recherche de l'utilisateur le plus actif: " + e.getMessage());
            return "Non disponible";
        }
    }
}
