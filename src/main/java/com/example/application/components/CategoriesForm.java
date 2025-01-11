package com.example.application.components;

import com.example.application.entity.Category;
import com.example.application.entity.DTO.CategoryDto;
import com.example.application.service.implementation.CategoryServiceV2;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Optional;

public class CategoriesForm extends HorizontalLayout {
    // name
    // description
    // id

    private CategoryServiceV2 categoryService;

    private ComboBox<CategoryDto> categoryComboBox;
    private Dialog addCategoryDialog;
    private TextField nameField;
    private TextField descriptionField;
    private Long id;
    private Button addCategoryButton;
    private Notification notification;
    private boolean disableNotification = false;

    public CategoriesForm(CategoryServiceV2 categoryService) {
        this.categoryService = categoryService;

        // ComboBox for categories
        categoryComboBox = new ComboBox<>("Sélectionné la catégorie");
        categoryComboBox.setItems(categoryService.findAll());
        categoryComboBox.setItemLabelGenerator(CategoryDto::getName);
        add(categoryComboBox);

        // Button to open the dialog
        addCategoryButton = new Button("Ajouter", event -> addCategoryDialog.open());
        add(addCategoryButton);

        addCategoryDialog = createAddCategoryDialog();
    }

    private Dialog createAddCategoryDialog() {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();
        nameField = new TextField("Nom");
        descriptionField = new TextField("Description");
        formLayout.add(nameField, descriptionField);

        Button saveButton = new Button("Enregistrer", event -> saveCategory());
        Button cancelButton = new Button("Annuler", event -> dialog.close());
        dialog.add(formLayout, saveButton, cancelButton);

        return dialog;
    }

    public void saveCategory() {
        String name = nameField.getValue();
        String description = descriptionField.getValue();

        if (name.isEmpty() || description.isEmpty()) {
            sendNotification("Veuillez remplir tous les champs", "error", 5000);
            return;
        }

        Optional<CategoryDto> category = Optional.of(new CategoryDto());
        category.get().setName(name);
        category.get().setDescription(description);
        CategoryDto result = categoryService.save(category.orElse(null));

        if (result == null) {
            sendNotification("La categorie existe déjà", "error", 5000);
            return;
        }

        sendNotification("La catégorie a été ajoutée", "success", 5000);

        category = categoryService.findByName(name);

        categoryComboBox.setItems(categoryService.findAll());
        categoryComboBox.setValue(category.get());
        addCategoryDialog.close();
    }

    public void disableAddBtn(boolean disable) {
        addCategoryButton.setEnabled(!disable);
        addCategoryButton.setVisible(!disable);
    }

    public CategoryDto getSelectedCategory() {
        return categoryComboBox.getValue();
    }

    public void setSelectedCategory(Long id) {
        Optional<CategoryDto> category = categoryService.findById(id);
        setSelectedCategory(category.orElse(null));
    }

    public void setReadOnly(boolean readOnly) {
        categoryComboBox.setReadOnly(readOnly);
        disableAddBtn(readOnly);
    }

    private void setSelectedCategory(CategoryDto category) {
        categoryComboBox.setValue(category);
    }

    private void sendNotification(String message, String type, int duration) {
        if (type.equals("success")) {
            message = "Success: " + message;
        } else if (type.equals("error")) {
            message = "Error: " + message;
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
