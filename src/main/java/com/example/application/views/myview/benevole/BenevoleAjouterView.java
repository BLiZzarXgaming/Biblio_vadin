package com.example.application.views.myview.benevole;

import com.example.application.entity.*;
import com.example.application.objectcustom.MoisOption;
import com.example.application.service.implementation.ItemServiceImpl;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@PageTitle("Ajouter des articles")
@Route(value = "volunteer/add", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleAjouterView extends Composite<VerticalLayout> {

    private Grid<Item> itemsGrid;
    private List<Item> itemsToAdd; // Liste des items à ajouter
    private Button addItemButton;
    private Button submitButton;

    private ItemServiceImpl itemService;

    // l'item dans le modal
    private Item itemModal;

    // les champs pour ajouter un livre
    private TextField ISBNField;
    private TextField authorField;
    private DatePicker publicationDateField;
    private TextField ISBNValueField;

    // les champs pour ajouter un magasine
    private TextField isniField;
    private ComboBox<MoisOption> monthComboBox;
    private IntegerField yearField;
    private TextField isniValueField;
    private ComboBox<MoisOption> monthValueComboBox;
    private IntegerField yearValueField;

    // les champs pour ajouter un jeu
    private TextField gtinField;
    private TextField gtinValueField;
    private IntegerField numberOfPiecesField;
    private IntegerField minimumAgeField;
    private TextArea descriptionGameField;

    // les champs pour une catgorie
    private TextField nameField;
    private TextField descriptionCategorieField;

    // les champs pour un éditeur
    private TextField namePublisherField;
    private TextField addressPublisherField;

    // les champs pour un fournisseur
    private TextField nameSupplierField;
    private TextField addressSupplierField;

    // les champs pour un item
    private TextField titleField;
    private NumberField valueField;
    private TextField linkItemField;

    // les champs pour une copie
    private DatePicker purchaseDateField;
    private NumberField purchasePriceField;






    public BenevoleAjouterView(ItemServiceImpl itemService) {
        this.itemService = itemService;
        getContent().setWidth("100%");
        getContent().setHeightFull();

        itemsToAdd = new ArrayList<>();
        configureComponents();
        //addContent();
    }

    private void configureComponents() {
        addItemButton = new Button("Ajouter un article", event -> {
            Dialog addmodal = createAddModal();
            getContent().add(addmodal);
            addmodal.open();
        });
        submitButton = new Button("Soumettre", event -> submitItems());
        submitButton.setEnabled(false); // Désactivé tant qu'aucun item n'est ajouté

        itemsGrid = new Grid<>();

        getContent().add(addItemButton, submitButton);
        H2 title = new H2("Ajouter des articles");
        getContent().add(title);
        configureGrid();
    }

    private String translateType(String type) {
        switch (type) {
            case "book":
                return "Livre";
            case "magazine":
                return "Revue";
            case "board_game":
                return "Jeu";
            default:
                return type;
        }
    }

    private void addContent() {


        // un boutton pour ajouter un item (ouvre un modal)

        // Va donner la liste des items ajoutés depuis le début
        Grid<Item> gridItemAjouter = new Grid<>();

        // colonne titre

        // colonne type (livre, jeu, magasine)

        // colonne le nombre de copies

        // un boutton pour supprimer un item de la liste des items ajoutés (dans la grid)

        // un boutton pour soummettre les items ajoutés qui envoye pour les ajouter à la base de données
        // si il y a un succès, on propose la génération d'un fichier PDF avec les codes barres des copies ajoutées

    }

    private void addCopyForm(VerticalLayout copiesLayout) {
        FormLayout copyForm = new FormLayout();
        DatePicker purchaseDateField = new DatePicker("Date d'achat");
        NumberField purchasePriceField = new NumberField("Prix d'achat");

        // Optionnellement, ajouter un bouton pour supprimer cette copie
        Button removeCopyButton = new Button("Supprimer", e -> copiesLayout.remove(copyForm));
        removeCopyButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        copyForm.add(purchaseDateField, purchasePriceField, removeCopyButton);
        copiesLayout.add(copyForm);
    }

    private void saveItemWithCopies(Dialog dialog, VerticalLayout copiesLayout) {
        // Créer une liste pour stocker les copies
        List<Copy> copies = new ArrayList<>();

        // Parcourir les composants du copiesLayout pour collecter les données
        for (Component c : copiesLayout.getChildren().toList()) {
            if (c instanceof FormLayout) {
                FormLayout copyForm = (FormLayout) c;

                // Récupérer les champs du formulaire de copie
                DatePicker purchaseDateField = (DatePicker) copyForm.getChildren().toList().get(0);
                NumberField purchasePriceField = (NumberField) copyForm.getChildren().toList().get(1);

                // Créer un nouvel objet Copy
                Copy copy = new Copy();
                copy.setAcquisitionDate(Date.from(purchaseDateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                copy.setPrice(purchasePriceField.getValue());
                copy.setStatus("available"); // ou tout autre statut par défaut
                copy.setItem(itemModal); // Lier la copie à l'article
                copies.add(copy);
            }
        }

        // Maintenant, sauvegarder l'article et les copies
        if (itemModal != null) {
            // Ajouter les copies à l'article
            itemModal.getCopies().addAll(copies);

            // Ajouter l'article à la liste des articles à ajouter si ce n'est pas déjà fait
            if (!itemsToAdd.contains(itemModal)) {
                itemsToAdd.add(itemModal);
            }

            // Optionnellement, rafraîchir la grille ou le composant UI affichant itemsToAdd
            itemsGrid.getDataProvider().refreshAll();

            Notification.show("Article et copies enregistrés avec succès.");
            dialog.close();
        } else {
            Notification.show("Erreur : aucun article sélectionné.");
        }
    }

    // un modal pour ajouter un item
    private Dialog createAddModal() {

        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("Ajouter un article");

        // valider si il y a déjà un artcile avec le même identifiant

        ComboBox<String> typeComboBox = new ComboBox<>("Type");
        typeComboBox.setItems(Arrays.asList("Livre", "Magasine", "Jeu"));

        dialog.add(typeComboBox);



        FormLayout formLayout = new FormLayout();

        typeComboBox.addValueChangeListener(event -> {
            formLayout.removeAll();
            if (typeComboBox.getValue().equals("Livre")) {
                // un formulaire pour ajouter un livre
                this.ISBNField = new TextField("ISBN");

                formLayout.add(ISBNField);

            } else if (typeComboBox.getValue().equals("Magasine")) {
                // un formulaire pour ajouter un magasine
                this.isniField = new TextField("ISNI");
                this.monthComboBox = new ComboBox<>("Mois");
                List<MoisOption> listeDesMois = IntStream.rangeClosed(1, 12)
                        .mapToObj(i -> new MoisOption(
                                String.format("%02d", i),
                                Month.of(i).getDisplayName(TextStyle.FULL, Locale.FRENCH)))
                        .collect(Collectors.toList());

                monthComboBox.setItems(listeDesMois);
                monthComboBox.setItemLabelGenerator(MoisOption::getNom);

                //TODO Valider le format de l'année
                this.yearField = new IntegerField("Année");

                formLayout.add(isniField, monthComboBox, yearField);

            } else if (typeComboBox.getValue().equals("Jeu")) {
                // un formulaire pour ajouter un jeu
                this.gtinField = new TextField("GTIN");
                formLayout.add(gtinField);
            }

            Button chercherAddButton = new Button("Chercher");


            FormLayout formLayoutAjouterDonnees = new FormLayout();
            // TODO ajouter les champs required pour les champs de recherche
            chercherAddButton.addClickListener(eventChercher -> {
                formLayoutAjouterDonnees.removeAll();
                if (typeComboBox.getValue().equals("Livre")) {
                    // chercher un livre par ISBN
                    String isbn = ISBNField.getValue();
                    Book book = itemService.findBookByIsbn(isbn);
                    if (book != null) {
                        // ajouter l'article à la liste des articles à ajouter
                        itemModal = itemService.findItemById(book.getItemId());

                        Notification.show("Livre trouvé");

                        ISBNValueField = new TextField("ISBN");
                        ISBNValueField.setReadOnly(true);
                        ISBNValueField.setValue(book.getIsbn());

                        authorField = new TextField("Auteur");
                        authorField.setReadOnly(true);
                        authorField.setValue(book.getAuthor());

                        publicationDateField = new DatePicker("Date de publication");
                        publicationDateField.setReadOnly(true);
                        Date test = book.getPublicationDate();
                        publicationDateField.setValue(LocalDate.ofInstant(test.toInstant(), ZoneId.systemDefault()));

                        titleField = new TextField("Titre");
                        titleField.setReadOnly(true);
                        titleField.setValue(itemModal.getTitle());

                        formLayoutAjouterDonnees.add(ISBNValueField, authorField, publicationDateField, titleField);

                        // Après que l'article ait été trouvé ou créé, vous pouvez ajouter une section pour les copies
                        VerticalLayout copiesLayout = new VerticalLayout();
                        Button addCopyButton = new Button("Ajouter une copie", e -> addCopyForm(copiesLayout));

                        // Ajouter le premier formulaire de copie par défaut
                        addCopyForm(copiesLayout);

                        // Ajouter la section des copies au formulaire
                        formLayoutAjouterDonnees.add(copiesLayout, addCopyButton);

                        // Ajouter un bouton 'Enregistrer' pour sauvegarder l'article et les copies
                        Button saveButton = new Button("Enregistrer", e -> saveItemWithCopies(dialog, copiesLayout));
                        formLayoutAjouterDonnees.add(saveButton);

                        formLayout.add(formLayoutAjouterDonnees);

                    } else {
                        // afficher un message d'erreur

                    }
                } else if (typeComboBox.getValue().equals("Magasine")) {
                    // chercher un magasine par ISNI, mois et année
                    String isni = isniField.getValue();
                    String month = monthComboBox.getValue().getNumero();
                    String year = yearField.getValue().toString();
                    Magazine magazine = itemService.findMagazineByIsni(isni, month, year);
                    if (magazine != null) {
                        // ajouter l'article à la liste des articles à ajouter
                        itemModal = itemService.findItemById(magazine.getItemId());
                        Notification.show("Magasine trouvé");
                    } else {
                        // afficher un message d'erreur

                    }
                } else if (typeComboBox.getValue().equals("Jeu")) {
                    // chercher un jeu par GTIN
                    String gtin = gtinField.getValue();
                    BoardGame boardGame = itemService.findBoardGameByGtin(gtin);
                    if (boardGame != null) {
                        // ajouter l'article à la liste des articles à ajouter
                        itemModal = itemService.findItemById(boardGame.getItemId());
                        Notification.show("Jeu trouvé");

                    } else {
                        // afficher un message d'erreur
                    }
                }
            });



            dialog.add(formLayout, chercherAddButton);
        });



        // si existe déjà, on peut ajouter des copies

        // si n'existe pas, on peut ajouter un nouvel article (dans les tables items, books, magazines, board_games,
        // Catégories, publishers, suppliers, copies)


        return dialog;
    }

    // un modal pour éditer un item dans la liste des items ajoutés
    private Dialog createEditModal() {
        // un modal pour éditer un item
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("Éditer un article");


        return dialog;
    }

    private void submitItems() {
        try {
            for (Item item : itemsToAdd) {
                itemService.saveItem(item);
                itemsToAdd.remove(item);
            }

            Notification.show("Les articles ont été ajoutés avec succès");
            itemsToAdd.clear();
            itemsGrid.getDataProvider().refreshAll();
            submitButton.setEnabled(false);
        } catch (Exception e) {
            Notification.show("Une erreur s'est produite lors de l'ajout des articles");
        }
    }

    private void configureGrid() {

        // Ajouter les colonnes au grid
        itemsGrid.addColumn(Item::getTitle).setHeader("Titre");
        itemsGrid.addColumn(item -> translateType(item.getType())).setHeader("Type");
        itemsGrid.addColumn(item -> item.getCopies().size()).setHeader("Nombre de copies");
        itemsGrid.addComponentColumn(item -> createRemoveButton(itemsGrid, item)).setHeader("Actions");

        itemsGrid.addItemClickListener(event -> {
            Item item = event.getItem();
            Dialog dialog = createEditModal();
            dialog.open();
        });

        // setItems() permet de définir les items à afficher dans le grid
        itemsGrid.setItems(itemsToAdd);

        // Ajouter le grid à la vue
        getContent().add(itemsGrid);
    }

    private Button createRemoveButton(Grid<Item> grid, Item item) {
        Button removeButton = new Button("Supprimer", clickEvent -> {
            itemsToAdd.remove(item);
            grid.getDataProvider().refreshAll();
            if (itemsToAdd.isEmpty()) {
                submitButton.setEnabled(false);
            }
        });
        return removeButton;
    }

}
