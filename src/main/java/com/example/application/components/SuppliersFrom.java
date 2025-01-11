package com.example.application.components;

import com.example.application.entity.DTO.SupplierDto;
import com.example.application.entity.Supplier;
import com.example.application.service.implementation.SupplierServiceV2;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Optional;

public class SuppliersFrom extends HorizontalLayout {

    private SupplierServiceV2 supplierService;
    private ComboBox<SupplierDto> supplierComboBox;
    private Dialog addSupplierDialog;
    private TextField nameField;
    private TextField contactInfoField;
    private Button addSupplierButton;
    private Notification notification;
    private boolean disableNotification = false;


    public SuppliersFrom(SupplierServiceV2 supplierService) {
        this.supplierService = supplierService;

        // Liste déroulante des fournisseurs
        supplierComboBox = new ComboBox<>("Sélectionné le fournisseur");
        supplierComboBox.setItems(supplierService.findAll());
        supplierComboBox.setItemLabelGenerator(SupplierDto::getName);
        add(supplierComboBox);

        // Bouton pour ouvrir la modal
        addSupplierButton = new Button("Ajouter", event -> addSupplierDialog.open());
        add(addSupplierButton);

        addSupplierDialog = createAddSupplierDialog();
    }

    private Dialog createAddSupplierDialog() {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();
        nameField = new TextField("Nom");
        contactInfoField = new TextField("Information de contact");
        formLayout.add(nameField, contactInfoField);

        Button saveButton = new Button("Enregistrer", event -> saveSupplier());
        Button cancelButton = new Button("Annuler", event -> dialog.close());
        dialog.add(formLayout, saveButton, cancelButton);

        return dialog;
    }

    public void saveSupplier() {
        String name = nameField.getValue();
        String contactInfo = contactInfoField.getValue();

        if (name.isEmpty() || contactInfo.isEmpty()) {
            sendNotification("Veuillez remplir tous les champs", "error", 5000);
            return;
        }

        Optional<SupplierDto> supplier = Optional.of(new SupplierDto());
        supplier.get().setName(name);
        supplier.get().setContactInfo(contactInfo);
        SupplierDto result = supplierService.save(supplier.orElse(null));

        if (result == null) {
            sendNotification("Le fournisseur existe déjà", "error", 5000);
            return;
        }

        sendNotification("Le fournisseur a été ajouté avec succès", "success", 5000);

        supplier = supplierService.findByName(name);
        supplierComboBox.setItems(supplierService.findAll());
        supplierComboBox.setValue(supplier.get());
        addSupplierDialog.close();
    }

    public void disableAddBtn(boolean disable) {
        addSupplierButton.setEnabled(!disable);
        addSupplierButton.setVisible(!disable);
    }

    public SupplierDto getSelectedSupplier() {
        return supplierComboBox.getValue();
    }

    public void setSelectedSupplier(SupplierDto supplier) {
        supplierComboBox.setValue(supplier);
    }

    public void setSupplierbyName(String name) {
        Optional<SupplierDto> supplier = supplierService.findByName(name);
        supplierComboBox.setValue(supplier.get());
    }

    public void setSupplierById(Long id) {
        Optional<SupplierDto> supplier = supplierService.findById(id);
        supplierComboBox.setValue(supplier.get());
    }

    public void setReadOnly(boolean readOnly) {
        supplierComboBox.setReadOnly(readOnly);
        disableAddBtn(readOnly);
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
