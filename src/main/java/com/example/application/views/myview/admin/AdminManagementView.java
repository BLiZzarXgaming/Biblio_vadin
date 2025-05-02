package com.example.application.views.myview.admin;

import com.example.application.entity.DTO.RoleDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.Role;
import com.example.application.repository.RoleRepository;
import com.example.application.security.Roles;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.utils.StatusUtils;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@PageTitle("Gestion des administrateurs")
@Route(value = "admin/manage-admins", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMINISTRATEUR")
public class AdminManagementView extends VerticalLayout {

    private final UserServiceV2 userService;
    private final RoleRepository roleRepository;

    // UI components
    private TextField searchField;
    private Grid<UserDto> adminsGrid;
    private Button addAdminButton;
    private boolean isSuperAdmin = false;

    public AdminManagementView(UserServiceV2 userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;

        // Check if current user is SuperAdmin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        isSuperAdmin = "SuperAdmin".equals(currentUsername);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Gestion des administrateurs");
        add(title);

        createSearchSection();
        createAdminsGrid();
        createAddAdminButton();

        // Initial load of administrators
        updateAdminsList("");
    }

    private void createSearchSection() {
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setWidthFull();

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom d'utilisateur, prénom ou nom");
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(500);
        searchField.addValueChangeListener(e -> updateAdminsList(e.getValue()));

        Button searchButton = new Button("Rechercher");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> updateAdminsList(searchField.getValue()));

        searchLayout.add(searchField, searchButton);
        searchLayout.setFlexGrow(1, searchField);

        add(searchLayout);
    }

    private void createAdminsGrid() {
        adminsGrid = new Grid<>();
        adminsGrid.addColumn(UserDto::getUsername).setHeader("Nom d'utilisateur").setSortable(true);
        adminsGrid.addColumn(UserDto::getFirstName).setHeader("Prénom").setSortable(true);
        adminsGrid.addColumn(UserDto::getLastName).setHeader("Nom").setSortable(true);
        adminsGrid.addColumn(UserDto::getEmail).setHeader("Email").setSortable(true);

        // Status column with styling
        adminsGrid.addColumn(new ComponentRenderer<>(user -> {
            Span statusBadge = new Span(user.getStatus());
            statusBadge.getElement().getThemeList().add("badge");

            if (StatusUtils.UserStatus.ACTIVE.equals(user.getStatus())) {
                statusBadge.getElement().getThemeList().add("success");
                statusBadge.setText("Actif");
            } else {
                statusBadge.getElement().getThemeList().add("error");
                statusBadge.setText("Inactif");
            }

            return statusBadge;
        })).setHeader("Statut").setSortable(true);

        // Action column - only visible for SuperAdmin
        if (isSuperAdmin) {
            adminsGrid.addComponentColumn(user -> {
                Button editButton = new Button("Éditer");
                editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                editButton.addClickListener(e -> openAdminDetails(user));

                // Disable editing for SuperAdmin (prevent self-deactivation)
                if ("SuperAdmin".equals(user.getUsername())) {
                    editButton.setEnabled(false);
                    editButton.getElement().setAttribute("title", "Impossible de modifier le SuperAdmin");
                }

                return editButton;
            }).setHeader("Actions").setWidth("150px").setFlexGrow(0);
        }

        adminsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        adminsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        adminsGrid.setHeight("60vh");

        add(adminsGrid);
    }

    private void createAddAdminButton() {
        addAdminButton = new Button("Ajouter un administrateur");
        addAdminButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addAdminButton.getStyle().set("margin-top", "20px");
        addAdminButton.addClickListener(e -> openAddAdminForm());

        add(addAdminButton);
    }

    private void updateAdminsList(String searchTerm) {
        List<UserDto> admins;

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Get all administrators
            admins = userService.findAll().stream()
                    .filter(user -> user.getRole() != null && StatusUtils.RoleName.ADMIN.equals(user.getRole().getName()))
                    .collect(Collectors.toList());
        } else {
            // Get filtered administrators
            searchTerm = searchTerm.toLowerCase();
            String finalSearchTerm = searchTerm;
            admins = userService.findAll().stream()
                    .filter(user -> user.getRole() != null && StatusUtils.RoleName.ADMIN.equals(user.getRole().getName()) &&
                            (user.getUsername().toLowerCase().contains(finalSearchTerm) ||
                                    (user.getFirstName() != null
                                            && user.getFirstName().toLowerCase().contains(finalSearchTerm))
                                    ||
                                    (user.getLastName() != null
                                            && user.getLastName().toLowerCase().contains(finalSearchTerm))))
                    .collect(Collectors.toList());
        }

        adminsGrid.setItems(admins);
    }

    private void openAdminDetails(UserDto admin) {
        // Only SuperAdmin can edit other admins
        if (!isSuperAdmin) {
            Notification.show("Vous n'avez pas les droits pour modifier les administrateurs.",
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("auto");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 title = new H3("Modifier un administrateur");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Username field (read-only)
        TextField usernameField = new TextField("Nom d'utilisateur");
        usernameField.setValue(admin.getUsername());
        usernameField.setReadOnly(true);

        // First name field
        TextField firstNameField = new TextField("Prénom");
        firstNameField.setValue(admin.getFirstName());
        firstNameField.setRequired(true);

        // Last name field
        TextField lastNameField = new TextField("Nom");
        lastNameField.setValue(admin.getLastName());
        lastNameField.setRequired(true);

        // Email field
        EmailField emailField = new EmailField("Email");
        emailField.setValue(admin.getEmail() != null ? admin.getEmail() : "");

        // Phone fields
        TextField phoneField = new TextField("Téléphone");
        phoneField.setValue(admin.getPhoneNumber() != null ? admin.getPhoneNumber() : "");

        TextField cellField = new TextField("Mobile");
        cellField.setValue(admin.getCellNumber() != null ? admin.getCellNumber() : "");

        // Password field
        PasswordField passwordField = new PasswordField("Nouveau mot de passe");
        passwordField.setPlaceholder("Laisser vide pour ne pas modifier");

        // Status toggle
        Checkbox activeCheckbox = new Checkbox("Compte actif");
        activeCheckbox.setValue(StatusUtils.UserStatus.ACTIVE.equals(admin.getStatus()));

        // Date of birth
        DatePicker dateOfBirthPicker = new DatePicker("Date de naissance");
        if (admin.getDateOfBirth() != null) {
            dateOfBirthPicker.setValue(
                    LocalDate.ofInstant(admin.getDateOfBirth(), ZoneId.systemDefault()));
        }

        // Add fields to form
        formLayout.add(usernameField, emailField, firstNameField, lastNameField,
                phoneField, cellField, passwordField, dateOfBirthPicker, activeCheckbox);

        // Buttons
        Button saveButton = new Button("Enregistrer", e -> {
            if (firstNameField.isEmpty() || lastNameField.isEmpty()) {
                Notification.show("Le prénom et le nom sont obligatoires",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Update admin information
            admin.setFirstName(firstNameField.getValue());
            admin.setLastName(lastNameField.getValue());
            admin.setEmail(emailField.getValue());
            admin.setPhoneNumber(phoneField.getValue());
            admin.setCellNumber(cellField.getValue());
            admin.setStatus(activeCheckbox.getValue() ? StatusUtils.UserStatus.ACTIVE : StatusUtils.UserStatus.SUSPENDED);

            // Update date of birth if changed
            if (dateOfBirthPicker.getValue() != null) {
                Instant dateOfBirth = dateOfBirthPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant();
                admin.setDateOfBirth(dateOfBirth);
            }

            // Update password if provided
            if (!passwordField.isEmpty()) {
                admin.setPassword(passwordField.getValue()); // In real app, this should be hashed
            }

            try {
                userService.save(admin);
                Notification.show("Administrateur mis à jour avec succès",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                updateAdminsList(searchField.getValue()); // Refresh grid
            } catch (Exception ex) {
                Notification.show("Erreur lors de la mise à jour: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        dialogLayout.add(title, formLayout, buttonLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void openAddAdminForm() {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("auto");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 title = new H3("Ajouter un administrateur");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // First name field
        TextField firstNameField = new TextField("Prénom");
        firstNameField.setRequired(true);

        // Last name field
        TextField lastNameField = new TextField("Nom");
        lastNameField.setRequired(true);

        // Email field
        EmailField emailField = new EmailField("Email");

        // Phone fields
        TextField phoneField = new TextField("Téléphone");
        TextField cellField = new TextField("Mobile");

        // Password field
        PasswordField passwordField = new PasswordField("Mot de passe");
        passwordField.setRequired(true);

        // Username field (optional - will be auto-generated if left empty)
        TextField usernameField = new TextField("Nom d'utilisateur (optionnel)");
        usernameField.setHelperText("Si laissé vide, un nom d'utilisateur sera généré automatiquement");

        // Date of birth
        DatePicker dateOfBirthPicker = new DatePicker("Date de naissance");
        dateOfBirthPicker.setRequired(true);
        dateOfBirthPicker.setMax(LocalDate.now());

        // Add fields to form
        formLayout.add(firstNameField, lastNameField, emailField,
                phoneField, cellField, usernameField, passwordField, dateOfBirthPicker);

        // Buttons
        Button saveButton = new Button("Enregistrer", e -> {
            // Validate required fields
            if (firstNameField.isEmpty() || lastNameField.isEmpty() ||
                    passwordField.isEmpty() || dateOfBirthPicker.isEmpty()) {
                Notification.show("Veuillez remplir tous les champs obligatoires",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Create new admin
            UserDto newAdmin = new UserDto();
            newAdmin.setFirstName(firstNameField.getValue());
            newAdmin.setLastName(lastNameField.getValue());
            newAdmin.setEmail(emailField.getValue());
            newAdmin.setPhoneNumber(phoneField.getValue());
            newAdmin.setCellNumber(cellField.getValue());
            newAdmin.setPassword(passwordField.getValue()); // In real app, this should be hashed
            newAdmin.setStatus(StatusUtils.UserStatus.ACTIVE);
            newAdmin.setIsChild(false);

            // Convert LocalDate to Instant for dateOfBirth
            Instant dateOfBirth = dateOfBirthPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant();
            newAdmin.setDateOfBirth(dateOfBirth);

            // Generate username if not provided
            String username = usernameField.getValue();
            if (username == null || username.trim().isEmpty()) {
                username = generateUniqueUsername();
            } else {
                // Check if username already exists
                Optional<UserDto> existingUser = userService.findByUsername(username);
                if (existingUser.isPresent()) {
                    Notification.show("Ce nom d'utilisateur existe déjà, veuillez en choisir un autre",
                            3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
            }
            newAdmin.setUsername(username);

            // Set role to Administrateur
            Role adminRole = roleRepository.findAll().stream()
                    .filter(role -> StatusUtils.RoleName.ADMIN.equals(role.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Role 'Administrateur' not found"));

            RoleDto roleDto = new RoleDto();
            roleDto.setId(adminRole.getId());
            roleDto.setName(adminRole.getName());
            newAdmin.setRole(roleDto);

            try {
                // Save the new admin
                userService.save(newAdmin);
                Notification.show("Administrateur créé avec succès. Nom d'utilisateur: " + username,
                        5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                updateAdminsList(""); // Refresh grid
            } catch (Exception ex) {
                Notification.show("Erreur lors de la création: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        dialogLayout.add(title, formLayout, buttonLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private String generateUniqueUsername() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int dayOfMonth = now.getDayOfMonth();
        String baseUsername;
        boolean isUnique = false;
        String username = "";

        while (!isUnique) {
            // Generate 6 random digits
            int randomPart = new Random().nextInt(900000) + 100000; // 6 digits between 100000 and 999999

            // Combine parts: ADMIN-YYYY-DD-RANDOM
            baseUsername = String.format("ADMIN-%d-%02d-%d", year, dayOfMonth, randomPart);

            // Check if username already exists
            Optional<UserDto> existingUser = userService.findByUsername(baseUsername);
            if (existingUser.isEmpty()) {
                username = baseUsername;
                isUnique = true;
            }
        }

        return username;
    }
}