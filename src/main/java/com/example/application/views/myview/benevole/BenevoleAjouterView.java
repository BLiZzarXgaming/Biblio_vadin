package com.example.application.views.myview.benevole;

import com.example.application.components.*;
import com.example.application.entity.*;
import com.example.application.entity.DTO.*;
import com.example.application.objectcustom.DocumentType;
import com.example.application.service.implementation.*;
import com.example.application.utils.StatusUtils;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@PageTitle("Ajouter des articles")
@Route(value = "volunteer/add", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleAjouterView extends Composite<VerticalLayout> {

    private enum TypeDoc {
        MAGAZINE("magazine"),
        BOOK("book"),
        BOARDGAME("board_game");

        private final String value;

        TypeDoc(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private PublisherServiceV2 publisherService;
    private SupplierServiceV2 supplierService;
    private BookServiceV2 bookService;
    private BoardGameServiceV2 boardGameService;
    private MagazineServiceV2 magazineService;
    private CategoryServiceV2 categoryService;
    private ItemServiceV2 itemService;
    private CopyServiceV2 copyService;
    private DocumentAdditionService documentAdditionService;

    private BooksForm booksForm;
    private BoardGamesForm boardGamesForm;
    private MagazinesForm magazinesForm;

    private ItemsForm itemsForm;
    private CopiesForm copiesForm;

    private ComboBox<DocumentType> typeDocComboBox;
    private Button searchButton;
    private Button addAllButton;
    private VerticalLayout content;

    public BenevoleAjouterView(PublisherServiceV2 publisherService,
            SupplierServiceV2 supplierService,
            BookServiceV2 bookService,
            BoardGameServiceV2 boardGameService,
            MagazineServiceV2 magazineService,
            CategoryServiceV2 categoryService,
            ItemServiceV2 itemService,
            CopyServiceV2 copyService,
            DocumentAdditionService documentAdditionService) {

        this.publisherService = publisherService;
        this.supplierService = supplierService;
        this.bookService = bookService;
        this.boardGameService = boardGameService;
        this.magazineService = magazineService;
        this.categoryService = categoryService;
        this.itemService = itemService;
        this.copyService = copyService;
        this.documentAdditionService = documentAdditionService;
        getContent().setWidth("100%");
        getContent().setHeightFull();

        // getContent().add( booksForm, boardGamesForm, magazinesForm, itemsForm,
        // copiesForm);

        typeDocComboBox = new ComboBox<>("Type de document");

        content = new VerticalLayout();

        typeDocComboBox.setItems(
                new DocumentType(StatusUtils.DocTypes.LIVRE, StatusUtils.DocTypes.BOOK),
                new DocumentType(StatusUtils.DocTypes.REVUE, StatusUtils.DocTypes.MAGAZINE),
                new DocumentType(StatusUtils.DocTypes.JEU, StatusUtils.DocTypes.BOARD_GAME));

        typeDocComboBox.setItemLabelGenerator(DocumentType::getDisplayName);

        typeDocComboBox.addValueChangeListener(event -> {
            String type = event.getValue().getReturnValue();
            searchButton = new Button("Rechercher"); // todo: empêcher d'ajouter plusieurs fois l'ui
            switch (type) {
                case StatusUtils.DocTypes.BOOK:
                    content.removeAll();
                    booksForm = new BooksForm(bookService, itemService);

                    searchButton.addClickListener(e -> {
                        if (!booksForm.validateForm()) {
                            createNotification("Veuillez remplir le ISBN", "error");
                            return;
                        }

                        content.removeAll();
                        content.add(booksForm, searchButton);

                        itemsForm = new ItemsForm(itemService, categoryService, publisherService, supplierService);
                        copiesForm = new CopiesForm(copyService);

                        BookDto booktemp = booksForm.searchBook();
                        if (booktemp != null) {

                            content.add(itemsForm, copiesForm, addAllButton);
                            itemsForm.setItemById(booktemp.getId());
                            copiesForm.setItem_id(booktemp.getId());
                        } else {
                            content.add(itemsForm, copiesForm, addAllButton);
                        }
                        itemsForm.setType(TypeDoc.BOOK.getValue());
                    });

                    content.add(booksForm, searchButton);
                    break;
                case StatusUtils.DocTypes.MAGAZINE:
                    content.removeAll();
                    magazinesForm = new MagazinesForm(magazineService, itemService);

                    searchButton.addClickListener(e -> {
                        if (!magazinesForm.validateForm()) {
                            createNotification("Veuillez remplir le ISNI, le mois et l'année", "error");
                            return;
                        }

                        content.removeAll();
                        content.add(magazinesForm, searchButton);

                        itemsForm = new ItemsForm(itemService, categoryService, publisherService, supplierService);
                        copiesForm = new CopiesForm(copyService);

                        MagazineDto magazinetemp = magazinesForm.searchMagazine();
                        if (magazinetemp != null) {
                            content.add(itemsForm, copiesForm, addAllButton);
                            itemsForm.setItemById(magazinetemp.getId());
                            copiesForm.setItem_id(magazinetemp.getId());
                        } else {
                            content.add(itemsForm, copiesForm, addAllButton);
                        }
                        itemsForm.setType(TypeDoc.MAGAZINE.getValue());
                    });

                    content.add(magazinesForm, searchButton);
                    break;
                case StatusUtils.DocTypes.BOARD_GAME:
                    content.removeAll();
                    boardGamesForm = new BoardGamesForm(boardGameService, itemService);

                    searchButton.addClickListener(e -> {
                        if (!boardGamesForm.validateForm()) {
                            createNotification("Veuillez remplir le GTIN", "error");
                            return;
                        }

                        content.removeAll();
                        content.add(boardGamesForm, searchButton);

                        itemsForm = new ItemsForm(itemService, categoryService, publisherService, supplierService);
                        copiesForm = new CopiesForm(copyService);

                        BoardGameDto boardGametemp = boardGamesForm.searchBoardGame();
                        if (boardGametemp != null) {

                            content.add(itemsForm, copiesForm, addAllButton);
                            itemsForm.setItemById(boardGametemp.getId());
                            copiesForm.setItem_id(boardGametemp.getId());
                        } else {
                            content.add(itemsForm, copiesForm, addAllButton);
                        }
                        itemsForm.setType(TypeDoc.BOARDGAME.getValue());
                    });

                    content.add(boardGamesForm, searchButton);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
        });

        addAllButton = new Button("Ajouter", event -> addAll());
        addAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getContent().add(typeDocComboBox, content);
    }

    // TODO: ajouter des check pour les formulaires

    private void addAll() {
        ItemDto item = itemsForm.getItem();
        String type = typeDocComboBox.getValue().getReturnValue();

        if (item.getId() != null) {
            // Si le document existe déjà, ajouter uniquement les copies
            documentAdditionService.addCopiesToExistingDocument(item, copiesForm.getCopies());
            createNotification("Copies ajoutées avec succès au document existant", "success");
            return;
        }

        try {
            switch (type) {
                case StatusUtils.DocTypes.BOOK:
                    BookDto book = booksForm.getBookInfo();
                    ItemDto resultItem = documentAdditionService.addBook(item, book, copiesForm.getCopies());
                    booksForm.setbookItemid(resultItem.getId());
                    break;
                case StatusUtils.DocTypes.MAGAZINE:
                    MagazineDto magazine = magazinesForm.getMagazineInfo();
                    documentAdditionService.addMagazine(item, magazine, copiesForm.getCopies());
                    break;
                case StatusUtils.DocTypes.BOARD_GAME:
                    BoardGameDto boardGame = boardGamesForm.getBoardGame();
                    documentAdditionService.addBoardGame(item, boardGame, copiesForm.getCopies());
                    break;
                default:
                    throw new IllegalStateException("Type de document non supporté: " + type);
            }

            // Notification de succès
            createNotification("Document ajouté avec succès", "success");

        } catch (Exception e) {
            // Gestion d'erreur
            createNotification("Erreur lors de l'ajout: " + e.getMessage(), "error");
        }
    }

    private void createNotification(String message, String type) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(
                "error".equals(type)
                        ? NotificationVariant.LUMO_ERROR
                        : NotificationVariant.LUMO_SUCCESS);
    }
}
