package com.example.application.views.myview.membre;

import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.User;
import com.example.application.service.implementation.LoanServiceV2;
import com.example.application.service.implementation.UserRelationshipService;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.utils.StatusUtils;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Mes Emprunts")
@Route(value = "membre/emprunts", layout = MainLayout.class)
@RolesAllowed("ROLE_MEMBRE")
public class MembreEmpruntsView extends VerticalLayout {

    private final LoanServiceV2 loanService;
    private final UserServiceV2 userService;
    private final UserRelationshipService userRelationshipService;

    private Grid<LoanDto> loansGrid;
    private UserDto currentUser;
    private List<LoanDto> allLoans = new ArrayList<>();
    private Map<Long, String> childNames = new HashMap<>();

    public MembreEmpruntsView(
            LoanServiceV2 loanService,
            UserServiceV2 userService,
            UserRelationshipService userRelationshipService) {

        this.loanService = loanService;
        this.userService = userService;
        this.userRelationshipService = userRelationshipService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        currentUser = userService.findByUsername(username).orElse(null);

        if (currentUser != null) {
            add(createHeader());
            add(createLoansGrid());
            loadLoans();
        } else {
            add(new H2("Utilisateur non trouvé"));
        }
    }

    private Component createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setSpacing(false);
        header.setPadding(false);

        H2 title = new H2("Mes documents empruntés");
        header.add(title);

        return header;
    }

    private Component createLoansGrid() {
        loansGrid = new Grid<>();

        // Add columns
        loansGrid.addColumn(loan -> loan.getCopy().getItem().getTitle())
                .setHeader("Titre")
                .setAutoWidth(true)
                .setFlexGrow(1);

        loansGrid.addColumn(loan -> {
            return StatusUtils.DocTypes.toFrench(loan.getCopy().getItem().getType());
        }).setHeader("Type").setAutoWidth(true);

        loansGrid.addColumn(loan -> loan.getLoanDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date d'emprunt")
                .setAutoWidth(true);

        loansGrid.addColumn(loan -> loan.getReturnDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date de retour prévue")
                .setAutoWidth(true);

        // Status column with styling based on due date
        loansGrid.addColumn(new ComponentRenderer<>(this::createStatusComponent))
                .setHeader("Statut")
                .setAutoWidth(true);

        // Add owner column (for parents viewing children's loans)
        loansGrid.addColumn(loan -> {
            if (loan.getMember().getId().equals(currentUser.getId())) {
                return "Moi";
            } else {
                return childNames.getOrDefault(loan.getMember().getId(), "Enfant");
            }
        }).setHeader("Emprunteur").setAutoWidth(true);

        loansGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        loansGrid.setHeight("70vh");

        return loansGrid;
    }

    private Component createStatusComponent(LoanDto loan) {
        Span status = new Span();
        status.getElement().getThemeList().add("badge");

        LocalDate dueDate = loan.getReturnDueDate();
        LocalDate today = LocalDate.now();

        long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

        if (dueDate.isBefore(today)) {
            // Overdue
            status.setText("En retard de " + Math.abs(daysUntilDue) + " jour(s)");
            status.getElement().getThemeList().add("error");
        } else if (daysUntilDue <= 3) {
            // Due soon (3 days or less)
            status.setText("À rendre dans " + daysUntilDue + " jour(s)");
            status.getElement().getThemeList().add("warning");
        } else {
            // Normal status
            status.setText("Dans les délais");
            status.getElement().getThemeList().add("success");
        }

        return status;
    }

    private void loadLoans() {
        allLoans.clear();
        childNames.clear();

        if (currentUser == null)
            return;

        // Load user's own loans
        List<LoanDto> userLoans = loanService.findByMember(currentUser.getId())
                .stream()
                .filter(loan -> StatusUtils.LoanStatus.BORROWED.equals(loan.getStatus()))
                .collect(Collectors.toList());

        allLoans.addAll(userLoans);

        // Check if user is a parent, load children's loans if so
        if (currentUser.getIsChild() == null || !currentUser.getIsChild()) {
            User userEntity = new User();
            userEntity.setId(currentUser.getId());

            List<User> children = userRelationshipService.findChildrenByParentId(currentUser.getId());

            for (User child : children) {
                // Store child name for display
                childNames.put(child.getId(), child.getFirstName() + " " + child.getLastName());

                // Get child's loans
                List<LoanDto> childLoans = loanService.findByMember(child.getId())
                        .stream()
                        .filter(loan -> StatusUtils.LoanStatus.BORROWED.equals(loan.getStatus()))
                        .collect(Collectors.toList());

                allLoans.addAll(childLoans);
            }
        }

        // Sort all loans by due date (earliest first)
        allLoans.sort((a, b) -> a.getReturnDueDate().compareTo(b.getReturnDueDate()));

        // Update grid
        loansGrid.setItems(allLoans);

        // Add information section if there are loans
        if (!allLoans.isEmpty()) {
            addLoanStatusLegend();
        }
    }

    private void addLoanStatusLegend() {
        VerticalLayout legendLayout = new VerticalLayout();
        legendLayout.setSpacing(false);
        legendLayout.setPadding(true);

        H3 legendTitle = new H3("Légende des statuts");
        legendTitle.getStyle().set("margin-top", "20px");
        legendTitle.getStyle().set("margin-bottom", "10px");

        Span normalStatus = new Span("Dans les délais");
        normalStatus.getElement().getThemeList().add("badge");
        normalStatus.getElement().getThemeList().add("success");

        Span warningStatus = new Span("À rendre dans 3 jours ou moins");
        warningStatus.getElement().getThemeList().add("badge");
        warningStatus.getElement().getThemeList().add("warning");

        Span errorStatus = new Span("En retard");
        errorStatus.getElement().getThemeList().add("badge");
        errorStatus.getElement().getThemeList().add("error");

        legendLayout.add(legendTitle, normalStatus, warningStatus, errorStatus);
        add(legendLayout);
    }
}