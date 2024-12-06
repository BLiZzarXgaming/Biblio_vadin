package com.example.application.components;

import com.example.application.entity.Supplier;
import com.example.application.service.implementation.SupplierServiceImpl;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;

public class SuppliersFrom extends VerticalLayout {

    private SupplierServiceImpl supplierService;
    private ComboBox<Supplier> supplierComboBox;
    private Dialog addSupplierDialog;
    private TextField nameField;
    private TextField contactInfoField;
    private Button addSupplierButton;
    private Notification notification;
    private boolean disableNotification = false;


    public SuppliersFrom(SupplierServiceImpl supplierService) {
        this.supplierService = supplierService;

        // Liste déroulante des fournisseurs
        supplierComboBox = new ComboBox<>("Sélectionné le fournisseur");
        supplierComboBox.setItems(supplierService.findAll());
        supplierComboBox.setItemLabelGenerator(Supplier::getName);
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

        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setContactInfo(contactInfo);
        int result = supplierService.save(supplier);

        if (result == 0) {
            sendNotification("Le fournisseur existe déjà", "error", 5000);
            return;
        }

        sendNotification("Le fournisseur a été ajouté avec succès", "success", 5000);

        supplier = supplierService.findFirstByName(name);
        supplierComboBox.setItems(supplierService.findAll());
        supplierComboBox.setValue(supplier);
        addSupplierDialog.close();
    }

    public void disableAddBtn(boolean disable) {
        addSupplierButton.setEnabled(!disable);
        addSupplierButton.setVisible(!disable);
    }

    public Supplier getSelectedSupplier() {
        return supplierComboBox.getValue();
    }

    public void setSelectedSupplier(Supplier supplier) {
        supplierComboBox.setValue(supplier);
    }

    public void setSupplierbyName(String name) {
        Supplier supplier = supplierService.findFirstByName(name);
        supplierComboBox.setValue(supplier);
    }

    public void setSupplierById(Long id) {
        Supplier supplier = supplierService.findById(id);
        supplierComboBox.setValue(supplier);
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
