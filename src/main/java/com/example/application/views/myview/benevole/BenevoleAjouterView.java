package com.example.application.views.myview.benevole;

import com.example.application.components.*;
import com.example.application.entity.*;
import com.example.application.entity.DTO.*;
import com.example.application.objectcustom.DocumentType;
import com.example.application.service.implementation.*;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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

    private BooksForm booksForm;
    private BoardGamesForm boardGamesForm;
    private MagazinesForm magazinesForm;

    private ItemsForm itemsForm;
    private CopiesForm copiesForm;

    private ComboBox<DocumentType> typeDocComboBox;
    private Button searchButton;
    private Button addAllButton;
    private VerticalLayout content;

    public BenevoleAjouterView( PublisherServiceV2 publisherService,
                                SupplierServiceV2 supplierService,
                                BookServiceV2 bookService,
                                BoardGameServiceV2 boardGameService,
                                MagazineServiceV2 magazineService,
                                CategoryServiceV2 categoryService,
                                ItemServiceV2 itemService,
                                CopyServiceV2 copyService) {

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
            searchButton = new Button("Rechercher"); // todo: empêcher d'ajouter plusieurs fois l'ui
            switch (type) {
                case "book":
                    content.removeAll();
                    booksForm = new BooksForm(bookService, itemService);

                    searchButton.addClickListener(e -> {
                        itemsForm = new ItemsForm(itemService, categoryService, publisherService, supplierService);
                        copiesForm = new CopiesForm(copyService);

                        BookDto booktemp = booksForm.searchBook();
                        if (booktemp != null) {

                            content.add(itemsForm, copiesForm, addAllButton);
                            itemsForm.setItemById(booktemp.getId());
                            copiesForm.setItem_id(booktemp.getId());
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
                    magazinesForm = new MagazinesForm(magazineService, itemService);

                    searchButton.addClickListener(e -> {
                        itemsForm = new ItemsForm(itemService, categoryService, publisherService, supplierService);
                        copiesForm = new CopiesForm(copyService);

                        MagazineDto magazinetemp = magazinesForm.searchMagazine();
                        if (magazinetemp != null) {
                            content.add(itemsForm, copiesForm, addAllButton);
                            itemsForm.setItemById(magazinetemp.getId());
                            copiesForm.setItem_id(magazinetemp.getId());
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
                    boardGamesForm = new BoardGamesForm(boardGameService, itemService);

                    searchButton.addClickListener(e -> {
                        itemsForm = new ItemsForm(itemService, categoryService, publisherService, supplierService);
                        copiesForm = new CopiesForm(copyService);

                        BoardGameDto boardGametemp = boardGamesForm.searchBoardGame();
                        if (boardGametemp != null) {

                            content.add(itemsForm, copiesForm, addAllButton);
                            itemsForm.setItemById(boardGametemp.getId());
                            copiesForm.setItem_id(boardGametemp.getId());
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

        Long idItem = itemsForm.getItem().getId();
        ItemDto item = itemsForm.getItem();

        String type = typeDocComboBox.getValue().getReturnValue();
        ItemDto resultItem = itemService.save(item);

        // Todo:
        // ne pas save si ça existe déjà
        // save si ça existe pas
        switch (type) {
            case "book":
                //booksForm.setbookItemid(idItem);
                //booksForm.saveBook(item);

                BookDto book = booksForm.getBookInfo();
                book.setItem(resultItem);
                bookService.insertBook(book);
                booksForm.setbookItemid(resultItem.getId());

                break;
            case "magazine":
                magazinesForm.setMagazineItemId(idItem);
                //magazinesForm.saveMagazine();
                break;
            case "board_game":
                boardGamesForm.setBoardGameItemId(idItem);
                //boardGamesForm.saveBoardGame();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

        System.out.println("livre OK");

        copiesForm.setItem(booksForm.getBookInfo().getItem());
        copiesForm.saveAllCopies();
    }
}
