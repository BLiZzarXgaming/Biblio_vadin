package com.example.application.views.myview.benevole;

import com.example.application.components.*;
import com.example.application.entity.*;
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

    private PublisherServiceImpl publisherService;
    private SupplierServiceImpl supplierService;
    private BookServiceImpl bookService;
    private BoardGameServiceImpl boardGameService;
    private MagazineServiceimpl magazineService;
    private CategoryServiceImpl categoryService;
    private ItemServiceImpl itemService;

    private PublishersForm publishersForm;
    private SuppliersFrom suppliersFrom;
    private BooksForm booksForm;
    private BoardGamesForm boardGamesForm;
    private MagazinesForm magazinesForm;
    private CategoriesForm categoriesForm;
    private ItemsForm itemsForm;

    public BenevoleAjouterView( PublisherServiceImpl publisherService, SupplierServiceImpl supplierService, BookServiceImpl bookService, BoardGameServiceImpl boardGameService,
                                MagazineServiceimpl magazineService, CategoryServiceImpl categoryService, ItemServiceImpl itemService) {

        this.publisherService = publisherService;
        this.supplierService = supplierService;
        this.bookService = bookService;
        this.boardGameService = boardGameService;
        this.magazineService = magazineService;
        this.categoryService = categoryService;
        this.itemService = itemService;

        getContent().setWidth("100%");
        getContent().setHeightFull();

        publishersForm = new PublishersForm(publisherService);
        suppliersFrom = new SuppliersFrom(supplierService);
        booksForm = new BooksForm(bookService);
        boardGamesForm = new BoardGamesForm(boardGameService);
        magazinesForm = new MagazinesForm(magazineService);
        categoriesForm = new CategoriesForm(categoryService);
        itemsForm = new ItemsForm(itemService, categoryService, publisherService, supplierService);

        getContent().add(publishersForm, suppliersFrom, booksForm, boardGamesForm, magazinesForm, categoriesForm, itemsForm);
    }
}
