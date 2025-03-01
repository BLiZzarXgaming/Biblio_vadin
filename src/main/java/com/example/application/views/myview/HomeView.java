package com.example.application.views.myview;

import com.example.application.entity.Availability;
import com.example.application.entity.DTO.AvailabilityDto;
import com.example.application.service.implementation.AvailabilityServiceV2;
import com.example.application.views.MainLayout;
import com.vaadin.componentfactory.Popup;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@PageTitle("Accueil")
@Route(value = "/", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class HomeView extends Composite<VerticalLayout> implements BeforeEnterObserver {

    // TODO : voir comment récupérer les disponibilités depuis la base de données

    @Autowired
    private final AvailabilityServiceV2 availabilityService;

    private FullCalendar calendar;
    private Popup popup;
    private LocalDate currentMonth = LocalDate.now();
    private Span monthLabel;

    public HomeView(@Autowired AvailabilityServiceV2 userService) {
        this.availabilityService = userService;
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setHeight("100%");

        H1 title = new H1("Bibliothèque Anne-Marie Doyon");
        title.getStyle().set("text-align", "center");
        title.getStyle().setWidth("100%");

        getContent().add(title);

        VerticalLayout container = new VerticalLayout();
        container.setWidth("100%");
        container.setHeight("80%");
        container.getStyle().set("flex-grow", "1");

        JsonObject initialOptions = Json.createObject();
        initialOptions.put("max-height", "100%");
        initialOptions.put("timeZone", "local");
        initialOptions.put("header", true);
        initialOptions.put("weekNumbers", false);
        initialOptions.put("eventLimit", false);
        initialOptions.put("navLinks", true);
        initialOptions.put("selectable", true);
        initialOptions.put("firstDay", 1);

        calendar = FullCalendarBuilder.create().withInitialOptions(initialOptions).build();
        calendar.setWidth("100%");
        calendar.setLocale(Locale.CANADA_FRENCH);

        // if (UI.getCurrent().)

        // Définir l'identifiant des éléments d'entrée
        calendar.setEntryDidMountCallback("""
                function(info) {
                    info.el.id = "entry-" + info.event.id;
                }""");

        // Ajouter un écouteur pour les clics sur les entrées
        calendar.addEntryClickedListener(event -> {
            openContextMenu(event.getEntry().getId(), event.getEntry().getDescription());
        });

        // Créer les contrôles de navigation entre les mois
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

        // Créer le conteneur pour le mois avec les boutons de navigation
        Div monthContainer = new Div();
        monthContainer.getStyle().setTextAlign(Style.TextAlign.CENTER);

        monthLabel = new Span(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.CANADA_FRENCH) + " "
                + currentMonth.getYear());
        monthLabel.getStyle().set("font-size", "20px");
        monthLabel.getStyle().set("font-weight", "bold");

        HorizontalLayout navigationLayout = new HorizontalLayout(prevMonthButton, monthLabel, nextMonthButton,
                todayButton);
        navigationLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        navigationLayout.setWidthFull();
        navigationLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        container.add(navigationLayout, calendar);
        container.setFlexGrow(1, calendar);

        getContent().add(container);

        // Charger les données initiales
        loadCalendarData();
    }

    private void updateCalendarMonth() {
        // Mettre à jour le titre du mois
        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.CANADA_FRENCH) + " "
                + currentMonth.getYear());

        // Changer la date d'affichage du calendrier
        calendar.gotoDate(currentMonth);

        // Recharger les données du calendrier pour le nouveau mois
        loadCalendarData();
    }

    private void loadCalendarData() {
        // Récupérer l'EntryProvider et le convertir en InMemoryEntryProvider
        var entryProvider = calendar.getEntryProvider().asInMemory();

        // Supprimer toutes les entrées existantes
        entryProvider.removeAllEntries();

        // Calculer le début et la fin du mois affiché
        LocalDate firstDay = currentMonth.withDayOfMonth(1);
        LocalDate lastDay = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());

        // Récupérer les disponibilités confirmées pour le mois en cours
        List<AvailabilityDto> availabilities = availabilityService.findByStatusAndDateBetween("Confirmed", firstDay,
                lastDay);

        // Ajouter chaque disponibilité au calendrier
        for (AvailabilityDto availability : availabilities) {
            Entry entry = new Entry();

            // Vérifier que la date et l'heure ne sont pas nulles
            if (availability.getDate() != null && availability.getTime() != null) {
                LocalDate date = availability.getDate();
                LocalTime time = availability.getTime();

                // Combiner LocalDate et LocalTime pour obtenir LocalDateTime
                LocalDateTime startDateTime = LocalDateTime.of(date, time);

                // Définir l'heure de début
                entry.setStart(startDateTime);

                // Calculer l'heure de fin en ajoutant la durée
                LocalDateTime endDateTime = startDateTime.plusMinutes(availability.getDuration());

                // Définir l'heure de fin
                entry.setEnd(endDateTime);

                // Définir la couleur en fonction du type
                if ("heureOuverture".equals(availability.getType())) {
                    entry.setColor("green");

                    // Définir le titre de l'entrée
                    entry.setTitle(availability.getTitle());

                    entry.setDescription("Ouvert de " + time.format(DateTimeFormatter.ofPattern("HH:mm")) + " à "
                            + time.plusMinutes(availability.getDuration())
                                    .format(DateTimeFormatter.ofPattern("HH:mm")));
                } else {
                    entry.setColor("orange");

                    // Définir le titre de l'entrée
                    entry.setTitle(availability.getTitle());

                    entry.setDescription(
                            entry.getTitle() + "\n" + "De " + time.format(DateTimeFormatter.ofPattern("HH:mm")) + " à "
                                    + time.plusMinutes(availability.getDuration())
                                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                                    + "\n\n"
                                    + availability.getDetails());
                }

                // Ajouter l'entrée au calendrier
                entryProvider.addEntry(entry);
            } else {
                // Gérer le cas où la date ou l'heure est nulle
                System.err.println("La date ou l'heure de disponibilité est nulle pour : " + availability.getTitle());
            }
        }
    }

    public void openContextMenu(String id, String description) {
        initPopup(); // init the popp

        popup.removeAll(); // remove old content

        popup.getStyle().setPadding("1em");

        TextArea durationEvent = new TextArea();
        durationEvent.setReadOnly(true);
        durationEvent.setValue(description);
        durationEvent.setWidth("100%");
        // durationEvent.getStyle().setPadding("0.5em");
        // durationEvent.setText(description);

        popup.add(durationEvent);
        popup.setFor("entry-" + id);

        popup.show();
    }

    private void initPopup() {
        if (popup == null) {
            popup = new Popup();
            popup.setFocusTrap(true);
            getContent().add(popup);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {

            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATEUR"))) {
                UI.getCurrent().getPage().setLocation("/admin/home");
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_BÉNÉVOLE"))) {
                UI.getCurrent().getPage().setLocation("/volunteer/home");
            }
        }
    }
}
