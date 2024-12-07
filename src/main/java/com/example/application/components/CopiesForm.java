package com.example.application.components;

import com.example.application.entity.Copy;
import com.example.application.entity.Item;
import com.example.application.service.implementation.CopyServiceImpl;
import com.example.application.utils.DateUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CopiesForm extends VerticalLayout {

    private enum Type {
        AVAILABLE("available"),
        UNAVAILABLE("unavailable"),
        BORROWED("borrowed"),
        RESERVED("reserved"),
        DELETED("deleted");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    //item_id
    //status
    //acquisition_date
    //price
    //id

    private CopyServiceImpl copyService;

    private Long item_id;
    private String status;
    private DatePicker acquisition_date;
    private NumberField price;
    private Long id;
    private Grid<Copy> copiesGrid;
    private List<Copy> copiesDataGrid;
    private Button addCopyButton;

    private Notification notification;
    private boolean disableNotification = false;

    private Dialog editDialog;
    private DatePicker editAcquisitionDate;
    private NumberField editPrice;
    private Button saveEditButton;

    private Long idTempscount = 1L;
    private Item item;


    public CopiesForm(CopyServiceImpl copyService) {
        this.copyService = copyService;

        FormLayout formLayout = new FormLayout();

        acquisition_date = new DatePicker("Date d'acquisition");
        formLayout.add(acquisition_date);

        price = new NumberField("Prix");
        formLayout.add(price);

        add(formLayout);

        addCopyButton = new Button("Ajouter une copie", event -> addCopy());

        add(addCopyButton);

        copiesDataGrid = new ArrayList<Copy>();

        copiesGrid = new Grid<>();
        copiesGrid.addColumn(Copy::getAcquisitionDate).setHeader("Date d'acquisition");
        copiesGrid.addColumn(Copy::getPrice).setHeader("Prix");
        copiesGrid.addComponentColumn(copy -> {
            com.vaadin.flow.component.button.Button editButton = new Button("Modifier");
            editButton.addClickListener(event -> openEditDialog(copy));
            Button deleteButton = new Button("Supprimer", event -> deleteCopy(copy));
            HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
            return actions;
        }).setHeader("Actions");
        add(copiesGrid);
        createEditDialog();
    }

    private void createEditDialog() {
        editDialog = new Dialog();
        FormLayout editFormLayout = new FormLayout();

        editAcquisitionDate = new DatePicker("Date d'acquisition");
        editFormLayout.add(editAcquisitionDate);

        editPrice = new NumberField("Prix");
        editFormLayout.add(editPrice);

        saveEditButton = new Button("Enregistrer", event -> saveEditCopy());
        editFormLayout.add(saveEditButton);

        editDialog.add(editFormLayout);
    }

    private void openEditDialog(Copy copy) {
        editAcquisitionDate.setValue(DateUtils.convertToLocalDateViaInstant(copy.getAcquisitionDate()));
        editPrice.setValue(copy.getPrice());
        id = copy.getId();
        editDialog.open();
    }

    private void saveEditCopy() {
        Copy copy = copiesDataGrid.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
        if (copy != null) {
            copy.setAcquisitionDate(DateUtils.convertToDateViaInstant(editAcquisitionDate.getValue()));
            copy.setPrice(editPrice.getValue());
            updateGrid();
            sendNotification("Copie modifiée avec succès", "success", 3000);

            editPrice.clear();
            editAcquisitionDate.clear();
        }
        editDialog.close();
    }

    private void updateGrid() {
        copiesGrid.setItems(copiesDataGrid);
    }

    private void addCopy() {
        Copy copy = new Copy();
        copy.setId(idTempscount);
        idTempscount++;
        copy.setAcquisitionDate(DateUtils.convertToDateViaInstant(acquisition_date.getValue()));
        copy.setPrice(price.getValue());
        copy.setStatus(Type.AVAILABLE.getValue());
        copiesDataGrid.add(copy);
        updateGrid();

        price.clear();
        acquisition_date.clear();
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void saveAllCopies() {
        for (Copy copy : copiesDataGrid) {
            copy.setItem(item);
            copy.setId(null);
            copyService.save(copy);
        }
        sendNotification("Toutes les copies ont été enregistrées avec succès", "success", 3000);
    }

    private void editCopy(Copy copy) {
        // Logic to edit the copy
    }

    private void deleteCopy(Copy copy) {
        copiesDataGrid.remove(copy);
        updateGrid();
    }

    public void setItem_id(Long item_id) {
        this.item_id = item_id;
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