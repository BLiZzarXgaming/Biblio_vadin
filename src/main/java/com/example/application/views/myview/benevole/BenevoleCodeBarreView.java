package com.example.application.views.myview.benevole;

import com.example.application.entity.DTO.CopyDto;
import com.example.application.entity.DTO.ItemDto;
import com.example.application.service.implementation.CopyServiceV2;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.vaadin.barcodes.Barcode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PageTitle("Génération de Codes-barres")
@Route(value = "volonteer/codesbarres", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleCodeBarreView extends VerticalLayout {

    private static final int PAGE_SIZE = 10;

    private final CopyServiceV2 copyService;

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final Checkbox todayFilter = new Checkbox("Ajoutés aujourd'hui");
    private final Grid<CopyDto> copyGrid = new Grid<>(CopyDto.class);
    private final List<CopyDto> selectedCopies = new ArrayList<>();
    private final FlexLayout barcodeContainer = new FlexLayout();

    // Map pour traduire les statuts
    private final Map<String, String> statusTranslations = new HashMap<>();

    // Map pour traduire les types de documents
    private final Map<String, String> typeTranslations = new HashMap<>();

    public BenevoleCodeBarreView(CopyServiceV2 copyService) {
        this.copyService = copyService;

        initStatusTranslations();
        initTypeTranslations();

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        add(createHeader());
        add(createSearchArea());

        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(true);

        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.add(configureGrid());
        gridLayout.add(createButtonsLayout());
        gridLayout.setWidth("50%");

        VerticalLayout barcodeLayout = new VerticalLayout();
        barcodeLayout.add(new H3("Codes-barres à imprimer"));
        configureBarcodeContainer();
        barcodeLayout.add(barcodeContainer);
        barcodeLayout.add(createPrintButton());
        barcodeLayout.setWidth("50%");

        mainContent.add(gridLayout, barcodeLayout);
        add(mainContent);
    }

    private void initStatusTranslations() {
        // Corriger les mappings des statuts entre l'anglais et le français
        // Ajouter toutes les variantes possibles pour s'assurer que les statuts sont
        // correctement traduits
        statusTranslations.put("Available", "Disponible");
        statusTranslations.put("AVAILABLE", "Disponible");
        statusTranslations.put("available", "Disponible");

        statusTranslations.put("Borrowed", "Emprunté");
        statusTranslations.put("BORROWED", "Emprunté");
        statusTranslations.put("borrowed", "Emprunté");

        statusTranslations.put("Reserved", "Réservé");
        statusTranslations.put("RESERVED", "Réservé");
        statusTranslations.put("reserved", "Réservé");

        statusTranslations.put("Repair", "En réparation");
        statusTranslations.put("REPAIR", "En réparation");
        statusTranslations.put("repair", "En réparation");

        statusTranslations.put("Lost", "Perdu");
        statusTranslations.put("LOST", "Perdu");
        statusTranslations.put("lost", "Perdu");
        statusTranslations.put("unavailable", "Indisponible");
        statusTranslations.put("deleted", "Supprimé");

        // Également mapper les versions françaises pour la recherche
        statusTranslations.put("Disponible", "Disponible");
        statusTranslations.put("Emprunté", "Emprunté");
        statusTranslations.put("Réservé", "Réservé");
        statusTranslations.put("En réparation", "En réparation");
        statusTranslations.put("Perdu", "Perdu");

        // Journaliser tous les mappings pour le débogage
        System.out.println("Mappings de traduction des statuts : " + statusTranslations);
    }

    private String translateStatus(String status) {
        if (status == null) {
            return "Inconnu";
        }

        String translated = statusTranslations.get(status);

        // Si la traduction n'est pas trouvée, essayer avec une version modifiée
        // (majuscules/minuscules)
        if (translated == null) {
            translated = statusTranslations.get(status.toUpperCase());
        }
        if (translated == null) {
            translated = statusTranslations.get(status.toLowerCase());
        }

        // Si toujours pas de traduction, utiliser le statut original
        if (translated == null) {
            System.out.println("Aucune traduction trouvée pour le statut : '" + status + "'");
            return status;
        }

        return translated;
    }

    private void initTypeTranslations() {
        // Traduction des types de documents
        typeTranslations.put("book", "Livre");
        typeTranslations.put("BOOK", "Livre");
        typeTranslations.put("Book", "Livre");

        typeTranslations.put("magazine", "Magazine");
        typeTranslations.put("MAGAZINE", "Magazine");
        typeTranslations.put("Magazine", "Magazine");

        typeTranslations.put("boardgame", "Jeu de société");
        typeTranslations.put("BOARDGAME", "Jeu de société");
        typeTranslations.put("Boardgame", "Jeu de société");
        typeTranslations.put("board_game", "Jeu de société");
        typeTranslations.put("Board_Game", "Jeu de société");

        typeTranslations.put("dvd", "DVD");
        typeTranslations.put("DVD", "DVD");

        typeTranslations.put("cd", "CD");
        typeTranslations.put("CD", "CD");

        // Journaliser les mappings pour le débogage
        System.out.println("Mappings de traduction des types : " + typeTranslations);
    }

    private String translateType(String type) {
        if (type == null) {
            return "Document";
        }

        String translated = typeTranslations.get(type);

        // Si la traduction n'est pas trouvée, essayer avec une version modifiée
        if (translated == null) {
            translated = typeTranslations.get(type.toUpperCase());
        }
        if (translated == null) {
            translated = typeTranslations.get(type.toLowerCase());
        }

        // Si toujours pas de traduction, utiliser le type original
        if (translated == null) {
            System.out.println("Aucune traduction trouvée pour le type : '" + type + "'");
            return type;
        }

        return translated;
    }

    private Component createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Génération de Codes-barres pour les Copies");
        header.add(title);

        Paragraph info = new Paragraph(
                "Sélectionnez une ou plusieurs copies pour générer leurs codes-barres. " +
                        "Vous pouvez imprimer la page contenant tous les codes-barres générés.");
        header.add(info);

        return header;
    }

    private Component createSearchArea() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();

        searchField.setPlaceholder("Rechercher par ID ou titre...");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> updateGrid());

        statusFilter.setPlaceholder("Filtrer par statut");
        statusFilter.setItems("Disponible", "Emprunté", "Réservé", "En réparation", "Perdu");
        statusFilter.addValueChangeListener(e -> updateGrid());

        todayFilter.addValueChangeListener(e -> updateGrid());

        Button resetButton = new Button("Réinitialiser");
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickListener(e -> {
            searchField.clear();
            statusFilter.clear();
            todayFilter.setValue(false);
            updateGrid();
        });

        layout.add(searchField, statusFilter, todayFilter, resetButton);
        return layout;
    }

    private Grid<CopyDto> configureGrid() {
        copyGrid.removeAllColumns();

        // Colonne ID avec lien pour plus de détails
        copyGrid.addColumn(copy -> copy.getId())
                .setHeader("ID")
                .setAutoWidth(true);

        // Colonne titre avec plus d'informations
        copyGrid.addColumn(copy -> {
            ItemDto item = copy.getItem();
            if (item == null)
                return "N/A";

            StringBuilder info = new StringBuilder(item.getTitle());

            // Voir si l'item a des informations supplémentaires spécifiques à son type
            try {
                // Obtenir le type d'élément
                String itemType = item.getType();
                if ("book".equalsIgnoreCase(itemType)) {
                    // Essayer d'obtenir des informations spécifiques si disponibles
                    System.out.println(
                            "L'élément est un livre, mais nous ne pouvons pas accéder directement à l'auteur.");
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de l'accès aux propriétés de l'item: " + e.getMessage());
            }

            return info.toString();
        }).setHeader("Titre").setAutoWidth(true);

        // Colonne pour le type d'item - Avec traduction
        copyGrid.addColumn(copy -> {
            ItemDto item = copy.getItem();
            if (item == null)
                return "N/A";

            // Utiliser le type de l'item directement avec traduction
            String itemType = item.getType();
            return translateType(itemType);
        }).setHeader("Type").setAutoWidth(true);

        // Colonne de statut avec traduction améliorée
        copyGrid.addColumn(copy -> {
            String status = copy.getStatus();
            String translatedStatus = translateStatus(status);

            // Journaliser pour le débogage
            System.out.println("Statut original: '" + status + "', traduit: '" + translatedStatus + "'");

            return translatedStatus;
        }).setHeader("Statut").setAutoWidth(true);

        // Informations supplémentaires sur la copie - sans le prix
        copyGrid.addColumn(copy -> {
            StringBuilder info = new StringBuilder();

            // Afficher la date d'acquisition
            if (copy.getAcquisitionDate() != null) {
                info.append("Acquis le ").append(copy.getAcquisitionDate());
            }

            return info.length() > 0 ? info.toString() : "Aucune info";
        }).setHeader("Détails").setAutoWidth(true);

        copyGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        copyGrid.addSelectionListener(event -> {
            selectedCopies.clear();
            selectedCopies.addAll(event.getAllSelectedItems());
        });

        // Ajout de styles pour la pagination
        copyGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Définir explicitement la taille de page
        copyGrid.setPageSize(PAGE_SIZE);

        // Configurer le DataProvider avec pagination côté serveur
        configureDataProvider();

        return copyGrid;
    }

    private void configureDataProvider() {
        // Créer un DataProvider qui utilise directement la source de données paginée
        DataProvider<CopyDto, Void> dataProvider = DataProvider.fromCallbacks(
                // First callback fetches items based on a query
                query -> fetchCopies(query),
                // Second callback fetches the number of items for a query
                query -> (int) countCopies());

        copyGrid.setDataProvider(dataProvider);
    }

    private Stream<CopyDto> fetchCopies(Query<CopyDto, Void> query) {
        // Limiter la taille pour s'assurer que nous n'obtenons que les éléments d'une
        // page
        int limit = Math.min(query.getLimit(), PAGE_SIZE);
        int offset = query.getOffset();

        // Créer un Pageable avec la limite et l'offset
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.ASC, "id"));

        System.out
                .println("Requête de pagination: offset=" + offset + ", limit=" + limit + ", page=" + (offset / limit));

        // Obtenir la Page selon les filtres
        Page<CopyDto> page = fetchCopiesPage(pageable);

        // Récupérer et afficher des informations de débogage
        List<CopyDto> copies = page.getContent();
        System.out.println("Page " + page.getNumber() + " de " + page.getTotalPages() +
                " (total: " + page.getTotalElements() + " éléments)");
        System.out.println("Éléments récupérés: " + copies.size());

        return copies.stream();
    }

    private long countCopies() {
        // Si aucun filtre n'est appliqué, utilisez le count() du service
        if (searchField.isEmpty() && statusFilter.isEmpty() && !todayFilter.getValue()) {
            return copyService.count();
        }

        // Sinon, nous devons compter en fonction des filtres
        // Pour éviter de charger toutes les données, nous faisons une page de taille 1
        // et récupérons le nombre total d'éléments
        Pageable pageable = PageRequest.of(0, 1);
        Page<CopyDto> page = fetchCopiesPage(pageable);

        return page.getTotalElements();
    }

    private Page<CopyDto> fetchCopiesPage(Pageable pageable) {
        String searchTerm = searchField.isEmpty() ? null : searchField.getValue().toLowerCase();
        String status = statusFilter.isEmpty() ? null : translateStatus(statusFilter.getValue());
        boolean filterByToday = todayFilter.getValue();
        LocalDate today = filterByToday ? LocalDate.now() : null;

        // Appliquer différentes méthodes de service selon les filtres actifs
        if (searchTerm != null && status != null && filterByToday) {
            return copyService.findBySearchTermAndStatusAndAcquisitionDate(searchTerm, status, today, pageable);
        } else if (searchTerm != null && status != null) {
            return copyService.findBySearchTermAndStatus(searchTerm, status, pageable);
        } else if (searchTerm != null && filterByToday) {
            return copyService.findBySearchTermAndAcquisitionDate(searchTerm, today, pageable);
        } else if (status != null && filterByToday) {
            return copyService.findByStatusAndAcquisitionDate(status, today, pageable);
        } else if (searchTerm != null) {
            return copyService.findBySearchTerm(searchTerm, pageable);
        } else if (status != null) {
            return copyService.findByStatusPaginated(status, pageable);
        } else if (filterByToday) {
            return copyService.findByAcquisitionDate(today, pageable);
        } else {
            return copyService.findAllPaginated(pageable);
        }
    }

    private void updateGrid() {
        // Actualiser le DataProvider
        copyGrid.getDataProvider().refreshAll();
    }

    private Component createButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();

        Button generateButton = new Button("Générer Codes-barres", new Icon(VaadinIcon.BARCODE));
        generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        generateButton.addClickListener(event -> generateBarcodes());

        Button clearSelectionButton = new Button("Effacer la sélection");
        clearSelectionButton.addClickListener(event -> {
            copyGrid.deselectAll();
            selectedCopies.clear();
        });

        buttonsLayout.add(generateButton, clearSelectionButton);
        return buttonsLayout;
    }

    private void configureBarcodeContainer() {
        barcodeContainer.setWidth("100%");
        barcodeContainer.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "10px")
                .set("justify-content", "center")
                .set("border", "1px solid #ccc")
                .set("padding", "10px")
                .set("min-height", "300px");
    }

    private Component createPrintButton() {
        Button printButton = new Button("Imprimer", new Icon(VaadinIcon.PRINT));
        printButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        printButton.addClickListener(event -> {
            if (barcodeContainer.getChildren().count() > 0) {
                // JavaScript pour impression
                getUI().ifPresent(ui -> ui.getPage().executeJs(
                        "const originalTitle = document.title;" +
                                "document.title = 'Codes-barres des Copies';" +
                                "window.print();" +
                                "setTimeout(() => {document.title = originalTitle;}, 100);"));
            } else {
                Notification.show("Aucun code-barre à imprimer", 3000, Notification.Position.MIDDLE);
            }
        });

        return printButton;
    }

    private void generateBarcodes() {
        if (selectedCopies.isEmpty()) {
            Notification notification = Notification.show(
                    "Veuillez sélectionner au moins une copie",
                    3000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        barcodeContainer.removeAll();

        for (CopyDto copy : selectedCopies) {
            Div barcodeCard = new Div();
            barcodeCard.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center")
                    .set("padding", "10px")
                    .set("border", "1px solid #ddd")
                    .set("border-radius", "4px")
                    .set("margin", "5px")
                    .set("width", "230px"); // Augmenté pour plus d'informations

            // Le code-barres contiendra l'ID de la copie préfixé par "C"
            String barcodeValue = "C" + copy.getId();

            // Créer le code-barres
            Barcode barcode = new Barcode(barcodeValue, Barcode.Type.code128, "180px", "80px");

            // Informations sur la copie - Enrichies
            StringBuilder detailBuilder = new StringBuilder();
            detailBuilder.append("Copie ID: ").append(copy.getId());

            if (copy.getItem() != null) {
                detailBuilder.append("\nTitre: ").append(copy.getItem().getTitle());
            } else {
                detailBuilder.append("\nTitre: N/A");
            }

            detailBuilder.append("\nStatut: ").append(translateStatus(copy.getStatus()));

            // Ajouter la date d'acquisition
            if (copy.getAcquisitionDate() != null) {
                detailBuilder.append("\nAcquis le: ").append(copy.getAcquisitionDate());
            }

            Paragraph copyInfo = new Paragraph(detailBuilder.toString());
            copyInfo.getStyle()
                    .set("font-size", "12px")
                    .set("text-align", "center")
                    .set("margin-top", "8px");

            // Ajouter un sous-titre indiquant le type d'élément
            String itemType = "Document";
            if (copy.getItem() != null) {
                String type = copy.getItem().getType();
                itemType = translateType(type);
            }

            Paragraph itemTypeInfo = new Paragraph(itemType);
            itemTypeInfo.getStyle()
                    .set("font-weight", "bold")
                    .set("margin", "0")
                    .set("color", "#1a73e8");

            barcodeCard.add(itemTypeInfo, barcode, copyInfo);
            barcodeContainer.add(barcodeCard);
        }

        Notification notification = Notification.show(
                selectedCopies.size() + " code(s)-barre(s) généré(s)",
                3000,
                Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}