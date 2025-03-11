package com.example.application.views.myview.admin;

import com.example.application.entity.DTO.RoleDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.Role;
import com.example.application.repository.RoleRepository;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@PageTitle("Gestion des bénévoles")
@Route(value = "admin/benevoles", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMINISTRATEUR")
public class AdminGestionBenevolesView extends VerticalLayout {

    private final UserServiceV2 userService;
    private final RoleRepository roleRepository;

    // UI components
    private TextField searchField;
    private Grid<UserDto> benevolesGrid;
    private Button addBenevoleButton;
    private HorizontalLayout paginationLayout;

    // Pagination
    private int pageSize = 10;
    private int currentPage = 0;
    private int totalPages = 0;
    private List<UserDto> allBenevoles;
    private ListDataProvider<UserDto> dataProvider;
    private Span pageInfoLabel;

    public AdminGestionBenevolesView(UserServiceV2 userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Gestion des bénévoles");
        add(title);

        createSearchSection();
        createBenevolesGrid();
        createPaginationControls();
        createAddBenevoleButton();

        // Initial load of benevoles
        updateBenevolesList("");
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
        searchField.addValueChangeListener(e -> {
            currentPage = 0; // Reset to first page on new search
            updateBenevolesList(e.getValue());
        });

        Button searchButton = new Button("Rechercher");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> {
            currentPage = 0; // Reset to first page on search button click
            updateBenevolesList(searchField.getValue());
        });

        searchLayout.add(searchField, searchButton);
        searchLayout.setFlexGrow(1, searchField);

        add(searchLayout);
    }

    private void createBenevolesGrid() {
        benevolesGrid = new Grid<>();
        benevolesGrid.addColumn(UserDto::getUsername).setHeader("Nom d'utilisateur").setSortable(true);
        benevolesGrid.addColumn(UserDto::getFirstName).setHeader("Prénom").setSortable(true);
        benevolesGrid.addColumn(UserDto::getLastName).setHeader("Nom").setSortable(true);
        benevolesGrid.addColumn(UserDto::getEmail).setHeader("Email").setSortable(true);
        benevolesGrid.addColumn(UserDto::getPhoneNumber).setHeader("Téléphone");

        // Add status column with styling
        benevolesGrid.addColumn(new ComponentRenderer<>(user -> {
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

        // Add action column
        benevolesGrid.addComponentColumn(user -> {
            Button editButton = new Button("Éditer");
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editButton.addClickListener(e -> openBenevoleDetails(user));

            Button toggleStatusButton = new Button();
            if (StatusUtils.UserStatus.ACTIVE.equals(user.getStatus())) {
                toggleStatusButton.setText("Désactiver");
                toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            } else {
                toggleStatusButton.setText("Activer");
                toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            }
            toggleStatusButton.addClickListener(e -> toggleBenevoleStatus(user));

            HorizontalLayout actions = new HorizontalLayout(editButton, toggleStatusButton);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Actions").setWidth("250px").setFlexGrow(0);

        benevolesGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        benevolesGrid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                openBenevoleDetails(event.getItem());
            }
        });

        benevolesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        benevolesGrid.setHeight("500px");

        add(benevolesGrid);
    }

    private void createPaginationControls() {
        paginationLayout = new HorizontalLayout();
        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        Button firstPageButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT));
        firstPageButton.addClickListener(e -> {
            currentPage = 0;
            refreshGrid();
        });

        Button prevPageButton = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
        prevPageButton.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshGrid();
            }
        });

        pageInfoLabel = new Span("Page 1 sur 1");

        Button nextPageButton = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
        nextPageButton.addClickListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                refreshGrid();
            }
        });

        Button lastPageButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));
        lastPageButton.addClickListener(e -> {
            currentPage = Math.max(0, totalPages - 1);
            refreshGrid();
        });

        paginationLayout.add(firstPageButton, prevPageButton, pageInfoLabel, nextPageButton, lastPageButton);

        add(paginationLayout);
    }

    private void createAddBenevoleButton() {
        addBenevoleButton = new Button("Ajouter un bénévole");
        addBenevoleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBenevoleButton.getStyle().set("margin-top", "20px");
        addBenevoleButton.addClickListener(e -> openAddBenevoleForm());

        add(addBenevoleButton);
    }

    private void updateBenevolesList(String searchTerm) {
        // Get all users with role "BÉNÉVOLE"
        allBenevoles = userService.findAll().stream()
                .filter(user -> user.getRole() != null && StatusUtils.RoleName.BENEVOLE.equals(user.getRole().getName()))
                .collect(Collectors.toList());

        // Filter by search term if provided
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String finalSearchTerm = searchTerm.toLowerCase();
            allBenevoles = allBenevoles.stream()
                    .filter(user -> (user.getUsername() != null
                            && user.getUsername().toLowerCase().contains(finalSearchTerm)) ||
                            (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(finalSearchTerm))
                            ||
                            (user.getLastName() != null && user.getLastName().toLowerCase().contains(finalSearchTerm))
                            ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(finalSearchTerm)))
                    .collect(Collectors.toList());
        }

        // Calculate pagination
        totalPages = (int) Math.ceil((double) allBenevoles.size() / pageSize);
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        dataProvider = DataProvider.ofCollection(allBenevoles);
        benevolesGrid.setDataProvider(dataProvider);

        refreshGrid();
    }

    private void refreshGrid() {
        // Apply pagination
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, allBenevoles.size());

        if (dataProvider != null) {
            dataProvider.refreshAll();

            // Extract paginated subset directly without using Query
            List<UserDto> pagedBenevoles = allBenevoles.stream()
                    .skip(start)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            benevolesGrid.setItems(pagedBenevoles);

            // Update pagination info
            updatePaginationInfo();
        }
    }

    private void updatePaginationInfo() {
        int displayPage = currentPage + 1;
        int displayTotalPages = Math.max(1, totalPages);
        pageInfoLabel.setText("Page " + displayPage + " sur " + displayTotalPages);
    }

    private void openBenevoleDetails(UserDto benevole) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("auto");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 title = new H3("Détails du bénévole");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Username field (read-only)
        TextField usernameField = new TextField("Nom d'utilisateur");
        usernameField.setValue(benevole.getUsername());
        usernameField.setReadOnly(true);

        // First name field
        TextField firstNameField = new TextField("Prénom");
        firstNameField.setValue(benevole.getFirstName());
        firstNameField.setRequired(true);

        // Last name field
        TextField lastNameField = new TextField("Nom");
        lastNameField.setValue(benevole.getLastName());
        lastNameField.setRequired(true);

        // Email field
        EmailField emailField = new EmailField("Email");
        emailField.setValue(benevole.getEmail() != null ? benevole.getEmail() : "");

        // Phone fields
        TextField phoneField = new TextField("Téléphone");
        phoneField.setValue(benevole.getPhoneNumber() != null ? benevole.getPhoneNumber() : "");

        TextField cellField = new TextField("Mobile");
        cellField.setValue(benevole.getCellNumber() != null ? benevole.getCellNumber() : "");

        // Password field
        PasswordField passwordField = new PasswordField("Nouveau mot de passe");
        passwordField.setPlaceholder("Laisser vide pour ne pas modifier");

        // Status toggle
        Checkbox activeCheckbox = new Checkbox("Compte actif");
        activeCheckbox.setValue(StatusUtils.UserStatus.ACTIVE.equals(benevole.getStatus()));

        // Date of birth
        DatePicker dateOfBirthPicker = new DatePicker("Date de naissance");
        if (benevole.getDateOfBirth() != null) {
            dateOfBirthPicker.setValue(
                    LocalDate.ofInstant(benevole.getDateOfBirth(), ZoneId.systemDefault()));
        }

        // Add fields to form
        formLayout.add(usernameField, emailField, firstNameField, lastNameField,
                phoneField, cellField, passwordField, dateOfBirthPicker,
                activeCheckbox);

        // Buttons
        Button saveButton = new Button("Enregistrer", e -> {
            if (firstNameField.isEmpty() || lastNameField.isEmpty()) {
                Notification.show("Le prénom et le nom sont obligatoires",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Update benevole information
            benevole.setFirstName(firstNameField.getValue());
            benevole.setLastName(lastNameField.getValue());
            benevole.setEmail(emailField.getValue());
            benevole.setPhoneNumber(phoneField.getValue());
            benevole.setCellNumber(cellField.getValue());
            benevole.setStatus(activeCheckbox.getValue() ? StatusUtils.UserStatus.ACTIVE : StatusUtils.UserStatus.SUSPENDED);

            // Update date of birth if changed
            if (dateOfBirthPicker.getValue() != null) {
                Instant dateOfBirth = dateOfBirthPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant();
                benevole.setDateOfBirth(dateOfBirth);
            }

            // Update password if provided
            if (!passwordField.isEmpty()) {
                benevole.setPassword(passwordField.getValue()); // In real app, this should be hashed
            }

            try {
                userService.save(benevole);
                Notification.show("Bénévole mis à jour avec succès",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid(); // Refresh grid to show updated data
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

    private void openAddBenevoleForm() {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("auto");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 title = new H3("Ajouter un bénévole");

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

        // Date of birth
        DatePicker dateOfBirthPicker = new DatePicker("Date de naissance");
        dateOfBirthPicker.setRequired(true);
        dateOfBirthPicker.setMax(LocalDate.now());

        // Add fields to form
        formLayout.add(firstNameField, lastNameField, emailField,
                phoneField, cellField, passwordField, dateOfBirthPicker);

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

            // Create new benevole
            UserDto newBenevole = new UserDto();
            newBenevole.setFirstName(firstNameField.getValue());
            newBenevole.setLastName(lastNameField.getValue());
            newBenevole.setEmail(emailField.getValue());
            newBenevole.setPhoneNumber(phoneField.getValue());
            newBenevole.setCellNumber(cellField.getValue());
            newBenevole.setPassword(passwordField.getValue()); // In real app, this should be hashed
            newBenevole.setStatus(StatusUtils.UserStatus.ACTIVE);
            newBenevole.setIsChild(false);

            // Convert LocalDate to Instant for dateOfBirth
            Instant dateOfBirth = dateOfBirthPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant();
            newBenevole.setDateOfBirth(dateOfBirth);

            // Generate username
            String username = generateUniqueUsername();
            newBenevole.setUsername(username);

            // Set role to BÉNÉVOLE
            Role benevoleRole = roleRepository.findAll().stream()
                    .filter(role -> StatusUtils.RoleName.BENEVOLE.equals(role.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Role 'Bénévole' not found"));

            RoleDto roleDto = new RoleDto();
            roleDto.setId(benevoleRole.getId());
            roleDto.setName(benevoleRole.getName());
            newBenevole.setRole(roleDto);

            try {
                // Save the new benevole
                userService.save(newBenevole);
                Notification.show("Bénévole créé avec succès. Nom d'utilisateur: " + username,
                        5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                updateBenevolesList(""); // Refresh grid with all benevoles
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

    private void toggleBenevoleStatus(UserDto benevole) {
        String newStatus = StatusUtils.UserStatus.ACTIVE.equals(benevole.getStatus()) ? StatusUtils.UserStatus.SUSPENDED : StatusUtils.UserStatus.ACTIVE;
        String actionText = StatusUtils.UserStatus.ACTIVE.equals(newStatus) ? "activer" : "désactiver";

        Dialog confirmDialog = new Dialog();
        confirmDialog.setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        H3 title = new H3("Confirmation");
        Span message = new Span("Êtes-vous sûr de vouloir " + actionText + " le compte de " +
                benevole.getFirstName() + " " + benevole.getLastName() + " ?");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        Button confirmButton = new Button("Confirmer", e -> {
            benevole.setStatus(newStatus);

            try {
                userService.save(benevole);
                Notification.show("Statut du bénévole mis à jour avec succès",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                refreshGrid(); // Refresh grid to show updated status
            } catch (Exception ex) {
                Notification.show("Erreur lors de la mise à jour: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> confirmDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttons.add(confirmButton, cancelButton);

        layout.add(title, message, buttons);
        confirmDialog.add(layout);

        confirmDialog.open();
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

            // Combine parts: YYYY + DD + RANDOM
            baseUsername = String.format("BEN_%d%02d%d", year, dayOfMonth, randomPart);

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