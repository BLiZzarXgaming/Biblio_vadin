package com.example.application.views.myview.admin;

import com.example.application.entity.DTO.LoanSettingDto;
import com.example.application.entity.DTO.SpecialLimitDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.SpecialLimit;
import com.example.application.entity.User;
import com.example.application.service.implementation.LoanSettingServiceV2;
import com.example.application.service.implementation.SpecialLimitService;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.utils.StatusUtils;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Gestion des limites d'emprunt")
@Route(value = "admin/limits", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMINISTRATEUR")
public class AdminLimitsView extends VerticalLayout {

    private final LoanSettingServiceV2 loanSettingService;
    private final UserServiceV2 userService;
    private final SpecialLimitService specialLimitService;

    // UI components
    private VerticalLayout globalSettingsLayout;
    private VerticalLayout specialLimitsLayout;
    private Tabs tabs;
    private TextField searchField;
    private Grid<UserDto> membersGrid;
    private LoanSettingDto globalSettings;

    public AdminLimitsView(LoanSettingServiceV2 loanSettingService,
            UserServiceV2 userService,
            SpecialLimitService specialLimitService) {

        this.loanSettingService = loanSettingService;
        this.userService = userService;
        this.specialLimitService = specialLimitService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 viewTitle = new H2("Gestion des limites d'emprunt");
        add(viewTitle);

        // Create tabs for switching between global and special limits
        createTabs();

        // Create layouts for both tabs
        createGlobalSettingsLayout();
        createSpecialLimitsLayout();

        // Load initial data
        loadGlobalSettings();

        // Show the first tab by default
        showGlobalSettingsTab();
    }

    private void createTabs() {
        Tab globalSettingsTab = new Tab("Paramètres globaux");
        Tab specialLimitsTab = new Tab("Limites spéciales");

        tabs = new Tabs(globalSettingsTab, specialLimitsTab);
        tabs.setWidthFull();

        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(globalSettingsTab)) {
                showGlobalSettingsTab();
            } else {
                showSpecialLimitsTab();
            }
        });

        add(tabs);
    }

    private void createGlobalSettingsLayout() {
        globalSettingsLayout = new VerticalLayout();
        globalSettingsLayout.setWidthFull();
        globalSettingsLayout.setPadding(true);
        globalSettingsLayout.setSpacing(true);

        H3 title = new H3("Paramètres globaux d'emprunt");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        IntegerField loanDurationField = new IntegerField("Durée d'emprunt (jours)");
        loanDurationField.setMin(1);
        loanDurationField.setMax(90);

        IntegerField maxLoansAdultField = new IntegerField("Nombre max. d'emprunts (adultes)");
        maxLoansAdultField.setMin(1);
        maxLoansAdultField.setMax(50);

        IntegerField maxLoansChildField = new IntegerField("Nombre max. d'emprunts (enfants)");
        maxLoansChildField.setMin(1);
        maxLoansChildField.setMax(50);

        IntegerField maxReservationsAdultField = new IntegerField("Nombre max. de réservations (adultes)");
        maxReservationsAdultField.setMin(1);
        maxReservationsAdultField.setMax(50);

        IntegerField maxReservationsChildField = new IntegerField("Nombre max. de réservations (enfants)");
        maxReservationsChildField.setMin(1);
        maxReservationsChildField.setMax(50);

        Button saveButton = new Button("Enregistrer");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            if (validateGlobalSettingsForm(loanDurationField, maxLoansAdultField, maxLoansChildField,
                    maxReservationsAdultField, maxReservationsChildField)) {

                globalSettings.setLoanDurationDays(loanDurationField.getValue());
                globalSettings.setMaxLoansAdult(maxLoansAdultField.getValue());
                globalSettings.setMaxLoansChild(maxLoansChildField.getValue());
                globalSettings.setMaxReservationsAdult(maxReservationsAdultField.getValue());
                globalSettings.setMaxReservationsChild(maxReservationsChildField.getValue());

                saveGlobalSettings();
            }
        });

        formLayout.add(loanDurationField, maxLoansAdultField, maxLoansChildField,
                maxReservationsAdultField, maxReservationsChildField, saveButton);

        globalSettingsLayout.add(title, formLayout);

        // Store references to fields for updating
        globalSettingsLayout.getElement().setProperty("loanDurationField", true);
        loanDurationField.getElement().setProperty("_component", true);
        maxLoansAdultField.getElement().setProperty("_component", true);
        maxLoansChildField.getElement().setProperty("_component", true);
        maxReservationsAdultField.getElement().setProperty("_component", true);
        maxReservationsChildField.getElement().setProperty("_component", true);

        add(globalSettingsLayout);
        globalSettingsLayout.setVisible(false);
    }

    private void createSpecialLimitsLayout() {
        specialLimitsLayout = new VerticalLayout();
        specialLimitsLayout.setWidthFull();
        specialLimitsLayout.setPadding(true);
        specialLimitsLayout.setSpacing(true);

        H3 title = new H3("Limites spéciales par membre");

        // Search field
        searchField = new TextField("Rechercher un membre");
        searchField.setPlaceholder("Nom, prénom ou nom d'utilisateur");
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(500);
        searchField.addValueChangeListener(e -> loadMembers(e.getValue()));

        // Create members grid
        membersGrid = new Grid<>();
        membersGrid.addColumn(UserDto::getUsername).setHeader("Nom d'utilisateur").setSortable(true);
        membersGrid.addColumn(UserDto::getFirstName).setHeader("Prénom").setSortable(true);
        membersGrid.addColumn(UserDto::getLastName).setHeader("Nom").setSortable(true);

        // Add a column to show if the member has special limits
        membersGrid.addColumn(new ComponentRenderer<>(user -> {
            User userEntity = new User();
            userEntity.setId(user.getId());

            Optional<SpecialLimit> specialLimit = specialLimitService.findFirstByUserOrderByCreatedAtDesc(userEntity);

            if (specialLimit.isPresent() && StatusUtils.SpecialLimit.ACTIVE.equals(specialLimit.get().getStatus())) {
                Span badge = new Span("Limites spéciales");
                badge.getElement().getThemeList().add("badge");
                badge.getElement().getThemeList().add("success");
                return badge;
            } else {
                Span badge = new Span("Limites globales");
                badge.getElement().getThemeList().add("badge");
                badge.getElement().getThemeList().add("contrast");
                return badge;
            }
        })).setHeader("Type de limites").setAutoWidth(true);

        // Add columns to show current limits
        membersGrid.addColumn(user -> {
            User userEntity = new User();
            userEntity.setId(user.getId());
            Optional<SpecialLimit> specialLimit = specialLimitService.findFirstByUserOrderByCreatedAtDesc(userEntity);

            if (specialLimit.isPresent() && StatusUtils.SpecialLimit.ACTIVE.equals(specialLimit.get().getStatus())) {
                return specialLimit.get().getMaxLoans();
            } else {
                boolean isChild = user.getIsChild() != null && user.getIsChild();
                if (isChild) {
                    return globalSettings.getMaxLoansChild();
                } else {
                    return globalSettings.getMaxLoansAdult();
                }
            }
        }).setHeader("Emprunts max.").setAutoWidth(true);

        membersGrid.addColumn(user -> {
            User userEntity = new User();
            userEntity.setId(user.getId());
            Optional<SpecialLimit> specialLimit = specialLimitService.findFirstByUserOrderByCreatedAtDesc(userEntity);

            if (specialLimit.isPresent() && StatusUtils.SpecialLimit.ACTIVE.equals(specialLimit.get().getStatus())) {
                return specialLimit.get().getMaxReservations();
            } else {
                boolean isChild = user.getIsChild() != null && user.getIsChild();
                if (isChild) {
                    return globalSettings.getMaxReservationsChild();
                } else {
                    return globalSettings.getMaxReservationsAdult();
                }
            }
        }).setHeader("Réservations max.").setAutoWidth(true);

        // Add buttons for actions
        membersGrid.addComponentColumn(user -> {
            Button editButton = new Button("Modifier");
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            editButton.addClickListener(e -> openSpecialLimitDialog(user));
            return editButton;
        }).setHeader("Actions").setAutoWidth(true);

        membersGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        membersGrid.setHeight("500px");

        specialLimitsLayout.add(title, searchField, membersGrid);
        add(specialLimitsLayout);
        specialLimitsLayout.setVisible(false);
    }

    private void showGlobalSettingsTab() {
        globalSettingsLayout.setVisible(true);
        specialLimitsLayout.setVisible(false);
    }

    private void showSpecialLimitsTab() {
        globalSettingsLayout.setVisible(false);
        specialLimitsLayout.setVisible(true);
        loadMembers("");
    }

    private boolean validateGlobalSettingsForm(
            IntegerField loanDurationField,
            IntegerField maxLoansAdultField,
            IntegerField maxLoansChildField,
            IntegerField maxReservationsAdultField,
            IntegerField maxReservationsChildField) {

        if (loanDurationField.getValue() == null ||
                maxLoansAdultField.getValue() == null ||
                maxLoansChildField.getValue() == null ||
                maxReservationsAdultField.getValue() == null ||
                maxReservationsChildField.getValue() == null) {

            showNotification("Veuillez remplir tous les champs", NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    private void loadGlobalSettings() {
        Optional<LoanSettingDto> settings = loanSettingService.findById(1L);

        if (settings.isEmpty()) {
            showNotification("Paramètres d'emprunt non trouvés", NotificationVariant.LUMO_ERROR);
            return;
        }

        globalSettings = settings.get();

        // Find UI components and set their values
        IntegerField loanDurationField = (IntegerField) globalSettingsLayout.getChildren()
                .filter(component -> component instanceof FormLayout)
                .findFirst()
                .map(formLayout -> ((FormLayout) formLayout).getChildren()
                        .filter(field -> field instanceof IntegerField)
                        .findFirst()
                        .orElse(null))
                .orElse(null);

        if (loanDurationField != null) {
            FormLayout formLayout = (FormLayout) loanDurationField.getParent().get();

            List<IntegerField> fields = formLayout.getChildren()
                    .filter(component -> component instanceof IntegerField)
                    .map(component -> (IntegerField) component)
                    .collect(Collectors.toList());

            if (fields.size() >= 5) {
                fields.get(0).setValue(globalSettings.getLoanDurationDays());
                fields.get(1).setValue(globalSettings.getMaxLoansAdult());
                fields.get(2).setValue(globalSettings.getMaxLoansChild());
                fields.get(3).setValue(globalSettings.getMaxReservationsAdult());
                fields.get(4).setValue(globalSettings.getMaxReservationsChild());
            }
        }
    }

    private void saveGlobalSettings() {
        try {
            loanSettingService.save(globalSettings);
            showNotification("Paramètres globaux enregistrés avec succès", NotificationVariant.LUMO_SUCCESS);

            // Refresh members grid if it's visible
            if (specialLimitsLayout.isVisible()) {
                loadMembers(searchField.getValue());
            }
        } catch (Exception e) {
            showNotification("Erreur lors de l'enregistrement des paramètres: " + e.getMessage(),
                    NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadMembers(String searchTerm) {
        if (membersGrid == null) {
            return;
        }

        List<UserDto> members;

        if (searchTerm == null || searchTerm.isEmpty()) {
            // Get all members
            members = userService.findAll().stream()
                    .filter(user -> user.getRole() != null && StatusUtils.RoleName.MEMBRE.equals(user.getRole().getName()))
                    .collect(Collectors.toList());
        } else {
            // Filter members by search term
            String finalSearchTerm = searchTerm.toLowerCase();
            members = userService.findAll().stream()
                    .filter(user -> user.getRole() != null && StatusUtils.RoleName.MEMBRE.equals(user.getRole().getName()))
                    .filter(user -> (user.getUsername() != null
                            && user.getUsername().toLowerCase().contains(finalSearchTerm)) ||
                            (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(finalSearchTerm))
                            ||
                            (user.getLastName() != null && user.getLastName().toLowerCase().contains(finalSearchTerm)))
                    .collect(Collectors.toList());
        }

        membersGrid.setItems(members);
    }

    private void openSpecialLimitDialog(UserDto user) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 title = new H3("Limites d'emprunt pour " + user.getFirstName() + " " + user.getLastName());

        // Get current limits (special or global)
        User userEntity = new User();
        userEntity.setId(user.getId());
        Optional<SpecialLimit> currentSpecialLimit = specialLimitService
                .findFirstByUserOrderByCreatedAtDesc(userEntity);
        boolean hasSpecialLimits = currentSpecialLimit.isPresent()
                && StatusUtils.SpecialLimit.ACTIVE.equals(currentSpecialLimit.get().getStatus());

        // Toggle for special limits
        Checkbox useSpecialLimitsCheckbox = new Checkbox("Utiliser des limites spéciales");
        useSpecialLimitsCheckbox.setValue(hasSpecialLimits);

        // Form for special limits
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        IntegerField maxLoansField = new IntegerField("Nombre max. d'emprunts");
        maxLoansField.setMin(1);
        maxLoansField.setMax(50);

        IntegerField maxReservationsField = new IntegerField("Nombre max. de réservations");
        maxReservationsField.setMin(1);
        maxReservationsField.setMax(50);

        // Set current values
        if (hasSpecialLimits) {
            maxLoansField.setValue(currentSpecialLimit.get().getMaxLoans());
            maxReservationsField.setValue(currentSpecialLimit.get().getMaxReservations());
        } else {
            boolean isChild = user.getIsChild() != null && user.getIsChild();
            maxLoansField.setValue(isChild ? globalSettings.getMaxLoansChild() : globalSettings.getMaxLoansAdult());
            maxReservationsField.setValue(
                    isChild ? globalSettings.getMaxReservationsChild() : globalSettings.getMaxReservationsAdult());
        }

        // Enable/disable fields based on checkbox
        maxLoansField.setEnabled(hasSpecialLimits);
        maxReservationsField.setEnabled(hasSpecialLimits);

        useSpecialLimitsCheckbox.addValueChangeListener(event -> {
            boolean useSpecial = event.getValue();
            maxLoansField.setEnabled(useSpecial);
            maxReservationsField.setEnabled(useSpecial);

            if (!useSpecial) {
                // Reset to global values when disabling special limits
                boolean isChild = user.getIsChild() != null && user.getIsChild();
                maxLoansField.setValue(isChild ? globalSettings.getMaxLoansChild() : globalSettings.getMaxLoansAdult());
                maxReservationsField.setValue(
                        isChild ? globalSettings.getMaxReservationsChild() : globalSettings.getMaxReservationsAdult());
            }
        });

        formLayout.add(maxLoansField, maxReservationsField);

        // Buttons
        Button saveButton = new Button("Enregistrer");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            if (useSpecialLimitsCheckbox.getValue()) {
                // Create or update special limits
                if (maxLoansField.getValue() == null || maxReservationsField.getValue() == null) {
                    showNotification("Veuillez remplir tous les champs", NotificationVariant.LUMO_ERROR);
                    return;
                }

                saveSpecialLimits(user, maxLoansField.getValue(), maxReservationsField.getValue());
            } else {
                // Remove special limits (set to inactive)
                removeSpecialLimits(user);
            }

            dialog.close();
            loadMembers(searchField.getValue());
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        dialogLayout.add(title, useSpecialLimitsCheckbox, formLayout, buttonLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void saveSpecialLimits(UserDto user, int maxLoans, int maxReservations) {
        try {
            SpecialLimitDto specialLimit = new SpecialLimitDto();

            // Set up user
            User userEntity = new User();
            userEntity.setId(user.getId());
            userEntity.setFirstName(user.getFirstName());
            userEntity.setLastName(user.getLastName());
            userEntity.setUsername(user.getUsername());

            specialLimit.setUser(user);
            specialLimit.setMaxLoans(maxLoans);
            specialLimit.setMaxReservations(maxReservations);
            specialLimit.setStatus(StatusUtils.SpecialLimit.ACTIVE);
            specialLimit.setCreatedAt(Instant.now());
            specialLimit.setUpdatedAt(Instant.now());

            specialLimitService.save(specialLimit);
            showNotification("Limites spéciales enregistrées avec succès", NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            showNotification("Erreur lors de l'enregistrement des limites spéciales: " + e.getMessage(),
                    NotificationVariant.LUMO_ERROR);
        }
    }

    private void removeSpecialLimits(UserDto user) {
        try {
            User userEntity = new User();
            userEntity.setId(user.getId());

            Optional<SpecialLimit> currentSpecialLimit = specialLimitService
                    .findFirstByUserOrderByCreatedAtDesc(userEntity);

            if (currentSpecialLimit.isPresent()) {
                // Set the special limit to inactive
                SpecialLimitDto specialLimit = new SpecialLimitDto();
                specialLimit.setId(currentSpecialLimit.get().getId());
                specialLimit.setUser(user);
                specialLimit.setMaxLoans(currentSpecialLimit.get().getMaxLoans());
                specialLimit.setMaxReservations(currentSpecialLimit.get().getMaxReservations());
                specialLimit.setStatus(StatusUtils.SpecialLimit.INACTIVE);
                specialLimit.setCreatedAt(currentSpecialLimit.get().getCreatedAt());
                specialLimit.setUpdatedAt(Instant.now());

                specialLimitService.save(specialLimit);
            }

            showNotification("Limites spéciales désactivées avec succès", NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            showNotification("Erreur lors de la désactivation des limites spéciales: " + e.getMessage(),
                    NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(variant);
    }
}