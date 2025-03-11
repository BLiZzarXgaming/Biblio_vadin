package com.example.application.views.myview;

import com.example.application.entity.DTO.BoardGameDto;
import com.example.application.entity.DTO.BookDto;
import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.DTO.MagazineDto;
import com.example.application.service.implementation.BoardGameServiceV2;
import com.example.application.service.implementation.BookServiceV2;
import com.example.application.service.implementation.ItemServiceV2;
import com.example.application.service.implementation.MagazineServiceV2;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Optional;

@Route(value = "item", layout = MainLayout.class)
@PageTitle("Détails")
@AnonymousAllowed
public class ItemDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private ItemServiceV2 itemService;
    private BookServiceV2 bookService;
    private MagazineServiceV2 magazineService;
    private BoardGameServiceV2 boardGameService;

    private Optional<ItemDto> item;

    public ItemDetailView(ItemServiceV2 itemService, BookServiceV2 bookService, MagazineServiceV2 magazineService, BoardGameServiceV2 boardGameService) {
        this.itemService = itemService;
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        try {
            Long itemId = parameter;
            item = itemService.findById(itemId);

            if (item.isPresent()) {
                displayItemDetails();
            } else {
                Notification.show("Item non trouvé", 3000, Notification.Position.MIDDLE);
                event.forwardTo(CatalogueView.class);
            }
        } catch (NumberFormatException e) {
            Notification.show("Identifiant de l'item invalide", 3000, Notification.Position.MIDDLE);
            event.forwardTo(CatalogueView.class);
        }
    }

    private void displayItemDetails() {
        add(new H1(item.get().getTitle()));
        add(new Paragraph("Type : " + item.get().getType()));
        add(new Paragraph("Catégorie : " + item.get().getCategory().getName()));
        add(new Paragraph("Éditeur : " + item.get().getPublisher().getName()));
        add(new Paragraph("Valeur : " + item.get().getValue()));

        // Afficher les détails spécifiques en fonction du type
        if ("Livre".equals(item.get().getType())) {
            Optional<BookDto> book = bookService.findById(item.get().getId());
            if (book.isPresent()) {
                add(new Paragraph("Auteur : " + book.get().getAuthor()));
                add(new Paragraph("ISBN : " + book.get().getIsbn()));
                add(new Paragraph("Date de Publication : " + book.get().getPublicationDate()));
            }
        } else if ("Revue".equals(item.get().getType())) {
            Optional<MagazineDto> magazine = magazineService.findById(item.get().getId());
            if (magazine.isPresent()) {
                add(new Paragraph("ISNI : " + magazine.get().getIsni()));
                add(new Paragraph("Mois : " + magazine.get().getMonth()));
                add(new Paragraph("Date de Publication : " + magazine.get().getPublicationDate()));
            }
        } else if ("Jeu".equals(item.get().getType())) {
            Optional<BoardGameDto> game = boardGameService.findById(item.get().getId());
            if (game.isPresent()) {
                add(new Paragraph("Nombre de Pièces : " + game.get().getNumberOfPieces()));
                add(new Paragraph("Âge Recommandé : " + game.get().getRecommendedAge()));
                add(new Paragraph("Règles du Jeu : " + game.get().getGameRules()));
            }
        }

        // Bouton pour retourner au catalogue
        Button backButton = new Button("Retour au Catalogue", e -> {
            getUI().ifPresent(ui -> ui.navigate(CatalogueView.class));
        });
        add(backButton);
    }
}
