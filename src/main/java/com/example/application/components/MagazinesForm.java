package com.example.application.components;

import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.DTO.MagazineDto;
import com.example.application.entity.Item;
import com.example.application.entity.Magazine;
import com.example.application.objectcustom.MoisOption;
import com.example.application.service.implementation.ItemServiceV2;
import com.example.application.service.implementation.MagazineServiceV2;
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
import java.util.Optional;

public class MagazinesForm extends VerticalLayout implements FormValidation {

    private MagazineServiceV2 magazinesService;
    private ItemServiceV2 itemService;

    private TextField isniField;
    private ComboBox<MoisOption> monthField;
    private DatePicker publicationDateField;
    private IntegerField yearField;
    private ItemDto item;

    private Notification notification;
    private boolean disableNotification = false;

    public MagazinesForm(MagazineServiceV2 magazinesService, ItemServiceV2 itemService) {
        this.magazinesService = magazinesService;
        this.itemService = itemService;
        item = null;

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

    public MagazineDto searchMagazine() {
        String isni = isniField.getValue();
        MoisOption month = monthField.getValue();
        Integer year = yearField.getValue();

        Optional<MagazineDto> magazine = magazinesService.findByIsniAndMonthAndYear(isni, month.getNumero(),
                year.toString());
        if (magazine.isPresent()) {
            fillFields(magazine.orElse(null));
            return magazine.orElse(null);
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

        if (isni == null || month == null || year == null || publicationDate == null || item == null) {
            sendNotification("Veuillez remplir tous les champs", "error", 5000);
            return;
        }

        MagazineDto magazine = new MagazineDto();
        magazine.setIsni(isni);
        magazine.setMonth(month.getNom());
        magazine.setYear(year.toString());
        magazine.setPublicationDate(publicationDate);
        magazine.setItem(item);
        MagazineDto result = magazinesService.save(magazine);

        if (result == null) {
            sendNotification("Le magazine existe déjà", "error", 5000);
            return;
        }

        sendNotification("Le magazine a été ajouté", "success", 3000);
    }

    public void setMagazineByIsni(String isni, String month, String year) {
        Optional<MagazineDto> magazine = magazinesService.findByIsniAndMonthAndYear(isni, month, year);
        if (magazine.isPresent()) {
            fillFields(magazine.orElse(null));
        }
    }

    public void setMagazineByItemId(Long itemId) {
        Optional<MagazineDto> magazine = magazinesService.findById(itemId);
        if (magazine.isPresent()) {
            fillFields(magazine.orElse(null));
        }
    }

    public void setMagazineItemId(Long itemId) {
        item = itemService.findById(itemId).orElse(null);
    }

    private void fillFields(MagazineDto magazine) {
        isniField.setValue(magazine.getIsni());
        monthField.setValue(new MoisOption(magazine.getMonth(), magazine.getMonth()));
        yearField.setValue(Integer.valueOf(magazine.getYear()));
        publicationDateField.setValue(magazine.getPublicationDate());

        item = itemService.findById(magazine.getId()).orElse(null);

        setFieldsReadOnly(true);
    }

    public void setFieldsReadOnly(boolean readOnly) {
        isniField.setReadOnly(readOnly);
        monthField.setReadOnly(readOnly);
        yearField.setReadOnly(readOnly);
        publicationDateField.setReadOnly(readOnly);
    }

    public MagazineDto getMagazineInfo() {
        String isni = isniField.getValue();
        MoisOption month = monthField.getValue();
        Integer year = yearField.getValue();
        LocalDate publicationDate = publicationDateField.getValue();

        if (isni == null || month == null || year == null || publicationDate == null) {
            return null;
        }

        MagazineDto magazine = new MagazineDto();
        magazine.setIsni(isni);
        magazine.setMonth(month.getNom());
        magazine.setYear(year.toString());
        magazine.setPublicationDate(publicationDate);
        magazine.setItem(item);

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

    public boolean validateForm() {
        return isniField.getValue() != null && !isniField.getValue().isEmpty() && monthField.getValue() != null
                && yearField.getValue() != null && publicationDateField.getValue() != null;
    }

}
