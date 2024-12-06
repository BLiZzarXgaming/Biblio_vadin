package com.example.application.components;

import com.example.application.entity.Category;
import com.example.application.service.implementation.CategoryServiceImpl;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class CategoriesForm extends VerticalLayout {
    // name
    // description
    // id

    private CategoryServiceImpl categoryService;

    private ComboBox<Category> categoryComboBox;
    private Dialog addCategoryDialog;
    private TextField nameField;
    private TextField descriptionField;
    private Long id;
    private Button addCategoryButton;
    private Notification notification;
    private boolean disableNotification = false;

    public CategoriesForm(CategoryServiceImpl categoryService) {
        this.categoryService = categoryService;

        // ComboBox for categories
        categoryComboBox = new ComboBox<>("Sélectionné la catégorie");
        categoryComboBox.setItems(categoryService.findAll());
        categoryComboBox.setItemLabelGenerator(Category::getName);
        add(categoryComboBox);

        // Button to open the dialog
        addCategoryButton = new Button("Ajouter une catégorie", event -> addCategoryDialog.open());
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

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        int result = categoryService.save(category);

        if (result == 0) {
            sendNotification("La categorie existe déjà", "error", 5000);
            return;
        }

        sendNotification("La catégorie a été ajoutée", "success", 5000);

        category = categoryService.findByName(name);

        categoryComboBox.setItems(categoryService.findAll());
        categoryComboBox.setValue(category);
        addCategoryDialog.close();
    }

    public void disableAddBtn(boolean disable) {
        addCategoryButton.setEnabled(!disable);
        addCategoryButton.setVisible(!disable);
    }

    public Category getSelectedCategory() {
        return categoryComboBox.getValue();
    }

    public void setSelectedCategory(Long id) {
        Category category = categoryService.findById(id);
        setSelectedCategory(category);
    }

    public void setReadOnly(boolean readOnly) {
        categoryComboBox.setReadOnly(readOnly);
        disableAddBtn(readOnly);
    }

    private void setSelectedCategory(Category category) {
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
