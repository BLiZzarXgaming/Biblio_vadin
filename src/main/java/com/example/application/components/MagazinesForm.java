package com.example.application.components;

import com.example.application.entity.Magazine;
import com.example.application.objectcustom.MoisOption;
import com.example.application.service.implementation.MagazineServiceimpl;
import com.example.application.utils.DateUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;

public class MagazinesForm extends VerticalLayout {

    private MagazineServiceimpl magazinesService;

    private TextField isniField;
    private ComboBox<MoisOption> monthField;
    private DatePicker publicationDateField;
    private IntegerField yearField;
    private Long itemId;

    private Notification notification;
    private boolean disableNotification = false;

    public MagazinesForm(MagazineServiceimpl magazinesService) {
        this.magazinesService = magazinesService;
        itemId = null;

        FormLayout formLayout = new FormLayout();

        // ISNI field
        isniField = new TextField("ISNI");
        formLayout.add(isniField);

        // Month field
        monthField = new ComboBox<>("Mois");
        monthField.setItems(MoisOption.getListeMois());
        monthField.setItemLabelGenerator(MoisOption::getNom);
        formLayout.add(monthField);

        // Publication date field
        publicationDateField = new DatePicker("Date de publication");
        formLayout.add(publicationDateField);

        // Year field
        yearField = new IntegerField("Année");
        formLayout.add(yearField);

        add(formLayout);
    }

    public Magazine searchMagazine() {
        String isni = isniField.getValue();
        MoisOption month = monthField.getValue();
        Integer year = yearField.getValue();

        Magazine magazine = magazinesService.findByIsniAndMonthAndYear(isni, month.getNumero(), year.toString());
        if (magazine != null) {
            fillFields(magazine);
            return magazine;
        } else {
            sendNotification("Magazine non trouvé", "error", 5000);
            return null;
        }
    }

    public void saveMagazine() {
        String isni = isniField.getValue();
        MoisOption month = monthField.getValue();
        Integer year = yearField.getValue();
        LocalDate publicationDate = publicationDateField.getValue();

        if (isni == null || month == null || year == null || publicationDate == null || itemId == null) {
            sendNotification("Veuillez remplir tous les champs", "error", 5000);
            return;
        }

        Magazine magazine = new Magazine();
        magazine.setIsni(isni);
        magazine.setMonth(month.getNom());
        magazine.setYear(year.toString());
        magazine.setPublicationDate(java.sql.Date.valueOf(publicationDate));
        magazine.setItemId(itemId);
        int result = magazinesService.save(magazine);

        if (result == 0) {
            sendNotification("Le magazine existe déjà", "error", 5000);
            return;
        }

        sendNotification("Le magazine a été ajouté", "success", 3000);
    }

    public void setMagazineByIsni(String isni, String month, String year) {
        Magazine magazine = magazinesService.findByIsniAndMonthAndYear(isni, month, year);
        if (magazine != null) {
            fillFields(magazine);
        }
    }

    public void setMagazineByItemId(Long itemId) {
        Magazine magazine = magazinesService.findByItemId(itemId);
        if (magazine != null) {
            fillFields(magazine);
        }
    }

    public void setMagazineItemId(Long itemId) {
        this.itemId = itemId;
    }

    private void fillFields(Magazine magazine) {
        isniField.setValue(magazine.getIsni());
        monthField.setValue(new MoisOption(magazine.getMonth(), magazine.getMonth()));
        yearField.setValue(Integer.valueOf(magazine.getYear()));
        publicationDateField.setValue(DateUtils.convertToLocalDateViaInstant(magazine.getPublicationDate()));
        this.itemId = magazine.getItemId();

        setFieldsReadOnly(true);
    }

    public void setFieldsReadOnly(boolean readOnly) {
        isniField.setReadOnly(readOnly);
        monthField.setReadOnly(readOnly);
        yearField.setReadOnly(readOnly);
        publicationDateField.setReadOnly(readOnly);
    }

    public Magazine getMagazineInfo() {
        String isni = isniField.getValue();
        MoisOption month = monthField.getValue();
        Integer year = yearField.getValue();
        LocalDate publicationDate = publicationDateField.getValue();

        if (isni == null || month == null || year == null || publicationDate == null) {
            return null;
        }

        Magazine magazine = new Magazine();
        magazine.setIsni(isni);
        magazine.setMonth(month.getNom());
        magazine.setYear(year.toString());
        magazine.setPublicationDate(java.sql.Date.valueOf(publicationDate));
        magazine.setItemId(itemId);

        return magazine;
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
