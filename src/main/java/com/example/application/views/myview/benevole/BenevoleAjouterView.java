package com.example.application.views.myview.benevole;

import com.example.application.components.*;
import com.example.application.entity.*;
import com.example.application.objectcustom.DocumentType;
import com.example.application.objectcustom.MoisOption;
import com.example.application.service.implementation.*;
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
import com.vaadin.flow.data.provider.DataProvider;
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

    private PublisherServiceImpl publisherService;
    private SupplierServiceImpl supplierService;
    private BookServiceImpl bookService;
    private BoardGameServiceImpl boardGameService;
    private MagazineServiceimpl magazineService;
    private CategoryServiceImpl categoryService;
    private ItemServiceImpl itemService;
    private CopyServiceImpl copyService;

    private BooksForm booksForm;
    private BoardGamesForm boardGamesForm;
    private MagazinesForm magazinesForm;

    private ItemsForm itemsForm;
    private CopiesForm copiesForm;

    private ComboBox<DocumentType> typeDocComboBox;
    private Button searchButton;
    private Button addAllButton;
    private VerticalLayout content;

    public BenevoleAjouterView( PublisherServiceImpl publisherService, SupplierServiceImpl supplierService, BookServiceImpl bookService, BoardGameServiceImpl boardGameService,
                                MagazineServiceimpl magazineService, CategoryServiceImpl categoryService, ItemServiceImpl itemService, CopyServiceImpl copyService) {

        this.publisherService = publisherService;
        this.supplierService = supplierService;
        this.bookService = bookService;
        this.boardGameService = boardGameService;
        this.magazineService = magazineService;
        this.categoryService = categoryService;
        this.itemService = itemService;
        this.copyService = copyService;

        getContent().setWidth("100%");
        getContent().setHeightFull();

        //getContent().add( booksForm, boardGamesForm, magazinesForm, itemsForm, copiesForm);

        typeDocComboBox = new ComboBox<>("Type de document");

        content = new VerticalLayout();


        typeDocComboBox.setItems(
                new DocumentType("livre", "book"),
                new DocumentType("magazine", "magazine"),
                new DocumentType("jeu", "board_game")
        );

        typeDocComboBox.setItemLabelGenerator(DocumentType::getDisplayName);

        typeDocComboBox.addValueChangeListener(event -> {
            String type = event.getValue().getReturnValue();
            searchButton = new Button("Rechercher");
            switch (type) {
                case "book":
                    content.removeAll();
                    booksForm = new BooksForm(bookService);

                    searchButton.addClickListener(e -> {
                        itemsForm = new ItemsForm(itemService, categoryService, publisherService, supplierService);
                        copiesForm = new CopiesForm(copyService);

                        Book booktemp = booksForm.searchBook();
                        if (booktemp != null) {

                            content.add(itemsForm, copiesForm, addAllButton);
                            itemsForm.setItemById(booktemp.getItemId());
                            copiesForm.setItem_id(booktemp.getItemId());
                        }
                        else {
                            content.add(itemsForm, copiesForm, addAllButton);
                        }
                        itemsForm.setType(TypeDoc.BOOK.getValue());
                    });


                    content.add(booksForm, searchButton);
                    break;
                case "magazine":
                    content.removeAll();
                    magazinesForm = new MagazinesForm(magazineService);

                    searchButton.addClickListener(e -> {
                        itemsForm = new ItemsForm(itemService, categoryService, publisherService, supplierService);
                        copiesForm = new CopiesForm(copyService);

                        Magazine magazinetemp = magazinesForm.searchMagazine();
                        if (magazinetemp != null) {
                            content.add(itemsForm, copiesForm, addAllButton);
                            itemsForm.setItemById(magazinetemp.getItemId());
                            copiesForm.setItem_id(magazinetemp.getItemId());
                        }
                        else {
                            content.add(itemsForm, copiesForm, addAllButton);
                        }
                        itemsForm.setType(TypeDoc.MAGAZINE.getValue());
                    });


                    content.add(magazinesForm, searchButton);
                    break;
                case "board_game":
                    content.removeAll();
                    boardGamesForm = new BoardGamesForm(boardGameService);

                    searchButton.addClickListener(e -> {
                        itemsForm = new ItemsForm(itemService, categoryService, publisherService, supplierService);
                        copiesForm = new CopiesForm(copyService);

                        BoardGame boardGametemp = boardGamesForm.searchBoardGame();
                        if (boardGametemp != null) {

                            content.add(itemsForm, copiesForm, addAllButton);
                            itemsForm.setItemById(boardGametemp.getItemId());
                            copiesForm.setItem_id(boardGametemp.getItemId());
                        }
                        else {
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


        itemsForm.saveItem();

        Long idItem = itemsForm.getItem().getId();
        Item item = itemsForm.getItem();

        String type = typeDocComboBox.getValue().getReturnValue();

        switch (type) {
            case "book":
                booksForm.setbookItemid(idItem);
                booksForm.saveBook();
                break;
            case "magazine":
                magazinesForm.setMagazineItemId(idItem);
                magazinesForm.saveMagazine();
                break;
            case "board_game":
                boardGamesForm.setBoardGameItemId(idItem);
                boardGamesForm.saveBoardGame();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

        copiesForm.setItem(item);
        copiesForm.saveAllCopies();
    }
}
