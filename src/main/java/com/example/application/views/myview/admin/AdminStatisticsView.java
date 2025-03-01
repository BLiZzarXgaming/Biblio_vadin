package com.example.application.views.myview.admin;

import com.example.application.entity.DTO.LoanDto;
import com.example.application.service.implementation.LoanServiceV2;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.service.implementation.ItemServiceV2;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxSizing;
import com.vaadin.flow.theme.lumo.LumoUtility.Border;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;

// Import des classes ApexCharts
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.PieBuilder;
import com.github.appreciated.apexcharts.config.tooltip.builder.YBuilder;
import com.github.appreciated.apexcharts.helper.Series;

// Imports supplémentaires pour les enums
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.yaxis.builder.TitleBuilder;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@PageTitle("Statistiques")
@Route(value = "admin/statistics", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMINISTRATEUR")
public class AdminStatisticsView extends VerticalLayout {

        private static final Logger LOGGER = Logger.getLogger(AdminStatisticsView.class.getName());

        private final LoanServiceV2 loanService;
        private final UserServiceV2 userService;
        private final ItemServiceV2 itemService;

        private VerticalLayout contentLayout;
        private LocalDate startDate;
        private LocalDate endDate;

        @Autowired
        public AdminStatisticsView(LoanServiceV2 loanService, UserServiceV2 userService, ItemServiceV2 itemService) {
                this.loanService = loanService;
                this.userService = userService;
                this.itemService = itemService;

                setSizeFull();
                setPadding(true);

                H2 title = new H2("Statistiques de la bibliothèque");

                // Filtres de date
                startDate = LocalDate.now().minusMonths(3);
                endDate = LocalDate.now();

                DatePicker dpStart = new DatePicker("Date de début", startDate, e -> {
                        if (e.getValue() != null) {
                                startDate = e.getValue();
                                refreshContent();
                        }
                });

                DatePicker dpEnd = new DatePicker("Date de fin", endDate, e -> {
                        if (e.getValue() != null) {
                                endDate = e.getValue();
                                refreshContent();
                        }
                });

                HorizontalLayout filtersLayout = new HorizontalLayout(dpStart, dpEnd);
                filtersLayout.setWidthFull();
                filtersLayout.setPadding(true);

                // Onglets pour différents types de statistiques
                Tab tabEmprunts = new Tab("Emprunts");
                Tab tabUtilisateurs = new Tab("Utilisateurs");
                Tab tabItems = new Tab("Inventaire");

                Tabs tabs = new Tabs(tabEmprunts, tabUtilisateurs, tabItems);
                tabs.setWidthFull();
                tabs.addSelectedChangeListener(event -> {
                        refreshContent();
                });

                // Bouton de génération de rapport
                Button btnGenerateReport = new Button("Générer rapport annuel", e -> {
                        generateAnnualReport();
                });

                HorizontalLayout actionLayout = new HorizontalLayout(tabs, btnGenerateReport);
                actionLayout.setWidthFull();
                actionLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

                // Layout principal pour le contenu
                contentLayout = new VerticalLayout();
                contentLayout.setSizeFull();
                contentLayout.setPadding(true);

                add(title, filtersLayout, actionLayout, contentLayout);

                // Afficher initialement les statistiques d'emprunts
                showLoanStatistics();
        }

        private void refreshContent() {
                contentLayout.removeAll();

                // Récupérer le HorizontalLayout en premier
                HorizontalLayout actionLayout = (HorizontalLayout) getComponentAt(2);
                // Puis récupérer les Tabs qui sont le premier composant du HorizontalLayout
                Tabs tabs = (Tabs) actionLayout.getComponentAt(0);
                Tab selectedTab = tabs.getSelectedTab();

                if (selectedTab.getLabel().equals("Emprunts")) {
                        showLoanStatistics();
                } else if (selectedTab.getLabel().equals("Utilisateurs")) {
                        showUserStatistics();
                } else if (selectedTab.getLabel().equals("Inventaire")) {
                        showItemStatistics();
                }
        }

        private void showLoanStatistics() {
                H3 subtitle = new H3("Statistiques des emprunts");
                contentLayout.add(subtitle);

                List<LoanDto> loans = loanService.findAll();
                List<LoanDto> filteredLoans = loans.stream()
                                .filter(loan -> !loan.getLoanDate().isBefore(startDate)
                                                && !loan.getLoanDate().isAfter(endDate))
                                .collect(Collectors.toList());

                try {
                        // Ajouter le panneau de statistiques clés
                        Component keyStatsPanel = createKeyStatsPanel(filteredLoans);
                        contentLayout.add(keyStatsPanel);

                        // Graphique des emprunts par mois
                        ApexCharts loansByMonthChart = createLoansByMonthChart(filteredLoans);
                        Div monthChartContainer = new Div(loansByMonthChart);
                        monthChartContainer.setClassName("chart-container");
                        monthChartContainer.setWidth("100%");
                        monthChartContainer.setHeight("450px");
                        monthChartContainer.getStyle().set("margin-top", "20px");

                        // Graphique des emprunts par statut
                        ApexCharts loansByStatusChart = createLoansByStatusChart(filteredLoans);
                        Div statusChartContainer = new Div(loansByStatusChart);
                        statusChartContainer.setClassName("chart-container");
                        statusChartContainer.setWidth("100%");
                        statusChartContainer.setHeight("450px");
                        statusChartContainer.getStyle().set("margin-top", "20px");

                        contentLayout.add(
                                        createSectionWithTitle("Évolution des emprunts par mois", monthChartContainer),
                                        createSectionWithTitle("Répartition des emprunts par statut",
                                                        statusChartContainer));

                        LOGGER.info("Graphiques des emprunts créés et ajoutés à la vue");
                } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de l'affichage des statistiques d'emprunts", e);
                        Notification.show("Erreur: " + e.getMessage())
                                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
        }

        private void showUserStatistics() {
                H3 subtitle = new H3("Statistiques des utilisateurs");
                contentLayout.add(subtitle);

                try {
                        // Ajouter le panneau de statistiques clés
                        Component userKeyStatsPanel = createUserKeyStatsPanel();
                        contentLayout.add(userKeyStatsPanel);

                        // Graphique sur les utilisateurs
                        ApexCharts usersByRoleChart = createUsersByRoleChart();

                        // Créer un conteneur pour le graphique avec une taille explicite
                        Div chartContainer = new Div(usersByRoleChart);
                        chartContainer.setClassName("chart-container");
                        chartContainer.setWidth("100%");
                        chartContainer.setHeight("500px"); // Légèrement plus grand que le graphique
                        chartContainer.getStyle().set("margin-top", "20px");

                        // Ajouter le conteneur au layout
                        contentLayout.add(
                                        createSectionWithTitle("Répartition des utilisateurs par rôle",
                                                        chartContainer));

                        // Ajouter un log pour déboguer
                        LOGGER.info("Graphique des utilisateurs par rôle créé et ajouté à la vue");

                } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de l'affichage des statistiques d'utilisateurs", e);
                        Notification.show("Erreur: " + e.getMessage())
                                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
        }

        private void showItemStatistics() {
                H3 subtitle = new H3("Statistiques de l'inventaire");
                contentLayout.add(subtitle);

                try {
                        // Ajouter le panneau de statistiques clés
                        Component itemKeyStatsPanel = createItemKeyStatsPanel();
                        contentLayout.add(itemKeyStatsPanel);

                        // Graphique sur les documents
                        ApexCharts itemsByTypeChart = createItemsByTypeChart();
                        Div chartContainer = new Div(itemsByTypeChart);
                        chartContainer.setClassName("chart-container");
                        chartContainer.setWidth("100%");
                        chartContainer.setHeight("450px");
                        chartContainer.getStyle().set("margin-top", "20px");

                        contentLayout.add(
                                        createSectionWithTitle("Répartition des documents par type", chartContainer));

                        LOGGER.info("Graphique des types de documents créé et ajouté à la vue");
                } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de l'affichage des statistiques de documents", e);
                        Notification.show("Erreur: " + e.getMessage())
                                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
        }

        private Component createSectionWithTitle(String title, Component component) {
                VerticalLayout layout = new VerticalLayout();
                layout.add(new H3(title));
                layout.add(component);
                layout.setWidthFull();
                return layout;
        }

        private ApexCharts createLoansByMonthChart(List<LoanDto> loans) {
                try {
                        // Regrouper les emprunts par mois
                        Map<String, Long> loansByMonth = new LinkedHashMap<>();

                        // Initialiser tous les mois dans la plage de dates
                        LocalDate current = startDate.withDayOfMonth(1);
                        while (!current.isAfter(endDate)) {
                                String monthYear = current
                                                .format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH));
                                loansByMonth.put(monthYear, 0L);
                                current = current.plusMonths(1);
                        }

                        // Compter les emprunts par mois
                        for (LoanDto loan : loans) {
                                String monthYear = loan.getLoanDate()
                                                .format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH));
                                loansByMonth.compute(monthYear, (k, v) -> v + 1);
                        }

                        LOGGER.info("Données du graphique: " + loansByMonth);

                        // Préparer les données pour le graphique
                        String[] monthLabels = loansByMonth.keySet().toArray(new String[0]);
                        Number[] monthValues = loansByMonth.values().toArray(new Number[0]);

                        // Création du graphique ApexCharts
                        ApexCharts chart = ApexChartsBuilder.get()
                                        .withChart(ChartBuilder.get()
                                                        .withType(Type.LINE)
                                                        .withZoom(ZoomBuilder.get()
                                                                        .withEnabled(true)
                                                                        .build())
                                                        .build())
                                        .withTitle(TitleSubtitleBuilder.get()
                                                        .withText("Évolution des emprunts par mois")
                                                        .withAlign(Align.LEFT)
                                                        .build())
                                        .withSeries(new Series<>("Nombre d'emprunts", monthValues))
                                        .withXaxis(XAxisBuilder.get()
                                                        .withCategories(monthLabels)
                                                        .build())
                                        .withYaxis(YAxisBuilder.get()
                                                        .withTitle(TitleBuilder.get()
                                                                        .withText("Nombre d'emprunts")
                                                                        .build())
                                                        .build())
                                        .withStroke(StrokeBuilder.get()
                                                        .withCurve(Curve.SMOOTH)
                                                        .build())
                                        .withTooltip(TooltipBuilder.get()
                                                        .withY(YBuilder.get()
                                                                        .withFormatter("function(value) { return value + ' emprunts'; }")
                                                                        .build())
                                                        .build())
                                        .build();

                        return chart;
                } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de la création du graphique des emprunts par mois", e);
                        throw new RuntimeException("Erreur lors de la création du graphique: " + e.getMessage(), e);
                }
        }

        private ApexCharts createLoansByStatusChart(List<LoanDto> loans) {
                try {
                        // Regrouper les emprunts par statut
                        Map<String, Long> loansByStatus = loans.stream()
                                        .collect(Collectors.groupingBy(LoanDto::getStatus, Collectors.counting()));

                        LOGGER.info("Données du graphique de statut: " + loansByStatus);

                        // Convertir les données pour le graphique
                        String[] statusLabels = loansByStatus.keySet().toArray(new String[0]);

                        // Convertir les valeurs en Double[] pour le graphique PIE
                        Double[] statusValues = loansByStatus.values().stream()
                                        .map(Long::doubleValue)
                                        .toArray(Double[]::new);

                        // Création du graphique ApexCharts
                        ApexCharts chart = ApexChartsBuilder.get()
                                        .withChart(ChartBuilder.get()
                                                        .withType(Type.PIE)
                                                        .withHeight("400") // Spécifier la hauteur sans unité "px"
                                                        .withWidth("100%")
                                                        .build())
                                        .withTitle(TitleSubtitleBuilder.get()
                                                        .withText("Répartition des emprunts par statut")
                                                        .withAlign(Align.LEFT)
                                                        .build())
                                        .withLabels(statusLabels)
                                        .withSeries(statusValues) // Utiliser directement le tableau de Double
                                        .withLegend(LegendBuilder.get()
                                                        .withPosition(Position.RIGHT)
                                                        .build())
                                        .withPlotOptions(PlotOptionsBuilder.get()
                                                        .withPie(PieBuilder.get()
                                                                        .withExpandOnClick(true)
                                                                        .build())
                                                        .build())
                                        .withTooltip(TooltipBuilder.get()
                                                        .withY(YBuilder.get()
                                                                        .withFormatter("function(value) { return value + ' emprunts'; }")
                                                                        .build())
                                                        .build())
                                        .build();

                        return chart;
                } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de la création du graphique des emprunts par statut", e);
                        throw new RuntimeException("Erreur lors de la création du graphique: " + e.getMessage(), e);
                }
        }

        private ApexCharts createUsersByRoleChart() {
                try {
                        // Simuler des données (à remplacer par de vraies données)
                        Map<String, Double> usersByRole = new HashMap<>();
                        usersByRole.put("Membres", 120.0);
                        usersByRole.put("Bénévoles", 15.0);
                        usersByRole.put("Administrateurs", 5.0);

                        LOGGER.info("Données du graphique des utilisateurs: " + usersByRole);

                        // Préparer les données pour le graphique
                        String[] roleLabels = usersByRole.keySet().toArray(new String[0]);
                        Double[] roleValues = usersByRole.values().toArray(new Double[0]);

                        // Création du graphique ApexCharts avec une approche différente
                        ApexCharts chart = ApexChartsBuilder.get()
                                        .withChart(ChartBuilder.get()
                                                        .withType(Type.DONUT)
                                                        .withHeight("400")
                                                        .build())
                                        .withLabels(roleLabels)
                                        .withSeries(roleValues) // Utiliser directement le tableau de Double
                                        .build();

                        return chart;
                } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de la création du graphique des utilisateurs par rôle",
                                        e);
                        throw new RuntimeException("Erreur lors de la création du graphique: " + e.getMessage(), e);
                }
        }

        private ApexCharts createItemsByTypeChart() {
                try {
                        // Simuler des données (à remplacer par de vraies données)
                        Map<String, Number> itemsByType = new HashMap<>();
                        itemsByType.put("Livres", 1500);
                        itemsByType.put("Magazines", 300);
                        itemsByType.put("Jeux de société", 100);

                        LOGGER.info("Données du graphique des types de documents: " + itemsByType);

                        // Préparer les données pour le graphique
                        String[] typeLabels = itemsByType.keySet().toArray(new String[0]);
                        Number[] typeValues = itemsByType.values().toArray(new Number[0]);

                        // Création du graphique ApexCharts
                        ApexCharts chart = ApexChartsBuilder.get()
                                        .withChart(ChartBuilder.get()
                                                        .withType(Type.BAR)
                                                        .build())
                                        .withTitle(TitleSubtitleBuilder.get()
                                                        .withText("Répartition des documents par type")
                                                        .withAlign(Align.LEFT)
                                                        .build())
                                        .withSeries(new Series<>("Quantité", typeValues))
                                        .withXaxis(XAxisBuilder.get()
                                                        .withCategories(typeLabels)
                                                        .build())
                                        .withYaxis(YAxisBuilder.get()
                                                        .withTitle(TitleBuilder.get()
                                                                        .withText("Nombre de documents")
                                                                        .build())
                                                        .build())
                                        .withPlotOptions(PlotOptionsBuilder.get()
                                                        .withBar(BarBuilder.get()
                                                                        .withDistributed(true)
                                                                        .withHorizontal(false)
                                                                        .build())
                                                        .build())
                                        .withTooltip(TooltipBuilder.get()
                                                        .withY(YBuilder.get()
                                                                        .withFormatter("function(value) { return value + ' documents'; }")
                                                                        .build())
                                                        .build())
                                        .build();

                        // IMPORTANT: Définir explicitement les dimensions après la création
                        chart.setWidth("100%");
                        chart.setHeight("400px");

                        return chart;
                } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de la création du graphique des types de documents", e);
                        throw new RuntimeException("Erreur lors de la création du graphique: " + e.getMessage(), e);
                }
        }

        private void generateAnnualReport() {
                try {
                        // Générer un nom de fichier pour le rapport
                        int year = LocalDate.now().getYear();
                        String filename = "rapport_bibliotheque_" + year + ".txt";

                        // Générer le contenu textuel du rapport
                        StringBuilder reportContent = new StringBuilder();
                        reportContent.append("RAPPORT ANNUEL DE LA BIBLIOTHÈQUE - ").append(year).append("\n\n");
                        reportContent.append("Généré le: ")
                                        .append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                        .append("\n\n");

                        // Ajouter des statistiques au rapport
                        reportContent.append("STATISTIQUES DES EMPRUNTS\n");
                        reportContent.append("------------------------\n");
                        reportContent.append("Nombre total d'emprunts: ").append(loanService.findAll().size())
                                        .append("\n");

                        // Créer un lien de téléchargement
                        StreamResource resource = new StreamResource(filename,
                                        () -> new ByteArrayInputStream(reportContent.toString().getBytes()));

                        Anchor downloadLink = new Anchor(resource, "Télécharger le rapport");
                        downloadLink.getElement().setAttribute("download", true);

                        // Afficher un message de confirmation avec le lien de téléchargement
                        VerticalLayout layout = new VerticalLayout();
                        layout.add(
                                        new Paragraph("Le rapport annuel a été généré avec succès (format texte)."),
                                        new Paragraph("Note: Ce rapport est au format texte (.txt) et non PDF."),
                                        downloadLink);

                        contentLayout.add(layout);

                        Notification.show("Rapport généré avec succès. Cliquez sur le lien pour télécharger.")
                                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de la génération du rapport", e);
                        Notification.show("Erreur lors de la génération du rapport: " + e.getMessage())
                                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
        }

        /**
         * Crée un panneau affichant les statistiques clés
         */
        private Component createKeyStatsPanel(List<LoanDto> loans) {
                HorizontalLayout statsPanel = new HorizontalLayout();
                statsPanel.setWidthFull();
                statsPanel.setPadding(true);
                statsPanel.setSpacing(true);

                // Statistique 1: Catégorie la plus populaire
                String mostPopularCategory = calculateMostPopularCategory(loans);
                Component statBox1 = createStatBox(
                                "Catégorie la plus populaire",
                                mostPopularCategory,
                                VaadinIcon.BOOK);

                // Statistique 2: Type de document le plus emprunté
                String mostBorrowedType = calculateMostBorrowedType(loans);
                Component statBox2 = createStatBox(
                                "Document le plus emprunté",
                                mostBorrowedType,
                                VaadinIcon.FILE_TEXT);

                // Statistique 3: Valeur totale de l'inventaire en dollars canadiens
                String totalValue = calculateTotalValue();
                Component statBox3 = createStatBox(
                                "Valeur de l'inventaire",
                                totalValue,
                                VaadinIcon.DOLLAR);

                // Statistique 4: Nombre de réservations
                int reservationsCount = calculateReservationsCount();
                Component statBox4 = createStatBox(
                                "Réservations",
                                String.valueOf(reservationsCount),
                                VaadinIcon.CALENDAR_CLOCK);

                statsPanel.add(statBox1, statBox2, statBox3, statBox4);
                statsPanel.setDefaultVerticalComponentAlignment(Alignment.STRETCH);

                return statsPanel;
        }

        /**
         * Crée une boîte de statistique individuelle
         */
        private Component createStatBox(String title, String value, VaadinIcon icon) {
                Div box = new Div();
                box.getStyle()
                                .set("background-color", "var(--lumo-base-color)")
                                .set("border-radius", "var(--lumo-border-radius-m)")
                                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                                .set("padding", "var(--lumo-space-m)")
                                .set("text-align", "center")
                                .set("flex-grow", "1");

                Icon statIcon = new Icon(icon);
                statIcon.getStyle()
                                .set("color", "var(--lumo-primary-color)")
                                .set("font-size", "2.5em")
                                .set("margin-bottom", "var(--lumo-space-s)");

                H3 statValue = new H3(value);
                statValue.getStyle()
                                .set("margin", "var(--lumo-space-xs) 0")
                                .set("font-size", "1.8em")
                                .set("font-weight", "bold")
                                .set("color", "var(--lumo-primary-text-color)");

                Span statTitle = new Span(title);
                statTitle.getStyle()
                                .set("color", "var(--lumo-secondary-text-color)")
                                .set("font-size", "var(--lumo-font-size-s)");

                box.add(statIcon, statValue, statTitle);

                return box;
        }

        /**
         * Calcule la catégorie la plus populaire basée sur les emprunts
         */
        private String calculateMostPopularCategory(List<LoanDto> loans) {
                // Simulé pour le moment - à remplacer par la vraie logique
                return "Romans";
        }

        /**
         * Détermine le type de document le plus emprunté
         */
        private String calculateMostBorrowedType(List<LoanDto> loans) {
                // Simulé pour le moment - à remplacer par la vraie logique
                return "Livres";
        }

        /**
         * Calcule la valeur totale des documents de l'inventaire en dollars canadiens
         */
        private String calculateTotalValue() {
                // Simulé pour le moment - à remplacer par la vraie logique
                return "12 500 $ CAD";
        }

        /**
         * Calcule le nombre total de réservations
         */
        private int calculateReservationsCount() {
                // Simulé pour le moment - à remplacer par la vraie logique
                return 24;
        }

        /**
         * Crée un panneau affichant les statistiques clés pour les utilisateurs
         */
        private Component createUserKeyStatsPanel() {
                HorizontalLayout statsPanel = new HorizontalLayout();
                statsPanel.setWidthFull();
                statsPanel.setPadding(true);
                statsPanel.setSpacing(true);

                // Statistique 1: Nombre total d'utilisateurs
                Component statBox1 = createStatBox(
                                "Nombre total d'utilisateurs",
                                "140",
                                VaadinIcon.USERS);

                // Statistique 2: Nouveaux utilisateurs ce mois
                Component statBox2 = createStatBox(
                                "Nouveaux utilisateurs (mois)",
                                "12",
                                VaadinIcon.USER_CARD);

                // Statistique 3: Utilisateurs actifs
                Component statBox3 = createStatBox(
                                "Utilisateurs actifs",
                                "85%",
                                VaadinIcon.CHART);

                // Statistique 4: Utilisateur le plus actif
                Component statBox4 = createStatBox(
                                "Membre le plus actif",
                                "M. Martin",
                                VaadinIcon.STAR);

                statsPanel.add(statBox1, statBox2, statBox3, statBox4);
                statsPanel.setDefaultVerticalComponentAlignment(Alignment.STRETCH);

                return statsPanel;
        }

        /**
         * Crée un panneau affichant les statistiques clés pour l'inventaire
         */
        private Component createItemKeyStatsPanel() {
                HorizontalLayout statsPanel = new HorizontalLayout();
                statsPanel.setWidthFull();
                statsPanel.setPadding(true);
                statsPanel.setSpacing(true);

                // Statistique 1: Nombre total de documents
                Component statBox1 = createStatBox(
                                "Nombre total de documents",
                                "1900",
                                VaadinIcon.ARCHIVES);

                // Statistique 2: Acquisitions récentes
                Component statBox2 = createStatBox(
                                "Acquisitions récentes",
                                "37",
                                VaadinIcon.PLUS_CIRCLE);

                // Statistique 3: Valeur totale de l'inventaire
                Component statBox3 = createStatBox(
                                "Valeur de l'inventaire",
                                "12 500 $ CAD",
                                VaadinIcon.DOLLAR);

                // Statistique 4: Document le plus populaire
                Component statBox4 = createStatBox(
                                "Document le plus populaire",
                                "Le Petit Prince",
                                VaadinIcon.BOOK);

                statsPanel.add(statBox1, statBox2, statBox3, statBox4);
                statsPanel.setDefaultVerticalComponentAlignment(Alignment.STRETCH);

                return statsPanel;
        }

        @SuppressWarnings("unchecked")
        private <T extends Component> T getComponentAt(int index, Class<T> type) {
                return (T) getComponentAt(index);
        }
}