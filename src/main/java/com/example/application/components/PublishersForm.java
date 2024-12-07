package com.example.application.components;

import com.example.application.entity.Publisher;
import com.example.application.service.implementation.PublisherServiceImpl;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;

public class PublishersForm extends HorizontalLayout {
    // name
    //contact_info
    //id

    private PublisherServiceImpl publisherService;
    private ComboBox<Publisher> publisherComboBox;
    private Dialog addPublisherDialog;
    private TextField nameField;
    private TextField contactInfoField;
    private Button addPublisherButton;
    private Notification notification;
    private boolean disableNotification = false;


    public PublishersForm(PublisherServiceImpl publisherService) {
        this.publisherService = publisherService;

        // Liste déroulante des publishers
        publisherComboBox = new ComboBox<>("Sélectionné l'éditeur");
        publisherComboBox.setItems(publisherService.findAll());
        publisherComboBox.setItemLabelGenerator(Publisher::getName);
        add(publisherComboBox);

        // Bouton pour ouvrir la modal
        addPublisherButton = new Button("Ajouter", event -> addPublisherDialog.open());
        add(addPublisherButton);

        addPublisherDialog = createAddPublisherDialog();
    }

    private Dialog createAddPublisherDialog() {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();
        nameField = new TextField("Nom");
        contactInfoField = new TextField("Information de contact");
        formLayout.add(nameField, contactInfoField);

        Button saveButton = new Button("Enregistrer", event -> savePublisher());
        Button cancelButton = new Button("Annuler", event -> dialog.close());
        dialog.add(formLayout, saveButton, cancelButton);

        return dialog;
    }

    public void disableAddBtn(boolean disable) {
        addPublisherButton.setEnabled(!disable);
        addPublisherButton.setVisible(!disable);
    }

    private void setSelectedPublisher(Publisher selectedPublisher) {
        publisherComboBox.setValue(selectedPublisher);
    }

    public void setSelectedPublisherById(Long id) {
        Publisher publisher = publisherService.findById(id);
        setSelectedPublisher(publisher);
    }

    //TODO : mettre enum
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

    public void setReadOnly(boolean readOnly) {
        publisherComboBox.setReadOnly(readOnly);
        disableAddBtn(readOnly);
    }

    public void savePublisher() {
        String name = nameField.getValue();
        String contactInfo = contactInfoField.getValue();

        if (name.isEmpty() || contactInfo.isEmpty()) {
            sendNotification("Veuillez remplir tous les champs", "error", 5000);
            return;
        }

        Publisher publisher = new Publisher();
        publisher.setName(name);
        publisher.setContactInfo(contactInfo);
        int result = publisherService.save(publisher);

        if (result == 0) {
            sendNotification("L'éditeur existe déjà", "error", 5000);
            return;
        }

        sendNotification("L'éditeur a été ajouté avec succès", "success", 5000);

        publisher = publisherService.findFirstByName(name);

        // Implémentez la logique pour sauvegarder le nouveau publisher
        publisherComboBox.setItems(publisherService.findAll()); // Rafraîchissez la liste des publishers
        setSelectedPublisher(publisher);
        addPublisherDialog.close();
    }

    public Publisher getSelectedPublisher() {
        return publisherComboBox.getValue();
    }

}
