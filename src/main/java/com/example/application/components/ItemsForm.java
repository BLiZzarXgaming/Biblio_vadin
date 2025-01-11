package com.example.application.components;

import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.Item;
import com.example.application.service.implementation.CategoryServiceV2;
import com.example.application.service.implementation.ItemServiceV2;
import com.example.application.service.implementation.PublisherServiceV2;
import com.example.application.service.implementation.SupplierServiceV2;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Optional;

public class ItemsForm extends VerticalLayout {

    // type
    //title
    // category (id)
    // publisher (id)
    // supplier (id)
    // value
    // link
    // id

    private ItemServiceV2 itemService;
    private CategoryServiceV2 categoryService;
    private PublisherServiceV2 publisherService;
    private SupplierServiceV2 supplierService;

    private String type;
    private TextField titleField;
    private CategoriesForm categoriesForm;
    private PublishersForm publishersForm;
    private SuppliersFrom suppliersFrom;
    private NumberField valueField;
    private TextField linkField;
    private Long id;

    private boolean disableNotification = false;
    private Notification notification;

    public ItemsForm(ItemServiceV2 itemService, CategoryServiceV2 categoryService, PublisherServiceV2 publisherService, SupplierServiceV2 supplierService) {
        this.itemService = itemService;
        this.categoryService = categoryService;
        this.publisherService = publisherService;
        this.supplierService = supplierService;

        FormLayout formLayout = new FormLayout();

        titleField = new TextField("Titre");
        formLayout.add(titleField);

        categoriesForm = new CategoriesForm(categoryService);
        formLayout.add(categoriesForm);

        publishersForm = new PublishersForm(publisherService);
        formLayout.add(publishersForm);

        suppliersFrom = new SuppliersFrom(supplierService);
        formLayout.add(suppliersFrom);

        valueField = new NumberField("Valeur");
        formLayout.add(valueField);

        linkField = new TextField("Lien");
        formLayout.add(linkField);

        add(formLayout);

    }

    public void setType(String type) {
        this.type = type;
    }

    public void saveItem() {
        String title = titleField.getValue();
        Long categoryId = categoriesForm.getSelectedCategory().getId();
        Long publisherId = publishersForm.getSelectedPublisher().getId();
        Long supplierId = suppliersFrom.getSelectedSupplier().getId();
        Double value = valueField.getValue();
        String link = linkField.getValue();

        if (title.isEmpty() || categoryId == null || publisherId == null || supplierId == null || value == null || link.isEmpty()) {
            sendNotification("Veuillez remplir tous les champs", "error", 5000);
            return;
        }

        ItemDto item = new ItemDto();
        item.setTitle(title);
        item.setCategory(categoriesForm.getSelectedCategory());
        item.setPublisher(publishersForm.getSelectedPublisher());
        item.setSupplier(suppliersFrom.getSelectedSupplier());
        item.setValue(value);
        item.setLink(link);
        item.setType(type);

        if (id != null) {
            item.setId(id);
        }

        ItemDto result =  itemService.save(item);

        if (result == null) {
            sendNotification("L'article existe déjà", "error", 5000);
            return;
        }

        setId(result.getId());

        sendNotification("L'article a été ajouté avec succès", "success", 5000);

    }

    public void setId(Long id) {
        this.id = id;
    }

    public void disableAddBtn(boolean disable) {
        categoriesForm.disableAddBtn(disable);
        publishersForm.disableAddBtn(disable);
        suppliersFrom.disableAddBtn(disable);
    }

    public void setReadeOnly(boolean readOnly) {
        titleField.setReadOnly(readOnly);
        categoriesForm.setReadOnly(readOnly);
        publishersForm.setReadOnly(readOnly);
        suppliersFrom.setReadOnly(readOnly);
        valueField.setReadOnly(readOnly);
        linkField.setReadOnly(readOnly);
    }

    public void setItemById(Long id) {
        Optional<ItemDto> item = itemService.findById(id);

        setId(item.get().getId());
        titleField.setValue(item.get().getTitle() != null ? item.get().getTitle() : "");
        categoriesForm.setSelectedCategory(item.get().getCategory().getId());
        publishersForm.setSelectedPublisherById(item.get().getPublisher().getId());
        suppliersFrom.setSelectedSupplier(item.get().getSupplier());
        valueField.setValue(item.get().getLink() != null ? item.get().getValue() : 0.0);
        linkField.setValue(item.get().getLink() != null ? item.get().getLink() : "");

        setReadeOnly(true);
    }

    public ItemDto getItem() {
        ItemDto item = new ItemDto();
        item.setId(id);
        item.setTitle(titleField.getValue());
        item.setCategory(categoriesForm.getSelectedCategory());
        item.setPublisher(publishersForm.getSelectedPublisher());
        item.setSupplier(suppliersFrom.getSelectedSupplier());
        item.setValue(valueField.getValue());
        item.setLink(linkField.getValue());
        item.setType(type);

        return item;
    }

    public void disableNotification(boolean disable) {
        disableNotification = disable;
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


}
