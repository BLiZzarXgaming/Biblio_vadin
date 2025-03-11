package com.example.application.service;

import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.Mapper.LoanMapper;
import com.example.application.repository.LoanRepositoryV2;
import com.example.application.service.implementation.LoanServiceV2;
import com.example.application.utils.StatusUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;

@Service
public class LoanServiceImplV2 implements LoanServiceV2 {
    private static final Logger LOGGER = Logger.getLogger(LoanServiceImplV2.class.getName());

    private final LoanRepositoryV2 loanRepository;
    private final LoanMapper loanMapper;

    public LoanServiceImplV2(LoanRepositoryV2 loanRepository, LoanMapper loanMapper) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
    }

    @Override
    public List<LoanDto> findAll() {
        return loanRepository.findAll().stream().map(loanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<LoanDto> findById(Long id) {
        return loanRepository.findById(id).map(loanMapper::toDto);
    }

    @Override
    public List<LoanDto> findByMember(Long memberId) {
        return loanRepository.findByMemberId(memberId).stream().map(loanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LoanDto> findByCopy(Long copyId) {
        return loanRepository.findByCopyId(copyId).stream().map(loanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LoanDto> findByStatus(String status) {
        return loanRepository.findByStatus(status).stream().map(loanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LoanDto> findByLoanDateRange(Date startDate, Date endDate) {
        return loanRepository.findByLoanDateBetween(startDate, endDate).stream().map(loanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LoanDto save(LoanDto loan) {
        // Validation spécifique si nécessaire
        return loanMapper.toDto(loanRepository.save(loanMapper.toEntity(loan)));
    }

    @Override
    public void deleteById(Long id) {
        loanRepository.deleteById(id);
    }

    // Implémentation des méthodes de statistiques

    @Override
    public Map<String, Integer> getLoansByMonth() {
        try {
            // Récupérer les données de l'année en cours
            Date startOfYear = Date.from(LocalDate.now().withDayOfMonth(1).withMonth(1)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date now = new Date();

            List<Object[]> results = loanRepository.countLoansByMonth(startOfYear, now);
            Map<String, Integer> loansByMonth = new LinkedHashMap<>();

            if (results != null && !results.isEmpty()) {
                for (Object[] result : results) {
                    if (result != null && result.length >= 2 && result[0] != null && result[1] != null) {
                        String yearMonth = result[0].toString();
                        Integer count = ((Number) result[1]).intValue();
                        // Convertir le format YYYY-MM en format français
                        String[] parts = yearMonth.split("-");
                        if (parts.length >= 2) {
                            String month = getMonthName(Integer.parseInt(parts[1]));
                            loansByMonth.put(month, count);
                        }
                    }
                }
            }

            // Assurer que tous les mois sont présents dans la carte
            Map<String, Integer> result = new LinkedHashMap<>();
            for (int i = 1; i <= 12; i++) {
                String month = getMonthName(i);
                result.put(month, loansByMonth.getOrDefault(month, 0));
            }

            return result;
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de la récupération des prêts par mois: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Integer> getLoansByStatus() {
        try {
            // Récupérer les données de l'année en cours
            Date startOfYear = Date.from(LocalDate.now().withDayOfMonth(1).withMonth(1)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date now = new Date();

            List<Object[]> results = loanRepository.countLoansByStatus(startOfYear, now);
            Map<String, Integer> loansByStatus = new HashMap<>();

            if (results != null && !results.isEmpty()) {
                for (Object[] result : results) {
                    if (result != null && result.length >= 2 && result[0] != null && result[1] != null) {
                        String status = StatusUtils.LoanStatus.toFrench(result[0].toString());
                        Integer count = ((Number) result[1]).intValue();
                        loansByStatus.put(status, count);
                    }
                }
            }

            return loansByStatus;
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de la récupération des prêts par statut: " + e.getMessage());
            return null;
        }
    }

    // Méthode utilitaire pour obtenir le nom du mois en français
    private String getMonthName(int month) {
        String[] months = { "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre" };
        if (month >= 1 && month <= 12) {
            return months[month - 1];
        }
        return "Inconnu";
    }
}
