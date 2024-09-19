package com.example.application.views.myview;

import com.example.application.entity.*;
import com.example.application.service.implementation.ItemServiceImpl;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Catalogue")
@Route(value = "/catalogue", layout = MainLayout.class)
@AnonymousAllowed
public class CatalogueView extends VerticalLayout {
    private ComboBox<String> typeComboBox;
    private VerticalLayout searchFieldsLayout;
    private Button searchButton;
    private Grid<Item> resultsGrid;
    private CallbackDataProvider<Item, Void> dataProvider;

    private ItemServiceImpl itemService;

    // Critères de recherche courants
    private String selectedType;
    private Map<String, Object> searchCriteria = new HashMap<>();

    public CatalogueView(ItemServiceImpl itemService) {
        this.itemService = itemService;
        setWidth("100%");
        getStyle().set("flex-grow", "1");
        setHeight("100%");
        configureComponents();
        add(createSearchLayout());
    }

    private void configureComponents() {
        // Initialisation des composants
        typeComboBox = new ComboBox<>("Type d'item");
        typeComboBox.setItems("Tous", "Livre", "Revue", "Jeu");
        typeComboBox.setValue("Tous");

        searchFieldsLayout = new VerticalLayout();
        searchFieldsLayout.setSpacing(false);
        searchFieldsLayout.setPadding(false);

        searchButton = new Button("Rechercher", e -> searchItems());

        configureGrid();
    }

    private Component createSearchLayout() {
        VerticalLayout layout = new VerticalLayout();

        // Listener pour adapter les inputs en fonction du type
        typeComboBox.addValueChangeListener(e -> updateSearchFields());

        // Initialisation des champs de recherche
        updateSearchFields();

        layout.add(typeComboBox, searchFieldsLayout, searchButton);
        return layout;
    }

    private void updateSearchFields() {
        searchFieldsLayout.removeAll();

        selectedType = typeComboBox.getValue();

        if ("Livre".equals(selectedType)) {
            createBookSearchFields();
        } else if ("Revue".equals(selectedType)) {
            createMagazineSearchFields();
        } else if ("Jeu".equals(selectedType)) {
            createGameSearchFields();
        } else {
            createGeneralSearchFields();
        }
    }

    private void createBookSearchFields() {
        searchCriteria.clear();

        TextField titleField = new TextField("Titre");
        TextField authorField = new TextField("Auteur");
        TextField isbnField = new TextField("ISBN");
        DatePicker publicationDateField = new DatePicker("Date de Publication");
        ComboBox<Category> categoryComboBox = new ComboBox<>("Catégorie");
        categoryComboBox.setItems(itemService.getAllCategories());
        ComboBox<Publisher> publisherComboBox = new ComboBox<>("Éditeur");
        publisherComboBox.setItems(itemService.getAllPublishers());

        searchFieldsLayout.add(titleField, authorField, isbnField, publicationDateField, categoryComboBox, publisherComboBox);
    }

    private void createMagazineSearchFields() {
        searchCriteria.clear();

        TextField titleField = new TextField("Titre");
        TextField isniField = new TextField("ISNI");
        ComboBox<String> monthComboBox = new ComboBox<>("Mois de Publication");

        monthComboBox.setItems(Arrays.stream(Month.values())
                .map(month -> month.getDisplayName(TextStyle.FULL, Locale.FRENCH))
                .collect(Collectors.toList())); //TODO voir solutions

        DatePicker publicationDateField = new DatePicker("Date de Publication");
        ComboBox<Category> categoryComboBox = new ComboBox<>("Catégorie");
        List<Category> categories = itemService.getAllCategories();

        // Crée un DataProvider à partir de la liste de catégories
        ListDataProvider<Category> dataProviderCategory = new ListDataProvider<>(categories);

        categoryComboBox.setItemLabelGenerator(Category::getName);

        categoryComboBox.setItems(dataProviderCategory);
        ComboBox<Publisher> publisherComboBox = new ComboBox<>("Éditeur");
        publisherComboBox.setItems(itemService.getAllPublishers());

        searchFieldsLayout.add(titleField, isniField, monthComboBox, publicationDateField, categoryComboBox, publisherComboBox);
    }

    private void createGameSearchFields() {
        searchCriteria.clear();

        TextField titleField = new TextField("Titre");
        IntegerField numberOfPiecesField = new IntegerField("Nombre de Pièces");
        IntegerField recommendedAgeField = new IntegerField("Âge Recommandé");
        ComboBox<Category> categoryComboBox = new ComboBox<>("Catégorie");
        categoryComboBox.setItems(itemService.getAllCategories());
        ComboBox<Publisher> publisherComboBox = new ComboBox<>("Éditeur");
        publisherComboBox.setItems(itemService.getAllPublishers());

        searchFieldsLayout.add(titleField, numberOfPiecesField, recommendedAgeField, categoryComboBox, publisherComboBox);
    }

    private void createGeneralSearchFields() {
        searchCriteria.clear();

        TextField keywordField = new TextField("Mot-clé");
        ComboBox<Category> categoryComboBox = new ComboBox<>("Catégorie");
        categoryComboBox.setItems(itemService.getAllCategories());
        ComboBox<Publisher> publisherComboBox = new ComboBox<>("Éditeur");
        publisherComboBox.setItems(itemService.getAllPublishers());

        searchFieldsLayout.add(keywordField, categoryComboBox, publisherComboBox);
    }

    private void searchItems() {
        selectedType = typeComboBox.getValue();
        searchCriteria.clear();

        if ("Livre".equals(selectedType)) {
            TextField titleField = (TextField) searchFieldsLayout.getComponentAt(0);
            TextField authorField = (TextField) searchFieldsLayout.getComponentAt(1);
            TextField isbnField = (TextField) searchFieldsLayout.getComponentAt(2);
            DatePicker publicationDateField = (DatePicker) searchFieldsLayout.getComponentAt(3);
            ComboBox<Category> categoryComboBox = (ComboBox<Category>) searchFieldsLayout.getComponentAt(4);
            ComboBox<Publisher> publisherComboBox = (ComboBox<Publisher>) searchFieldsLayout.getComponentAt(5);

            searchCriteria.put("title", titleField.getValue());
            searchCriteria.put("author", authorField.getValue());
            searchCriteria.put("isbn", isbnField.getValue());
            searchCriteria.put("publicationDate", publicationDateField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());

        } else if ("Revue".equals(selectedType)) {
            TextField titleField = (TextField) searchFieldsLayout.getComponentAt(0);
            TextField isniField = (TextField) searchFieldsLayout.getComponentAt(1);
            ComboBox<String> monthComboBox = (ComboBox<String>) searchFieldsLayout.getComponentAt(2);
            DatePicker publicationDateField = (DatePicker) searchFieldsLayout.getComponentAt(3);
            ComboBox<Category> categoryComboBox = (ComboBox<Category>) searchFieldsLayout.getComponentAt(4);
            ComboBox<Publisher> publisherComboBox = (ComboBox<Publisher>) searchFieldsLayout.getComponentAt(5);

            searchCriteria.put("title", titleField.getValue());
            searchCriteria.put("isni", isniField.getValue());
            searchCriteria.put("month", monthComboBox.getValue());
            searchCriteria.put("publicationDate", publicationDateField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());

        } else if ("Jeu".equals(selectedType)) {
            TextField titleField = (TextField) searchFieldsLayout.getComponentAt(0);
            IntegerField numberOfPiecesField = (IntegerField) searchFieldsLayout.getComponentAt(1);
            IntegerField recommendedAgeField = (IntegerField) searchFieldsLayout.getComponentAt(2);
            ComboBox<Category> categoryComboBox = (ComboBox<Category>) searchFieldsLayout.getComponentAt(3);
            ComboBox<Publisher> publisherComboBox = (ComboBox<Publisher>) searchFieldsLayout.getComponentAt(4);

            searchCriteria.put("title", titleField.getValue());
            searchCriteria.put("numberOfPieces", numberOfPiecesField.getValue());
            searchCriteria.put("recommendedAge", recommendedAgeField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());

        } else {
            TextField keywordField = (TextField) searchFieldsLayout.getComponentAt(0);
            ComboBox<Category> categoryComboBox = (ComboBox<Category>) searchFieldsLayout.getComponentAt(1);
            ComboBox<Publisher> publisherComboBox = (ComboBox<Publisher>) searchFieldsLayout.getComponentAt(2);

            searchCriteria.put("keyword", keywordField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());
        }

        dataProvider.refreshAll();
    }

    private void configureGrid() {
        resultsGrid = new Grid<>(Item.class);
        resultsGrid.setSizeFull();

        resultsGrid.removeAllColumns();

        resultsGrid.addColumn(Item::getTitle).setHeader("Titre").setSortable(true);
        resultsGrid.addColumn(Item::getType).setHeader("Type").setSortable(true);
        resultsGrid.addColumn(item -> item.getCategory().getName()).setHeader("Catégorie");
        resultsGrid.addColumn(item -> item.getPublisher().getName()).setHeader("Éditeur");

        // Colonnes spécifiques en fonction du type
        resultsGrid.addColumn(item -> {
            if ("Livre".equals(item.getType())) {
                Book book = itemService.findBookByItemId(item.getId());
                return book != null ? book.getAuthor() : "";
            } else if ("Revue".equals(item.getType())) {
                Magazine magazine = itemService.findMagazineByItemId(item.getId());
                return magazine != null ? magazine.getIsni() : "";
            } else if ("Jeu".equals(item.getType())) {
                BoardGame game = itemService.findBoardGameByItemId(item.getId());
                return game != null ? game.getRecommendedAge() : "";
            } else {
                return "";
            }
        }).setHeader("Détails");

        resultsGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        // Ajouter un listener pour la sélection des items
        resultsGrid.asSingleSelect().addValueChangeListener(event -> {
            Item selectedItem = event.getValue();
            if (selectedItem != null) {
                resultsGrid.getUI().ifPresent(ui ->
                        ui.navigate(ItemDetailView.class, selectedItem.getId().toString())
                );
            }
        });

        // Configuration du DataProvider
        dataProvider = DataProvider.fromCallbacks(
                // Fetch items
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();

                    // Récupérer les items avec pagination
                    return itemService.fetchItemsWithFilters(searchCriteria, selectedType, offset, limit).stream();
                },
                // Count items
                query -> itemService.countItemsWithFilters(searchCriteria, selectedType)
        );

        resultsGrid.setDataProvider(dataProvider);

        add(resultsGrid);
    }
}
