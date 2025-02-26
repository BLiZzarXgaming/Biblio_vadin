package com.example.application.views.myview.benevole;

import com.example.application.entity.DTO.RoleDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.DTO.UserRelationshipDto;
import com.example.application.entity.DTO.UserRelationshipIdDto;
import com.example.application.entity.Role;
import com.example.application.entity.User;
import com.example.application.repository.RoleRepository;
import com.example.application.repository.UserRepositoryV2;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.views.MainLayout;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@PageTitle("Gestion des membres")
@Route(value = "volunteer/members", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleListeMembresView extends VerticalLayout {

    private final UserServiceV2 userService;
    private final RoleRepository roleRepository;
    private final UserRepositoryV2 userRepository;

    private final PasswordEncoder passwordEncoder;

    // UI components
    private TextField searchField;
    private Grid<UserDto> membersGrid;
    private Button addMemberButton;

    public BenevoleListeMembresView(UserServiceV2 userService, RoleRepository roleRepository,
            UserRepositoryV2 userRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Gestion des membres");
        add(title);

        createSearchSection();
        createMembersGrid();
        createAddMemberButton();

        // Initial load of members
        updateMembersList("");
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
        searchField.addValueChangeListener(e -> updateMembersList(e.getValue()));

        Button searchButton = new Button("Rechercher");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> updateMembersList(searchField.getValue()));

        // Add search on Enter
        searchField.addKeyPressListener(e -> {
            if (e.getKey().equals(Key.ENTER)) {
                updateMembersList(searchField.getValue());
            }
        });

        searchLayout.add(searchField, searchButton);
        searchLayout.setFlexGrow(1, searchField);

        add(searchLayout);
    }

    private void createMembersGrid() {
        membersGrid = new Grid<>();
        membersGrid.addColumn(UserDto::getUsername).setHeader("Nom d'utilisateur").setSortable(true);
        membersGrid.addColumn(UserDto::getFirstName).setHeader("Prénom").setSortable(true);
        membersGrid.addColumn(UserDto::getLastName).setHeader("Nom").setSortable(true);
        membersGrid.addColumn(UserDto::getEmail).setHeader("Email").setSortable(true);

        // Add status column with styling
        membersGrid.addColumn(new ComponentRenderer<>(user -> {
            Span statusBadge = new Span(user.getStatus());
            statusBadge.getElement().getThemeList().add("badge");

            if ("active".equals(user.getStatus())) {
                statusBadge.getElement().getThemeList().add("success");
                statusBadge.setText("Actif");
            } else {
                statusBadge.getElement().getThemeList().add("error");
                statusBadge.setText("Inactif");
            }

            return statusBadge;
        })).setHeader("Statut").setSortable(true);

        // Add child indicator
        membersGrid.addColumn(new ComponentRenderer<>(user -> {
            if (user.getIsChild() != null && user.getIsChild()) {
                Span childBadge = new Span("Enfant");
                childBadge.getElement().getThemeList().add("badge");
                childBadge.getElement().getThemeList().add("contrast");
                return childBadge;
            } else {
                return new Span("Adulte");
            }
        })).setHeader("Type").setWidth("100px");

        // Add action column
        membersGrid.addComponentColumn(user -> {
            Button editButton = new Button("Éditer");
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editButton.addClickListener(e -> openMemberDetails(user));
            return editButton;
        }).setHeader("Actions").setWidth("150px").setFlexGrow(0);

        membersGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        membersGrid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                openMemberDetails(event.getItem());
            }
        });

        membersGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        membersGrid.setHeight("70vh");

        add(membersGrid);
    }

    private void createAddMemberButton() {
        addMemberButton = new Button("Ajouter un membre");
        addMemberButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addMemberButton.getStyle().set("margin-top", "20px");
        addMemberButton.addClickListener(e -> openAddMemberForm());

        add(addMemberButton);
    }

    private void updateMembersList(String searchTerm) {
        List<UserDto> members;

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Get all members
            members = userService.findAll().stream()
                    .filter(user -> user.getRole() != null && "Membre".equals(user.getRole().getName()))
                    .collect(Collectors.toList());
        } else {
            // Get filtered members
            searchTerm = searchTerm.toLowerCase();
            String finalSearchTerm = searchTerm;
            members = userService.findAll().stream()
                    .filter(user -> user.getRole() != null && "Membre".equals(user.getRole().getName()) &&
                            (user.getUsername().toLowerCase().contains(finalSearchTerm) ||
                                    (user.getFirstName() != null
                                            && user.getFirstName().toLowerCase().contains(finalSearchTerm))
                                    ||
                                    (user.getLastName() != null
                                            && user.getLastName().toLowerCase().contains(finalSearchTerm))))
                    .collect(Collectors.toList());
        }

        membersGrid.setItems(members);
    }

    private void openMemberDetails(UserDto member) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("auto");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 title = new H3("Détails du membre");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Username field (read-only)
        TextField usernameField = new TextField("Nom d'utilisateur");
        usernameField.setValue(member.getUsername());
        usernameField.setReadOnly(true);

        // First name field
        TextField firstNameField = new TextField("Prénom");
        firstNameField.setValue(member.getFirstName());
        firstNameField.setRequired(true);

        // Last name field
        TextField lastNameField = new TextField("Nom");
        lastNameField.setValue(member.getLastName());
        lastNameField.setRequired(true);

        // Email field
        TextField emailField = new TextField("Email");
        emailField.setValue(member.getEmail() != null ? member.getEmail() : "");

        // Phone fields
        TextField phoneField = new TextField("Téléphone");
        phoneField.setValue(member.getPhoneNumber() != null ? member.getPhoneNumber() : "");

        TextField cellField = new TextField("Mobile");
        cellField.setValue(member.getCellNumber() != null ? member.getCellNumber() : "");

        // Password field
        PasswordField passwordField = new PasswordField("Nouveau mot de passe");
        passwordField.setPlaceholder("Laisser vide pour ne pas modifier");

        // Status toggle
        Checkbox activeCheckbox = new Checkbox("Compte actif");
        activeCheckbox.setValue("active".equals(member.getStatus()));

        // Child status
        Checkbox childCheckbox = new Checkbox("Compte enfant");
        childCheckbox.setValue(member.getIsChild());

        // Date of birth
        DatePicker dateOfBirthPicker = new DatePicker("Date de naissance");
        if (member.getDateOfBirth() != null) {
            dateOfBirthPicker.setValue(
                    LocalDate.ofInstant(member.getDateOfBirth(), ZoneId.systemDefault()));
        }

        // Parent-child relationship section
        Div parentSection = new Div();
        parentSection.setWidthFull();
        parentSection.getStyle().set("padding-top", "20px");
        parentSection.getStyle().set("padding-bottom", "10px");

        // Get member user and its ID
        Long memberId = member.getId();

        // Parent selectors
        ComboBox<UserDto> parent1ComboBox = new ComboBox<>("Premier parent");
        ComboBox<UserDto> parent2ComboBox = new ComboBox<>("Deuxième parent (optionnel)");

        // Get all adult members for parent selection
        List<UserDto> adultMembers = userService.findAll().stream()
                .filter(user -> user.getRole() != null &&
                        "Membre".equals(user.getRole().getName()) &&
                        (user.getIsChild() == null || !user.getIsChild()) &&
                        !user.getId().equals(memberId)) // Can't be parent of self
                .collect(Collectors.toList());

        parent1ComboBox.setItems(adultMembers);
        parent1ComboBox.setItemLabelGenerator(
                user -> user.getFirstName() + " " + user.getLastName() + " (" + user.getUsername() + ")");

        parent2ComboBox.setItems(adultMembers);
        parent2ComboBox.setItemLabelGenerator(
                user -> user.getFirstName() + " " + user.getLastName() + " (" + user.getUsername() + ")");

        // Find existing parent relationships
        List<User> existingParents = findParents(memberId);

        // Set current values if they exist
        if (!existingParents.isEmpty()) {
            // Find the first parent in our list of adults
            for (UserDto adultUser : adultMembers) {
                if (adultUser.getId().equals(existingParents.get(0).getId())) {
                    parent1ComboBox.setValue(adultUser);
                    break;
                }
            }

            // If there's a second parent, find them too
            if (existingParents.size() > 1) {
                for (UserDto adultUser : adultMembers) {
                    if (adultUser.getId().equals(existingParents.get(1).getId())) {
                        parent2ComboBox.setValue(adultUser);
                        break;
                    }
                }
            }
        }

        parent1ComboBox.setVisible(member.getIsChild());
        parent2ComboBox.setVisible(member.getIsChild());

        // Show/hide parent selectors based on child status
        childCheckbox.addValueChangeListener(event -> {
            boolean isChild = event.getValue();
            parent1ComboBox.setVisible(isChild);
            parent2ComboBox.setVisible(isChild);

            // Clear parents if not a child
            if (!isChild) {
                parent1ComboBox.clear();
                parent2ComboBox.clear();
            }
        });

        parentSection.add(parent1ComboBox, parent2ComboBox);

        // Add fields to form
        formLayout.add(usernameField, emailField, firstNameField, lastNameField,
                phoneField, cellField, passwordField, dateOfBirthPicker,
                activeCheckbox, childCheckbox);

        // Buttons
        Button saveButton = new Button("Enregistrer", e -> {
            if (firstNameField.isEmpty() || lastNameField.isEmpty()) {
                Notification.show("Le prénom et le nom sont obligatoires",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Validate parent selections for child accounts
            if (childCheckbox.getValue() && parent1ComboBox.isEmpty()) {
                Notification.show("Un compte enfant doit avoir au moins un parent désigné",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Ensure parents are different
            if (childCheckbox.getValue() &&
                    !parent2ComboBox.isEmpty() &&
                    parent1ComboBox.getValue().getId().equals(parent2ComboBox.getValue().getId())) {
                Notification.show("Les deux parents doivent être différents",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Update member information
            member.setFirstName(firstNameField.getValue());
            member.setLastName(lastNameField.getValue());
            member.setEmail(emailField.getValue());
            member.setPhoneNumber(phoneField.getValue());
            member.setCellNumber(cellField.getValue());
            member.setStatus(activeCheckbox.getValue() ? "active" : "inactive");
            member.setIsChild(childCheckbox.getValue());

            // Update date of birth if changed
            Instant dateOfBirth = dateOfBirthPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant();
            member.setDateOfBirth(dateOfBirth);

            // Update password if provided
            if (!passwordField.isEmpty()) {
                String hashedPassword = passwordEncoder.encode(passwordField.getValue());
                member.setPassword(hashedPassword); // In real app, this should be hashed
            }

            try {
                // Save the member first
                userService.save(member);

                // Now handle parent-child relationships
                if (member.getIsChild()) {
                    updateParentChildRelationships(
                            member.getId(),
                            parent1ComboBox.getValue(),
                            parent2ComboBox.isEmpty() ? null : parent2ComboBox.getValue());
                } else {
                    // If no longer a child, remove any parent relationships
                    removeParentChildRelationships(member.getId());
                }

                Notification.show("Membre mis à jour avec succès",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                updateMembersList(searchField.getValue()); // Refresh grid
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

        dialogLayout.add(title, formLayout, parentSection, buttonLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void openAddMemberForm() {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("auto");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 title = new H3("Ajouter un membre");

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
        TextField emailField = new TextField("Email");

        // Phone fields
        TextField phoneField = new TextField("Téléphone");
        TextField cellField = new TextField("Mobile");

        // Password field
        PasswordField passwordField = new PasswordField("Mot de passe");
        passwordField.setRequired(true);

        // Child status
        Checkbox childCheckbox = new Checkbox("Compte enfant");
        childCheckbox.setValue(false);

        // Date of birth
        DatePicker dateOfBirthPicker = new DatePicker("Date de naissance");
        dateOfBirthPicker.setRequired(true);
        dateOfBirthPicker.setMax(LocalDate.now());

        // Parent-child relationship section
        Div parentSection = new Div();
        parentSection.setWidthFull();
        parentSection.getStyle().set("padding-top", "20px");
        parentSection.getStyle().set("padding-bottom", "10px");

        // Parent selectors
        ComboBox<UserDto> parent1ComboBox = new ComboBox<>("Premier parent");
        ComboBox<UserDto> parent2ComboBox = new ComboBox<>("Deuxième parent (optionnel)");

        // Get all adult members for parent selection
        List<UserDto> adultMembers = userService.findAll().stream()
                .filter(user -> user.getRole() != null &&
                        "Membre".equals(user.getRole().getName()) &&
                        (user.getIsChild() == null || !user.getIsChild()))
                .collect(Collectors.toList());

        parent1ComboBox.setItems(adultMembers);
        parent1ComboBox.setItemLabelGenerator(
                user -> user.getFirstName() + " " + user.getLastName() + " (" + user.getUsername() + ")");

        parent2ComboBox.setItems(adultMembers);
        parent2ComboBox.setItemLabelGenerator(
                user -> user.getFirstName() + " " + user.getLastName() + " (" + user.getUsername() + ")");

        parent1ComboBox.setVisible(false);
        parent2ComboBox.setVisible(false);

        // Show/hide parent selectors based on child status
        childCheckbox.addValueChangeListener(event -> {
            boolean isChild = event.getValue();
            parent1ComboBox.setVisible(isChild);
            parent2ComboBox.setVisible(isChild);

            // Clear parents if not a child
            if (!isChild) {
                parent1ComboBox.clear();
                parent2ComboBox.clear();
            }
        });

        parentSection.add(parent1ComboBox, parent2ComboBox);

        // Add fields to form
        formLayout.add(firstNameField, lastNameField, emailField,
                phoneField, cellField, passwordField, dateOfBirthPicker, childCheckbox);

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

            // Validate parent selections for child accounts
            if (childCheckbox.getValue() && parent1ComboBox.isEmpty()) {
                Notification.show("Un compte enfant doit avoir au moins un parent désigné",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Ensure parents are different
            if (childCheckbox.getValue() &&
                    !parent2ComboBox.isEmpty() &&
                    parent1ComboBox.getValue().getId().equals(parent2ComboBox.getValue().getId())) {
                Notification.show("Les deux parents doivent être différents",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Create new member
            UserDto newMember = new UserDto();
            newMember.setFirstName(firstNameField.getValue());
            newMember.setLastName(lastNameField.getValue());
            newMember.setEmail(emailField.getValue());
            newMember.setPhoneNumber(phoneField.getValue());
            newMember.setCellNumber(cellField.getValue());
            if (!passwordField.isEmpty()) {
                String hashedPassword = passwordEncoder.encode(passwordField.getValue());
                newMember.setPassword(hashedPassword);
            } // In real app, this should be hashed
            newMember.setStatus("active");
            newMember.setIsChild(childCheckbox.getValue());

            // Convert LocalDate to Instant for dateOfBirth
            Instant dateOfBirth = dateOfBirthPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant();
            newMember.setDateOfBirth(dateOfBirth);

            // Generate username
            String username = generateUniqueUsername();
            newMember.setUsername(username);

            // Set role to MEMBRE
            Role membreRole = roleRepository.findAll().stream()
                    .filter(role -> "Membre".equals(role.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Role 'Membre' not found"));

            RoleDto roleDto = new RoleDto();
            roleDto.setId(membreRole.getId());
            roleDto.setName(membreRole.getName());
            newMember.setRole(roleDto);

            try {
                // Save the new member
                UserDto savedMember = userService.save(newMember);

                // Now handle parent-child relationships if it's a child
                if (childCheckbox.getValue()) {
                    createParentChildRelationships(
                            savedMember.getId(),
                            parent1ComboBox.getValue(),
                            parent2ComboBox.isEmpty() ? null : parent2ComboBox.getValue());
                }

                Notification.show("Membre créé avec succès. Nom d'utilisateur: " + username,
                        5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                updateMembersList(""); // Refresh grid
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

        dialogLayout.add(title, formLayout, parentSection, buttonLayout);
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

            // Combine parts: YYYY + DD + RANDOM
            baseUsername = String.format("%d%02d%d", year, dayOfMonth, randomPart);

            // Check if username already exists
            Optional<UserDto> existingUser = userService.findByUsername(baseUsername);
            if (existingUser.isEmpty()) {
                username = baseUsername;
                isUnique = true;
            }
        }

        return username;
    }

    /**
     * Find all parents of a child user
     */
    private List<User> findParents(Long childId) {
        List<User> parents = new ArrayList<>();

        // This would typically involve querying your UserRelationship repository
        // Since we don't have direct access to that repository, we'll use a native
        // query
        // through the UserRepository

        try {
            // Get all users that are parents of this child
            List<User> parentUsers = userRepository.findAll().stream()
                    .filter(user -> {
                        // Find UserRelationships where this user is a parent
                        // and the childId matches our target child
                        return user.getId() != childId; // This is a simplified placeholder
                    })
                    .collect(Collectors.toList());

            // In reality, you would run a query like:
            // SELECT u.* FROM users u
            // JOIN user_relationships ur ON u.id = ur.parent_id
            // WHERE ur.child_id = :childId

            return parentUsers;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Create parent-child relationships for a new child
     */
    @Transactional
    private void createParentChildRelationships(Long childId, UserDto parent1, UserDto parent2) {
        // Here you would save new UserRelationship entities
        // Since we don't have direct access to that repository, we'll describe the
        // process

        if (parent1 != null) {
            createParentChildRelationship(parent1.getId(), childId);
        }

        if (parent2 != null) {
            createParentChildRelationship(parent2.getId(), childId);
        }
    }

    /**
     * Create a single parent-child relationship
     */
    private void createParentChildRelationship(Long parentId, Long childId) {
        // This method would:
        // 1. Create a new UserRelationshipId with parentId and childId
        // 2. Create a new UserRelationship with that id and a type like "parent"
        // 3. Save that relationship

        // Example code (not executable as we don't have the repository):
        UserRelationshipDto relationship = new UserRelationshipDto();

        UserRelationshipIdDto id = new UserRelationshipIdDto();
        id.setParentId(parentId);
        id.setChildId(childId);
        relationship.setId(id);

        // Set relationship type
        relationship.setRelationshipType("parent");

        // You would then save this using a UserRelationshipRepository
    }

    /**
     * Update parent-child relationships for an existing child
     */
    @Transactional
    private void updateParentChildRelationships(Long childId, UserDto parent1, UserDto parent2) {
        // 1. Remove all existing parent relationships
        removeParentChildRelationships(childId);

        // 2. Create new relationships
        createParentChildRelationships(childId, parent1, parent2);
    }

    /**
     * Remove all parent-child relationships for a child
     */
    @Transactional
    private void removeParentChildRelationships(Long childId) {
        // This method would:
        // 1. Find all UserRelationship entities where childId matches
        // 2. Delete them all

        // Example code (not executable as we don't have the repository):
        // userRelationshipRepository.deleteAllByChildId(childId);
    }
}