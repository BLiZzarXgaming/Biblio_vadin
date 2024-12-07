package com.example.application.components;

import com.example.application.entity.Book;
import com.example.application.service.implementation.BookServiceImpl;
import com.example.application.utils.DateUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;

public class BooksForm extends VerticalLayout {

    private  BookServiceImpl bookService;
    private TextField isbnField;
    private TextField authorField;
    private DatePicker publicationDateField;
    private Notification notification;
    private Long item_id;
    private boolean disableNotification = false;

    public BooksForm(BookServiceImpl bookService) {
        this.bookService = bookService;
        item_id = null;

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

    public Book searchBook() {

        String isbn = isbnField.getValue();
        Book book = bookService.findByIsbn(isbn);
        if (book != null) {
            fillFields(book);
            return book;
        } else {
            sendNotification("Livre non trouvé", "error", 5000);
            return null;
        }
    }

    public void saveBook() {
        String isbn = isbnField.getValue();
        String author = authorField.getValue();
        LocalDate publicationDate = publicationDateField.getValue();

        if (author.isEmpty() || isbn.isEmpty() || publicationDate == null || item_id == null) {
            sendNotification("Veuillez remplir tout les champs du livre", "error", 5000);
            return;
        }

        Book book = new Book();
        book.setIsbn(isbn);
        book.setAuthor(author);
        book.setPublicationDate(java.sql.Date.valueOf(publicationDate));
        book.setItemId(item_id);
        int result = bookService.save(book);

        if (result == 0) {
            sendNotification("Le livre existe déjà", "error", 5000);
            return;
        }

        sendNotification("Le livre a été ajouté", "success", 3000);
    }

    public boolean setbookByIsbn(String isbn) {
        Book book = bookService.findByIsbn(isbn);
        if (book != null) {
            fillFields(book);
            return true;
        }
        return false;
    }

    public void setBookByItemId(Long itemId) {
        Book book = bookService.findByItemId(itemId);
        if (book != null) {
            fillFields(book);
        }
    }

    public void setbookItemid(Long itemId) {
        item_id = itemId;
    }

    private void fillFields(Book book) {
        isbnField.setValue(book.getIsbn());
        authorField.setValue(book.getAuthor());
        publicationDateField.setValue(DateUtils.convertToLocalDateViaInstant(book.getPublicationDate()));
        item_id = book.getItemId();

        setFieldsReadOnly(true);
    }

    public void setFieldsReadOnly(boolean readOnly) {
        isbnField.setReadOnly(readOnly);
        authorField.setReadOnly(readOnly);
        publicationDateField.setReadOnly(readOnly);
    }

    public Book getBookInfo() {
        String isbn = isbnField.getValue();
        String author = authorField.getValue();
        LocalDate publicationDate = publicationDateField.getValue();

        if (isbn.isEmpty() || author.isEmpty() || publicationDate == null) {
            return null;
        }

        Book book = new Book();
        book.setIsbn(isbn);
        book.setAuthor(author);
        book.setPublicationDate(java.sql.Date.valueOf(publicationDate));
        book.setItemId(item_id);

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

    public void setDisableNotification(boolean disableNotification) {
        this.disableNotification = disableNotification;
    }
}
