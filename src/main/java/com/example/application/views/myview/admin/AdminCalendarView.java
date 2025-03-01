package com.example.application.views.myview.admin;

import com.example.application.entity.Availability;
import com.example.application.entity.DTO.AvailabilityDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.User;
import com.example.application.service.implementation.AvailabilityServiceV2;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.views.MainLayout;
import com.example.application.config.SecurityService;
import com.example.application.identification_user.MyUserPrincipal;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@PageTitle("Gestion du Calendrier")
@Route(value = "admin/calendar", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMINISTRATEUR")
public class AdminCalendarView extends VerticalLayout {

    private final AvailabilityServiceV2 availabilityService;
    private final UserServiceV2 userService;
    private final SecurityService securityService;

    // UI Components
    private FullCalendar calendar;
    private Grid<AvailabilityDto> eventsGrid;
    private Grid<AvailabilityDto> openHoursGrid;
    private VerticalLayout calendarLayout;
    private VerticalLayout gridLayout;
    private VerticalLayout mainContent;
    private UserDto currentUser;

    // State tracking
    private String currentView = "calendar"; // "calendar" or "grid"
    private Map<String, Component> viewContainers = new HashMap<>();
    private LocalDate currentMonth = LocalDate.now();

    // Constants
    private static final String TYPE_EVENT = "event";
    private static final String TYPE_OPENING_HOURS = "heureOuverture";
    private static final String STATUS_CONFIRMED = "Confirmed";
    private static final String STATUS_DRAFT = "Draft";
    private static final String STATUS_CANCELLED = "Cancelled";

    public AdminCalendarView(AvailabilityServiceV2 availabilityService, UserServiceV2 userService,
            SecurityService securityService) {
        this.availabilityService = availabilityService;
        this.userService = userService;
        this.securityService = securityService;

        // Get current user
        try {
            // Récupérer l'utilisateur connecté via SecurityService
            MyUserPrincipal userPrincipal = (MyUserPrincipal) securityService.getAuthenticatedUser();
            if (userPrincipal != null) {
                String username = userPrincipal.getUsername();
                Optional<UserDto> user = userService.findByUsername(username);
                if (user.isPresent()) {
                    currentUser = user.get();
                    System.out.println("Utilisateur connecté récupéré: " + currentUser.getUsername());
                } else {
                    // Fallback - get first admin user
                    System.out.println(
                            "Utilisateur connecté trouvé dans Spring Security mais pas dans la base de données: "
                                    + username);
                    findAdminUser();
                }
            } else {
                // No authenticated user found
                System.out.println(
                        "Aucun utilisateur authentifié trouvé via SecurityService. Utilisation d'un utilisateur administrateur par défaut.");
                findAdminUser();
            }
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            // Try to get the first admin user as fallback
            findAdminUser();
        }

        // Si aucun utilisateur n'a été trouvé, afficher un message d'erreur
        if (currentUser == null) {
            System.err.println(
                    "ATTENTION: Aucun utilisateur administrateur trouvé. Les fonctionnalités du calendrier peuvent ne pas fonctionner correctement.");
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Main title
        H2 title = new H2("Gestion du Calendrier");

        // Create tabs for switching between views
        Tab calendarTab = new Tab("Vue Calendrier");
        Tab eventsTab = new Tab("Événements");
        Tab openingHoursTab = new Tab("Heures d'ouverture");

        Tabs tabs = new Tabs(calendarTab, eventsTab, openingHoursTab);
        tabs.setWidth("100%");

        // Create buttons for adding new entries
        Button addEventButton = new Button("Nouvel Événement", e -> showEventDialog(null));
        addEventButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button addOpeningHoursButton = new Button("Nouvelles Heures d'ouverture", e -> showOpeningHoursDialog(null));
        addOpeningHoursButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttonsLayout = new HorizontalLayout(addEventButton, addOpeningHoursButton);
        buttonsLayout.setSpacing(true);

        // Initialize main content container
        mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(false);

        // Create calendar view
        calendarLayout = createCalendarLayout();
        viewContainers.put("calendar", calendarLayout);

        // Create grids for events and opening hours
        eventsGrid = createEventsGrid();
        openHoursGrid = createOpeningHoursGrid();

        VerticalLayout eventsLayout = new VerticalLayout(new H3("Liste des Événements"), eventsGrid);
        eventsLayout.setSizeFull();
        viewContainers.put("events", eventsLayout);

        VerticalLayout openHoursLayout = new VerticalLayout(new H3("Liste des Heures d'ouverture"), openHoursGrid);
        openHoursLayout.setSizeFull();
        viewContainers.put("openingHours", openHoursLayout);

        // Set initial view
        mainContent.add(calendarLayout);

        // Handle tab selection
        tabs.addSelectedChangeListener(event -> {
            mainContent.removeAll();
            if (event.getSelectedTab().equals(calendarTab)) {
                currentView = "calendar";
                mainContent.add(viewContainers.get("calendar"));
                loadCalendarData();
            } else if (event.getSelectedTab().equals(eventsTab)) {
                currentView = "events";
                mainContent.add(viewContainers.get("events"));
                loadEventsData();
            } else if (event.getSelectedTab().equals(openingHoursTab)) {
                currentView = "openingHours";
                mainContent.add(viewContainers.get("openingHours"));
                loadOpeningHoursData();
            }
        });

        // Add components to main layout
        add(title, tabs, buttonsLayout, mainContent);
        expand(mainContent);

        // Initial data load
        loadCalendarData();
    }

    private VerticalLayout createCalendarLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setHeightFull();
        layout.setPadding(false);
        layout.setSpacing(true);

        // Month navigation
        Button prevMonthButton = new Button(new Icon(VaadinIcon.ANGLE_LEFT), e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendarMonth();
        });

        Button nextMonthButton = new Button(new Icon(VaadinIcon.ANGLE_RIGHT), e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendarMonth();
        });

        Button todayButton = new Button("Aujourd'hui", e -> {
            currentMonth = LocalDate.now();
            updateCalendarMonth();
        });

        H3 monthTitle = new H3(
                currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE) + " " + currentMonth.getYear());
        monthTitle.getStyle().set("margin", "0");

        HorizontalLayout navigationLayout = new HorizontalLayout(prevMonthButton, monthTitle, nextMonthButton,
                todayButton);
        navigationLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        navigationLayout.setWidthFull();
        navigationLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Create calendar
        calendar = FullCalendarBuilder.create().build();
        calendar.setHeightByParent();
        calendar.setWidthFull();
        calendar.setLocale(Locale.FRANCE);

        // Add entry click listener
        calendar.addEntryClickedListener(event -> {
            String entryId = event.getEntry().getCustomProperty("id");
            Long availabilityId = Long.parseLong(entryId);
            availabilityService.findById(availabilityId).ifPresent(availability -> {
                if (TYPE_EVENT.equals(availability.getType())) {
                    showEventDialog(availability);
                } else if (TYPE_OPENING_HOURS.equals(availability.getType())) {
                    showOpeningHoursDialog(availability);
                }
            });
        });

        layout.add(navigationLayout, calendar);
        layout.expand(calendar);

        return layout;
    }

    private Grid<AvailabilityDto> createEventsGrid() {
        Grid<AvailabilityDto> grid = new Grid<>();
        grid.setHeight("100%");

        grid.addColumn(AvailabilityDto::getTitle).setHeader("Titre").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(av -> av.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date").setAutoWidth(true);
        grid.addColumn(av -> av.getTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .setHeader("Heure").setAutoWidth(true);
        grid.addColumn(av -> av.getDuration() + " min").setHeader("Durée").setAutoWidth(true);

        // Status column with badge
        grid.addColumn(new ComponentRenderer<>(availability -> {
            Span badge = new Span(availability.getStatus());
            badge.getElement().getThemeList().add("badge");

            if (STATUS_CONFIRMED.equals(availability.getStatus())) {
                badge.getElement().getThemeList().add("success");
                badge.setText("Confirmé");
            } else if (STATUS_DRAFT.equals(availability.getStatus())) {
                badge.getElement().getThemeList().add("contrast");
                badge.setText("Brouillon");
            } else if (STATUS_CANCELLED.equals(availability.getStatus())) {
                badge.getElement().getThemeList().add("error");
                badge.setText("Annulé");
            }

            return badge;
        })).setHeader("Statut").setAutoWidth(true);

        // Actions column
        grid.addComponentColumn(availability -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button editButton = new Button(new Icon(VaadinIcon.EDIT), e -> showEventDialog(availability));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), e -> confirmDelete(availability));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY,
                    ButtonVariant.LUMO_ERROR);

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        return grid;
    }

    private Grid<AvailabilityDto> createOpeningHoursGrid() {
        Grid<AvailabilityDto> grid = new Grid<>();
        grid.setHeight("100%");

        grid.addColumn(AvailabilityDto::getTitle).setHeader("Titre").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(av -> av.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date").setAutoWidth(true);
        grid.addColumn(av -> av.getTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .setHeader("Heure de début").setAutoWidth(true);
        grid.addColumn(av -> av.getTime().plusMinutes(av.getDuration()).format(DateTimeFormatter.ofPattern("HH:mm")))
                .setHeader("Heure de fin").setAutoWidth(true);
        grid.addColumn(av -> av.getDuration() + " min").setHeader("Durée").setAutoWidth(true);

        // Status column with badge
        grid.addColumn(new ComponentRenderer<>(availability -> {
            Span badge = new Span(availability.getStatus());
            badge.getElement().getThemeList().add("badge");

            if (STATUS_CONFIRMED.equals(availability.getStatus())) {
                badge.getElement().getThemeList().add("success");
                badge.setText("Confirmé");
            } else if (STATUS_DRAFT.equals(availability.getStatus())) {
                badge.getElement().getThemeList().add("contrast");
                badge.setText("Brouillon");
            } else if (STATUS_CANCELLED.equals(availability.getStatus())) {
                badge.getElement().getThemeList().add("error");
                badge.setText("Annulé");
            }

            return badge;
        })).setHeader("Statut").setAutoWidth(true);

        // Actions column
        grid.addComponentColumn(availability -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button editButton = new Button(new Icon(VaadinIcon.EDIT), e -> showOpeningHoursDialog(availability));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), e -> confirmDelete(availability));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY,
                    ButtonVariant.LUMO_ERROR);

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        return grid;
    }

    private void confirmDelete(AvailabilityDto availability) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmer la suppression");

        String itemType = TYPE_EVENT.equals(availability.getType()) ? "l'événement" : "les heures d'ouverture";
        dialog.setText("Êtes-vous sûr de vouloir supprimer " + itemType + " \"" + availability.getTitle() + "\" ?");

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");

        dialog.setConfirmText("Supprimer");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(event -> {
            try {
                availabilityService.deleteById(availability.getId());
                showNotification("Suppression réussie", NotificationVariant.LUMO_SUCCESS);
                refreshView();
            } catch (Exception e) {
                showNotification("Erreur lors de la suppression: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void showEventDialog(AvailabilityDto existingEvent) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        boolean isEditing = existingEvent != null;
        dialog.setHeaderTitle(isEditing ? "Modifier l'événement" : "Nouvel événement");

        // Create form layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Form fields
        TextField titleField = new TextField("Titre");
        titleField.setRequired(true);
        titleField.setWidthFull();

        DatePicker dateField = new DatePicker("Date");
        dateField.setRequired(true);

        TimePicker timeField = new TimePicker("Heure");
        timeField.setRequired(true);

        IntegerField durationField = new IntegerField("Durée (minutes)");
        durationField.setMin(15);
        durationField.setValue(60); // Default 1 hour
        durationField.setStepButtonsVisible(true);
        durationField.setStep(15);

        TextArea detailsField = new TextArea("Détails");
        detailsField.setWidthFull();
        detailsField.setHeight("100px");

        ComboBox<String> statusField = new ComboBox<>("Statut");
        statusField.setItems(STATUS_DRAFT, STATUS_CONFIRMED, STATUS_CANCELLED);
        statusField.setValue(STATUS_CONFIRMED); // Default to confirmed

        // Recurrence options
        ComboBox<String> recurrenceTypeField = new ComboBox<>("Récurrence");
        recurrenceTypeField.setItems("Aucune", "Quotidienne", "Hebdomadaire", "Bimensuelle", "Mensuelle");
        recurrenceTypeField.setValue("Aucune");

        IntegerField recurrenceCountField = new IntegerField("Nombre de récurrences");
        recurrenceCountField.setMin(1);
        recurrenceCountField.setValue(1);
        recurrenceCountField.setVisible(false);

        ComboBox<DayOfWeek> dayOfWeekField = new ComboBox<>("Jour de la semaine");
        dayOfWeekField.setItems(DayOfWeek.values());
        dayOfWeekField.setItemLabelGenerator(day -> day.getDisplayName(TextStyle.FULL, Locale.FRANCE));
        dayOfWeekField.setVisible(false);

        // Enable/disable recurrence fields based on selection
        recurrenceTypeField.addValueChangeListener(event -> {
            boolean hasRecurrence = !"Aucune".equals(event.getValue());
            recurrenceCountField.setVisible(hasRecurrence);
            dayOfWeekField.setVisible("Hebdomadaire".equals(event.getValue()) ||
                    "Bimensuelle".equals(event.getValue()));
        });

        // Apply existing event data if editing
        if (isEditing) {
            titleField.setValue(existingEvent.getTitle());
            dateField.setValue(existingEvent.getDate());
            timeField.setValue(existingEvent.getTime());
            durationField.setValue(existingEvent.getDuration());
            detailsField.setValue(existingEvent.getDetails());
            statusField.setValue(existingEvent.getStatus());
            // Recurrence can't be edited once created, but could be extended in the future
        }

        // Add fields to form
        formLayout.add(titleField, statusField);
        formLayout.add(dateField, timeField);
        formLayout.add(durationField);
        formLayout.add(detailsField, 2);
        formLayout.add(recurrenceTypeField);
        formLayout.add(recurrenceCountField, dayOfWeekField);

        // Buttons
        Button saveButton = new Button("Enregistrer", e -> {
            if (titleField.isEmpty() || dateField.isEmpty() || timeField.isEmpty() || durationField.isEmpty()) {
                showNotification("Veuillez remplir tous les champs obligatoires", NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                if (isEditing) {
                    // Update existing event
                    AvailabilityDto updatedEvent = existingEvent;
                    updatedEvent.setTitle(titleField.getValue());
                    updatedEvent.setDate(dateField.getValue());
                    updatedEvent.setTime(timeField.getValue());
                    updatedEvent.setDuration(durationField.getValue());
                    updatedEvent.setDetails(detailsField.getValue());
                    updatedEvent.setStatus(statusField.getValue());

                    availabilityService.save(updatedEvent);
                    showNotification("Événement mis à jour avec succès", NotificationVariant.LUMO_SUCCESS);
                } else {
                    // Create new event(s)
                    String recurrenceType = recurrenceTypeField.getValue();
                    int recurrenceCount = recurrenceCountField.getValue() != null ? recurrenceCountField.getValue() : 1;

                    if ("Aucune".equals(recurrenceType)) {
                        // Single event
                        AvailabilityDto newEvent = createNewAvailability(
                                titleField.getValue(),
                                dateField.getValue(),
                                timeField.getValue(),
                                durationField.getValue(),
                                detailsField.getValue(),
                                TYPE_EVENT,
                                statusField.getValue());
                        // S'assurer que l'utilisateur est défini
                        newEvent.setUser(currentUser);
                        availabilityService.save(newEvent);
                    } else {
                        // Recurring events
                        List<AvailabilityDto> recurringEvents = generateRecurringEvents(
                                titleField.getValue(),
                                dateField.getValue(),
                                timeField.getValue(),
                                durationField.getValue(),
                                detailsField.getValue(),
                                statusField.getValue(),
                                recurrenceType,
                                recurrenceCount,
                                dayOfWeekField.getValue());

                        for (AvailabilityDto event : recurringEvents) {
                            // S'assurer que l'utilisateur est défini pour chaque événement récurrent
                            event.setUser(currentUser);
                            availabilityService.save(event);
                        }
                    }

                    showNotification("Événement(s) créé(s) avec succès", NotificationVariant.LUMO_SUCCESS);
                }

                dialog.close();
                refreshView();

            } catch (Exception ex) {
                showNotification("Erreur: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonsLayout.setWidthFull();
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Add components to dialog
        VerticalLayout dialogLayout = new VerticalLayout(formLayout, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void showOpeningHoursDialog(AvailabilityDto existingHours) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        boolean isEditing = existingHours != null;
        dialog.setHeaderTitle(isEditing ? "Modifier les heures d'ouverture" : "Nouvelles heures d'ouverture");

        // Create form layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Form fields
        TextField titleField = new TextField("Titre");
        titleField.setRequired(true);
        titleField.setWidthFull();

        DatePicker dateField = new DatePicker("Date");
        dateField.setRequired(true);

        TimePicker openTimeField = new TimePicker("Heure d'ouverture");
        openTimeField.setRequired(true);

        TimePicker closeTimeField = new TimePicker("Heure de fermeture");
        closeTimeField.setRequired(true);

        TextArea detailsField = new TextArea("Détails/Notes");
        detailsField.setWidthFull();
        detailsField.setHeight("100px");

        ComboBox<String> statusField = new ComboBox<>("Statut");
        statusField.setItems(STATUS_DRAFT, STATUS_CONFIRMED, STATUS_CANCELLED);
        statusField.setValue(STATUS_CONFIRMED); // Default to confirmed

        // Special calculate duration span
        Div durationInfo = new Div();
        durationInfo.setWidthFull();
        durationInfo.getStyle().set("margin-top", "1em");
        durationInfo.getStyle().set("font-style", "italic");

        Consumer<LocalTime> updateDuration = (time) -> {
            if (openTimeField.getValue() != null && closeTimeField.getValue() != null) {
                LocalTime openTime = openTimeField.getValue();
                LocalTime closeTime = closeTimeField.getValue();

                if (closeTime.isAfter(openTime)) {
                    int durationMinutes = (closeTime.getHour() - openTime.getHour()) * 60
                            + (closeTime.getMinute() - openTime.getMinute());

                    int hours = durationMinutes / 60;
                    int minutes = durationMinutes % 60;

                    durationInfo.setText("Durée calculée: " +
                            (hours > 0 ? hours + "h" : "") +
                            (minutes > 0 ? minutes + "min" : ""));
                } else {
                    durationInfo.setText("Erreur: L'heure de fermeture doit être après l'heure d'ouverture");
                }
            }
        };

        openTimeField.addValueChangeListener(e -> updateDuration.accept(e.getValue()));
        closeTimeField.addValueChangeListener(e -> updateDuration.accept(e.getValue()));

        // Recurrence options
        ComboBox<String> recurrenceTypeField = new ComboBox<>("Récurrence");
        recurrenceTypeField.setItems("Aucune", "Quotidienne", "Hebdomadaire", "Bimensuelle", "Mensuelle");
        recurrenceTypeField.setValue("Aucune");

        IntegerField recurrenceCountField = new IntegerField("Nombre de récurrences");
        recurrenceCountField.setMin(1);
        recurrenceCountField.setValue(1);
        recurrenceCountField.setVisible(false);

        ComboBox<DayOfWeek> dayOfWeekField = new ComboBox<>("Jour de la semaine");
        dayOfWeekField.setItems(DayOfWeek.values());
        dayOfWeekField.setItemLabelGenerator(day -> day.getDisplayName(TextStyle.FULL, Locale.FRANCE));
        dayOfWeekField.setVisible(false);

        // Enable/disable recurrence fields based on selection
        recurrenceTypeField.addValueChangeListener(event -> {
            boolean hasRecurrence = !"Aucune".equals(event.getValue());
            recurrenceCountField.setVisible(hasRecurrence);
            dayOfWeekField.setVisible("Hebdomadaire".equals(event.getValue()) ||
                    "Bimensuelle".equals(event.getValue()));
        });

        // Apply existing opening hours data if editing
        if (isEditing) {
            titleField.setValue(existingHours.getTitle());
            dateField.setValue(existingHours.getDate());
            openTimeField.setValue(existingHours.getTime());

            // Calculate close time from duration
            LocalTime closeTime = existingHours.getTime().plusMinutes(existingHours.getDuration());
            closeTimeField.setValue(closeTime);

            detailsField.setValue(existingHours.getDetails());
            statusField.setValue(existingHours.getStatus());

            // Update duration display
            updateDuration.accept(openTimeField.getValue());
        }

        // Add fields to form
        formLayout.add(titleField, statusField);
        formLayout.add(dateField);
        formLayout.add(openTimeField, closeTimeField);
        formLayout.add(durationInfo, 2);
        formLayout.add(detailsField, 2);
        formLayout.add(recurrenceTypeField);
        formLayout.add(recurrenceCountField, dayOfWeekField);

        // Buttons
        Button saveButton = new Button("Enregistrer", e -> {
            if (titleField.isEmpty() || dateField.isEmpty() || openTimeField.isEmpty() || closeTimeField.isEmpty()) {
                showNotification("Veuillez remplir tous les champs obligatoires", NotificationVariant.LUMO_ERROR);
                return;
            }

            LocalTime openTime = openTimeField.getValue();
            LocalTime closeTime = closeTimeField.getValue();

            if (!closeTime.isAfter(openTime)) {
                showNotification("L'heure de fermeture doit être après l'heure d'ouverture",
                        NotificationVariant.LUMO_ERROR);
                return;
            }

            // Calculate duration in minutes
            int durationMinutes = (closeTime.getHour() - openTime.getHour()) * 60
                    + (closeTime.getMinute() - openTime.getMinute());

            try {
                if (isEditing) {
                    // Update existing opening hours
                    AvailabilityDto updatedHours = existingHours;
                    updatedHours.setTitle(titleField.getValue());
                    updatedHours.setDate(dateField.getValue());
                    updatedHours.setTime(openTimeField.getValue());
                    updatedHours.setDuration(durationMinutes);
                    updatedHours.setDetails(detailsField.getValue());
                    updatedHours.setStatus(statusField.getValue());

                    availabilityService.save(updatedHours);
                    showNotification("Heures d'ouverture mises à jour avec succès", NotificationVariant.LUMO_SUCCESS);
                } else {
                    // Create new opening hours
                    String recurrenceType = recurrenceTypeField.getValue();
                    int recurrenceCount = recurrenceCountField.getValue() != null ? recurrenceCountField.getValue() : 1;

                    if ("Aucune".equals(recurrenceType)) {
                        // Single entry
                        AvailabilityDto newHours = createNewAvailability(
                                titleField.getValue(),
                                dateField.getValue(),
                                openTimeField.getValue(),
                                durationMinutes,
                                detailsField.getValue(),
                                TYPE_OPENING_HOURS,
                                statusField.getValue());
                        // S'assurer que l'utilisateur est défini
                        newHours.setUser(currentUser);
                        availabilityService.save(newHours);
                    } else {
                        // Recurring entries
                        List<AvailabilityDto> recurringHours = generateRecurringEvents(
                                titleField.getValue(),
                                dateField.getValue(),
                                openTimeField.getValue(),
                                durationMinutes,
                                detailsField.getValue(),
                                statusField.getValue(),
                                recurrenceType,
                                recurrenceCount,
                                dayOfWeekField.getValue());

                        for (AvailabilityDto hours : recurringHours) {
                            hours.setType(TYPE_OPENING_HOURS);
                            // S'assurer que l'utilisateur est défini pour chaque entrée récurrente
                            hours.setUser(currentUser);
                            availabilityService.save(hours);
                        }
                    }

                    showNotification("Heures d'ouverture créées avec succès", NotificationVariant.LUMO_SUCCESS);
                }

                dialog.close();
                refreshView();

            } catch (Exception ex) {
                showNotification("Erreur: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonsLayout.setWidthFull();
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Add components to dialog
        VerticalLayout dialogLayout = new VerticalLayout(formLayout, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private AvailabilityDto createNewAvailability(String title, LocalDate date, LocalTime time,
            int duration, String details, String type, String status) {
        AvailabilityDto availability = new AvailabilityDto();
        availability.setTitle(title);
        availability.setDate(date);
        availability.setTime(time);
        availability.setDuration(duration);
        availability.setDetails(details);
        availability.setType(type);
        availability.setStatus(status);
        availability.setUser(currentUser);
        return availability;
    }

    private List<AvailabilityDto> generateRecurringEvents(String title, LocalDate startDate, LocalTime time,
            int duration, String details, String status,
            String recurrenceType, int recurrenceCount,
            DayOfWeek specificDayOfWeek) {
        List<AvailabilityDto> recurringEvents = new ArrayList<>();
        LocalDate currentDate = startDate;

        // If specific day is selected for weekly/biweekly recurrence
        if (specificDayOfWeek != null &&
                ("Hebdomadaire".equals(recurrenceType) || "Bimensuelle".equals(recurrenceType))) {
            // Adjust start date to the specified day of week if needed
            if (currentDate.getDayOfWeek() != specificDayOfWeek) {
                currentDate = currentDate.with(TemporalAdjusters.next(specificDayOfWeek));
            }
        }

        for (int i = 0; i < recurrenceCount; i++) {
            // Create event for current date
            AvailabilityDto event = createNewAvailability(
                    title, currentDate, time, duration, details, TYPE_EVENT, status);
            recurringEvents.add(event);

            // Calculate next date based on recurrence type
            switch (recurrenceType) {
                case "Quotidienne":
                    currentDate = currentDate.plusDays(1);
                    break;
                case "Hebdomadaire":
                    currentDate = currentDate.plusWeeks(1);
                    break;
                case "Bimensuelle":
                    currentDate = currentDate.plusWeeks(2);
                    break;
                case "Mensuelle":
                    currentDate = currentDate.plusMonths(1);
                    break;
            }
        }

        return recurringEvents;
    }

    private void loadCalendarData() {
        // Récupérer l'EntryProvider et le convertir en InMemoryEntryProvider
        var entryProvider = calendar.getEntryProvider().asInMemory();

        // Supprimer toutes les entrées existantes
        entryProvider.removeAllEntries();

        // Calculate the start and end of the displayed month
        LocalDate firstDay = currentMonth.withDayOfMonth(1);
        LocalDate lastDay = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());

        // Get all availabilities for the current month
        List<AvailabilityDto> availabilities = availabilityService.findByDateBetween(firstDay, lastDay);

        // Add each availability to the calendar
        for (AvailabilityDto availability : availabilities) {
            if (STATUS_CONFIRMED.equals(availability.getStatus())) {
                Entry entry = new Entry();

                // Dans FullCalendar 6.x, on utilise une propriété personnalisée pour l'ID
                // car la méthode setId n'existe plus
                entry.setCustomProperty("id", String.valueOf(availability.getId()));
                entry.setTitle(availability.getTitle());
                entry.setDescription(availability.getDetails());

                // Set start and end times
                LocalDateTime start = LocalDateTime.of(availability.getDate(), availability.getTime());
                LocalDateTime end = start.plusMinutes(availability.getDuration());

                entry.setStart(start);
                entry.setEnd(end);

                // Set color based on type
                if (TYPE_EVENT.equals(availability.getType())) {
                    entry.setColor("#6200EA"); // Purple for events
                } else if (TYPE_OPENING_HOURS.equals(availability.getType())) {
                    entry.setColor("#00C853"); // Green for opening hours
                }

                // Ajouter l'entrée au calendrier via l'EntryProvider
                entryProvider.addEntry(entry);
            }
        }
    }

    private void loadEventsData() {
        // Load only events (not opening hours)
        List<AvailabilityDto> events = availabilityService.findByType(TYPE_EVENT);
        eventsGrid.setItems(events);
    }

    private void loadOpeningHoursData() {
        // Load only opening hours (not events)
        List<AvailabilityDto> openingHours = availabilityService.findByType(TYPE_OPENING_HOURS);
        openHoursGrid.setItems(openingHours);
    }

    private void updateCalendarMonth() {
        // Update the month title
        HorizontalLayout navigationLayout = (HorizontalLayout) calendarLayout.getComponentAt(0);
        H3 monthTitle = (H3) navigationLayout.getComponentAt(1);
        monthTitle.setText(
                currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE) + " " + currentMonth.getYear());

        // Changer la date d'affichage du calendrier
        calendar.gotoDate(currentMonth);

        // Reload calendar data for the new month
        loadCalendarData();
    }

    private void refreshView() {
        if ("calendar".equals(currentView)) {
            loadCalendarData();
        } else if ("events".equals(currentView)) {
            loadEventsData();
        } else if ("openingHours".equals(currentView)) {
            loadOpeningHoursData();
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(3000);
        notification.open();
    }

    // Méthode privée pour trouver un utilisateur administrateur
    private void findAdminUser() {
        List<UserDto> adminUsers = userService.findAll().stream()
                .filter(u -> "ROLE_ADMINISTRATEUR".equals(u.getRole().getName()))
                .collect(Collectors.toList());

        if (!adminUsers.isEmpty()) {
            currentUser = adminUsers.get(0);
            System.out.println("Utilisateur administrateur par défaut utilisé: " + currentUser.getUsername());
        }
    }
}