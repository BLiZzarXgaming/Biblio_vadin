package com.example.application.views.myview;

import com.example.application.entity.Availability;
import com.example.application.service.implementation.AvailabilityServiceImpl;
import com.example.application.views.MainLayout;
import com.vaadin.componentfactory.Popup;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.Timezone;

import java.time.*;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@PageTitle("Accueil")
@Route(value = "/", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class HomeView extends Composite<VerticalLayout> {

    // TODO : voir comment récupérer les disponibilités depuis la base de données

    @Autowired
    private final AvailabilityServiceImpl availabilityService;

    private Popup popup;

    public HomeView(@Autowired AvailabilityServiceImpl userService) {
        this.availabilityService = userService;
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setHeight("100%");

        H1 title = new H1("Bienvenue sur l'application de gestion de projet");

        getContent().add(title);

        VerticalLayout container = new VerticalLayout();
        container.setWidth("100%");
        container.setHeight("80%");
        container.getStyle().set("flex-grow", "1");

        JsonObject initialOptions = Json.createObject();
        initialOptions.put("max-height", "100%");
        //initialOptions.put("timeZone", "UTC");
        initialOptions.put("header", true); // TODO : voir comment activer les headers
        initialOptions.put("weekNumbers", false);
        initialOptions.put("eventLimit", false); // pass an int value to limit the entries per day
        initialOptions.put("navLinks", true);
        initialOptions.put("selectable", true);


        FullCalendar calendar = FullCalendarBuilder.create().withInitialOptions(initialOptions).build();
        calendar.setTimezone(new Timezone(ZoneId.of("America/Montreal")));

        calendar.setWidth("100%");
        calendar.setLocale(Locale.CANADA_FRENCH);
        calendar.addEntryClickedListener(event -> {
            Notification.show("Entry clicked: " + event.getEntry().getTitle());
        });

        // Définir l'identifiant des éléments d'entrée
        calendar.setEntryDidMountCallback("""
                function(info) {
                    info.el.id = "entry-" + info.event.id;
                }""");

        // Ajouter un écouteur pour les clics sur les entrées
        calendar.addEntryClickedListener(event -> {
            openContextMenu(event.getEntry().getId());
        });

        initPopup();

        //ajout des events
        // Récupérer les disponibilités confirmées depuis la base de données
        LocalDate currentDate = LocalDate.now(ZoneId.of("America/Montreal"));
        LocalDate endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        List<Availability> availabilities = availabilityService.findAvailabilitiesByStatusAndDateBetween("Confirmed", currentDate, endOfMonth );

        // Ajouter les disponibilités au calendrier
        for (Availability availability : availabilities) {
            Entry entry = new Entry();



            // Verify that date and time are not null
            if (availability.getDate() != null && availability.getTime() != null) {
                LocalDate date = availability.getDate(); // LocalDate
                LocalTime time = availability.getTime(); // LocalTime

                // Combine LocalDate and LocalTime to get LocalDateTime
                LocalDateTime startDateTime = LocalDateTime.of(date, time);

                // Define the start time
                entry.setStart(startDateTime);

                // Calculate the end time by adding the duration
                LocalDateTime endDateTime = startDateTime.plusMinutes(availability.getDuration());

                // Define the end time
                entry.setEnd(endDateTime);

                // Set the color based on the type
                if ("heureOuverture".equals(availability.getType())) {
                    entry.setColor("green");
                } else {
                    entry.setColor("orange");
                }

                // Set the title of the entry
                entry.setTitle(availability.getTitle() + " de " + time.toString() + " à " + time.plusMinutes(availability.getDuration()).toString());

                // Add the entry to the calendar
                calendar.getEntryProvider().asInMemory().addEntry(entry);
            } else {
                // Handle the case where date or time is null
                System.err.println("La date ou l'heure de disponibilité est nulle pour : " + availability.getTitle());
            }
        }


        // solution pour afficher le mois en haut du calendrier (pas de header)
        Div monthContainer = new Div();
        monthContainer.getStyle().setTextAlign(Style.TextAlign.CENTER);

        //currentDate = LocalDate.now(ZoneId.of("America/Montreal"));
        String currentMonth = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.CANADA_FRENCH);
        Span monthLabel = new Span(currentMonth);
        monthLabel.getStyle().set("font-size", "20px");
        monthLabel.getStyle().set("font-weight", "bold");

        monthContainer.getStyle().setWidth("100%");
        monthContainer.add(monthLabel);

        container.add(monthContainer, calendar);
        container.setFlexGrow(1, calendar);

        getContent().add(container);
    }


    public void openContextMenu (String id){
        initPopup(); // init the popp

        popup.removeAll(); // remove old content


        // setup the context menu
        // (side note: the list box shows a checkmark, when selecting an item, therefore you may want to use a different
        // component for a real application or hide the checkmark with CSS)
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Option A", "Option B", "Option C");
        listBox.addValueChangeListener(event -> {
            Notification.show("Selected " + event.getValue());
            popup.hide();
        });

        popup.add(listBox);
        popup.setFor("entry-" + id);

        popup.show();
    }

    private void initPopup () {
        if (popup == null) {
            popup = new Popup();
            popup.setFocusTrap(true);
           getContent().add(popup);
        }
    }
}

