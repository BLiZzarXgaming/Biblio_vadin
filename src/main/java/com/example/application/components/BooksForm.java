package com.example.application.components;

import com.example.application.entity.Book;
import com.example.application.entity.DTO.BookDto;
import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.Item;
import com.example.application.service.implementation.BookServiceV2;
import com.example.application.service.implementation.ItemServiceV2;
import com.example.application.utils.DateUtils;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public class BooksForm extends VerticalLayout implements FormValidation {

    private BookServiceV2 bookService;
    private ItemServiceV2 itemService;
    private TextField isbnField;
    private TextField authorField;
    private DatePicker publicationDateField;
    private Notification notification;
    private ItemDto item;
    private boolean disableNotification = false;

    public BooksForm(BookServiceV2 bookService, ItemServiceV2 itemService) {
        this.bookService = bookService;
        this.itemService = itemService;
        item = null;

        // Form layout
        FormLayout formLayout = new FormLayout();

        // ISBN field (read-only)
        isbnField = new TextField("ISBN");
        formLayout.add(isbnField);

        // Author field
        authorField = new TextField("Auteur");
        formLayout.add(authorField);

        // Publication date field
        publicationDateField = new DatePicker("Date de publication");
        formLayout.add(publicationDateField);

        add(formLayout);
    }

    public BookDto searchBook() {

        String isbn = isbnField.getValue();
        Optional<BookDto> book = bookService.findByIsbn(isbn);
        if (book.isPresent()) {
            fillFields(book.orElse(null));
            return book.orElse(null);
        } else {
            sendNotification("Livre non trouvé", "error", 5000);
            return null;
        }
    }

    public void saveBook(ItemDto itemtemp) {
        String isbn = isbnField.getValue();
        String author = authorField.getValue();
        LocalDate publicationDate = publicationDateField.getValue();

        if (author.isEmpty() || isbn.isEmpty() || publicationDate == null || item == null) {
            sendNotification("Veuillez remplir tout les champs du livre", "error", 5000);
            return;
        }

        BookDto book = new BookDto();
        book.setIsbn(isbn);
        book.setAuthor(author);
        book.setPublicationDate(publicationDate);
        book.setItem(itemtemp);
        BookDto result = null;
        try {
            result = bookService.save(book);
            sendNotification("Le livre a été ajouté", "success", 3000);
        } catch (Exception e) {
            sendNotification("Erreur lors de l'ajout du livre", "error", 5000);
            System.out.println(e.getMessage());
        }
        // bookService.save(book);

        if (result == null) {
            sendNotification("Le livre existe déjà", "error", 5000);
            return;
        }

        this.item = result.getItem();

        sendNotification("Le livre a été ajouté", "success", 3000);
    }

    public boolean setbookByIsbn(String isbn) {
        Optional<BookDto> book = bookService.findByIsbn(isbn);
        if (book.isPresent()) {
            fillFields(book.orElse(null));
            return true;
        }
        return false;
    }

    public void setBookByItemId(Long itemId) {
        Optional<BookDto> book = bookService.findById(itemId);
        if (book.isPresent()) {
            fillFields(book.orElse(null));
        }
    }

    public void setbookItemid(Long itemId) {

        Optional<ItemDto> item = itemService.findById(itemId);
        if (item.isPresent()) {
            this.item = item.orElse(null);
        }
    }

    private void fillFields(BookDto book) {
        isbnField.setValue(book.getIsbn());
        authorField.setValue(book.getAuthor());
        publicationDateField.setValue(book.getPublicationDate());
        item = book.getItem(); // itemService.findById(book.getId()).orElse(null);

        setFieldsReadOnly(true);
    }

    public void setFieldsReadOnly(boolean readOnly) {
        isbnField.setReadOnly(readOnly);
        authorField.setReadOnly(readOnly);
        publicationDateField.setReadOnly(readOnly);
    }

    public BookDto getBookInfo() {
        String isbn = isbnField.getValue();
        String author = authorField.getValue();
        LocalDate publicationDate = publicationDateField.getValue();

        if (isbn.isEmpty() || author.isEmpty() || publicationDate == null) {
            return null;
        }

        BookDto book = new BookDto();
        book.setIsbn(isbn);
        book.setAuthor(author);
        book.setPublicationDate(publicationDate);
        book.setItem(item);

        return book;
    }

    private void sendNotification(String message, String type, int duration) {
        if (type.equals("success")) {
            message = "Succès: " + message;
        } else if (type.equals("error")) {
            message = "Erreur: " + message;
        }

        notification = new Notification(message, duration, Notification.Position.BOTTOM_START);

        if (type.equals("success")) {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else if (type.equals("error")) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        if (!disableNotification || type.equals("error")) {
            notification.open();
        }
    }

    public boolean validateForm() {
        return isbnField.getValue() != null && !isbnField.getValue().isEmpty();
    }

    public void setDisableNotification(boolean disableNotification) {
        this.disableNotification = disableNotification;
    }
}
