package com.example.application.service;

import com.example.application.entity.DTO.CategoryDto;
import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.DTO.PublisherDto;
import com.example.application.entity.Mapper.ItemMapper;
import com.example.application.repository.ItemRepositoryV2;
import com.example.application.repository.CopyRepositoryV2;
import com.example.application.service.implementation.ItemServiceV2;
import com.example.application.utils.StatusUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ItemServiceImplV2 implements ItemServiceV2 {
    private static final Logger LOGGER = Logger.getLogger(ItemServiceImplV2.class.getName());

    private final ItemRepositoryV2 itemRepository;
    private final ItemMapper itemMapper;
    private final CopyRepositoryV2 copyRepository;

    public ItemServiceImplV2(ItemRepositoryV2 itemRepository, ItemMapper itemMapper, CopyRepositoryV2 copyRepository) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.copyRepository = copyRepository;
    }

    @Override
    public List<ItemDto> findAll() {
        return itemRepository.findAll().stream().map(itemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<ItemDto> findById(Long id) {
        return itemRepository.findById(id).map(itemMapper::toDto);
    }

    @Override
    public List<ItemDto> findByType(String type) {
        return itemRepository.findByType(type).stream().map(itemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchByTitle(String keyword) {
        return itemRepository.findByTitleContaining(keyword).stream().map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findByCategory(Long categoryId) {
        return itemRepository.findByCategoryId(categoryId).stream().map(itemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public ItemDto save(ItemDto item) {
        return itemMapper.toDto(itemRepository.save(itemMapper.toEntity(item)));
    }

    @Override
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> fetchItemsWithFilters(Map<String, Object> searchCriteria, String selectedType, int offset,
            int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit); // Pageable object for pagination
        if (StatusUtils.DocTypes.LIVRE.equals(selectedType)) { // Book
            return itemRepository.findBookByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (String) searchCriteria.get("author"),
                    (String) searchCriteria.get("isbn"),
                    (LocalDate) searchCriteria.get("publicationDate"),
                    searchCriteria.get("category") != null ? ((CategoryDto) searchCriteria.get("category")).getId()
                            : null,
                    searchCriteria.get("publisher") != null ? ((PublisherDto) searchCriteria.get("publisher")).getId()
                            : null,
                    pageable).stream().map(itemMapper::toDto).collect(Collectors.toList());
        } else if (StatusUtils.DocTypes.REVUE.equals(selectedType)) { // Magazine
            return itemRepository.findMagazineByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (String) searchCriteria.get("isni"),
                    (String) searchCriteria.get("month"),
                    (LocalDate) searchCriteria.get("publicationDate"),
                    searchCriteria.get("category") != null ? ((CategoryDto) searchCriteria.get("category")).getId()
                            : null,
                    searchCriteria.get("publisher") != null ? ((PublisherDto) searchCriteria.get("publisher")).getId()
                            : null,
                    searchCriteria.get("year") != null ? (Integer) searchCriteria.get("year") : null,
                    pageable).stream().map(itemMapper::toDto).collect(Collectors.toList());
        } else if (StatusUtils.DocTypes.JEU.equals(selectedType)) { // BoardGame
            return itemRepository.findBoardGameByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (Integer) searchCriteria.get("numberOfPieces"),
                    (Integer) searchCriteria.get("recommendedAge"),
                    searchCriteria.get("category") != null ? ((CategoryDto) searchCriteria.get("category")).getId()
                            : null,
                    searchCriteria.get("publisher") != null ? ((PublisherDto) searchCriteria.get("publisher")).getId()
                            : null,
                    searchCriteria.get("gtin") != null ? (String) searchCriteria.get("gtin") : null,
                    pageable).stream().map(itemMapper::toDto).collect(Collectors.toList());
        } else { // All items
            return itemRepository.findByCriteriaWithPagination(
                    (String) searchCriteria.get("keyword"),
                    searchCriteria.get("category") != null ? ((CategoryDto) searchCriteria.get("category")).getId()
                            : null,
                    searchCriteria.get("publisher") != null ? ((PublisherDto) searchCriteria.get("publisher")).getId()
                            : null,
                    pageable).stream().map(itemMapper::toDto).collect(Collectors.toList());
        }
    }

    // Implémentation des méthodes de statistiques avec utilisation réelle des
    // repositories

    @Override
    public Map<String, Long> countItemsByType() {
        Map<String, Long> itemsByType = new HashMap<>();
        List<ItemDto> allItems = itemRepository.findAllItemByType().stream().map(itemMapper::toDto)
                .toList();

        // Compter le nombre de documents par type
        itemsByType.put("Livres", allItems.stream()
                .filter(item -> StatusUtils.DocTypes.BOOK.equals(item.getType()))
                .count());

        itemsByType.put("Magazines", allItems.stream()
                .filter(item -> StatusUtils.DocTypes.MAGAZINE.equals(item.getType()))
                .count());

        itemsByType.put("Jeux de société", allItems.stream()
                .filter(item -> StatusUtils.DocTypes.BOARD_GAME.equals(item.getType()))
                .count());

        return itemsByType;
    }

    @Override
    public String getMostPopularCategory(Date startDate, Date endDate) {
        try {
            Object[] result = itemRepository.findMostPopularCategorySince(startDate, endDate);

            if (result != null && result.length > 0 && result[0] != null) {
                Object[] categoryData = (Object[]) result[0];
                // Extraire le nom de la catégorie (premier élément du tableau)
                String categoryName = (categoryData[0]).toString();
                // Extraire le nombre de prêts (deuxième élément)
                long loanCount = 0;
                if (categoryData.length > 1 && categoryData[1] != null) {
                    loanCount = ((Number) categoryData[1]).longValue();
                }

                return categoryName + " (" + loanCount + " emprunts)";
            }

            return "Aucune catégorie populaire trouvée";
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de la recherche de la catégorie la plus populaire: " + e.getMessage());
            return "Non disponible";
        }
    }

    @Override
    public String getMostBorrowedType(Date startDate, Date endDate) {
        try {
            Object[] result = itemRepository.findMostBorrowedTypeSince(startDate, endDate);

            if (result != null && result.length > 0 && result[0] != null) {
                Object[] typeData = (Object[]) result[0];
                // Conversion du type technique en libellé compréhensible
                String type = typeData[0].toString();
                // Extraire le nombre de prêts (deuxième élément)
                long loanCount = 0;
                if (result.length > 0 && result[0] != null) {
                    loanCount = (long) typeData[1];
                }

                String typeLabel;
                switch (type) {
                    case StatusUtils.DocTypes.BOOK:
                        typeLabel = "Livres";
                        break;
                    case StatusUtils.DocTypes.MAGAZINE:
                        typeLabel = "Magazines";
                        break;
                    case StatusUtils.DocTypes.BOARD_GAME:
                        typeLabel = "Jeux de société";
                        break;
                    default:
                        typeLabel = type;
                }

                return typeLabel + " (" + loanCount + " emprunts)";
            }

            return "Aucun type populaire trouvé";
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de la recherche du type le plus emprunté: " + e.getMessage());
            return "Non disponible";
        }
    }

    @Override
    public double calculateTotalInventoryValue() {
        try {
            return copyRepository.calculateTotalInventoryValue();

        } catch (Exception e) {
            LOGGER.warning("Erreur lors du calcul de la valeur totale de l'inventaire: " + e.getMessage());
            return 0.0;
        }
    }

    @Override
    public double calculateTotalBorrowedValue(Date startDate, Date endDate) {
        try {

            // Utiliser la méthode du repository pour calculer la valeur totale des emprunts
            return copyRepository.calculateTotalBorrowedValue(startDate, endDate);
        } catch (Exception e) {
            LOGGER.warning("Erreur lors du calcul de la valeur totale des emprunts: " + e.getMessage());
            return 0.0;
        }
    }

    @Override
    public int countTotalItems() {
        return (int) itemRepository.countItemByCopies();
    }

    @Override
    public int countRecentAcquisitions() {
        try {
            // Récupérer les données des 30 derniers jours
            Date thirtyDaysAgo = Date
                    .from(LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());
            return (int) itemRepository.countItemsCreatedSince(thirtyDaysAgo);
        } catch (Exception e) {
            LOGGER.warning("Erreur lors du comptage des acquisitions récentes: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public String getMostPopularItem(Date startDate, Date endDate) {
        try {
            // Utiliser la méthode du repository pour trouver l'item le plus emprunté

            Object[] result = itemRepository.findMostPopularItemSince(startDate, endDate);

            if (result != null && result.length > 0 && result[0] != null) {
                Object[] itemData = (Object[]) result[0];

                // Extraire les informations: [id, title, type, count]
                String title = (itemData[1] != null) ? itemData[1].toString() : "Sans titre";
                String type = (itemData[2] != null) ? itemData[2].toString() : "";
                long loanCount = 0;
                if (itemData.length > 3 && itemData[3] != null) {
                    loanCount = ((Number) itemData[3]).longValue();
                }

                type = type.toUpperCase();

                // Formatter selon le type
                String typeLabel = "";
                if (StatusUtils.DocTypes.BOOK.equals(type)) {
                    typeLabel = " (Livre)";
                } else if (StatusUtils.DocTypes.MAGAZINE.equals(type)) {
                    typeLabel = " (Magazine)";
                } else if (StatusUtils.DocTypes.BOARD_GAME.equals(type)) {
                    typeLabel = " (Jeu de société)";
                }

                return title + typeLabel + " - " + loanCount + " emprunts";
            }

            return "Aucun document populaire trouvé";
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de la recherche du document le plus populaire: " + e.getMessage());
            return "Non disponible";
        }
    }
}
