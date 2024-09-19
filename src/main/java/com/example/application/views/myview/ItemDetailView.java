package com.example.application.views.myview;

import com.example.application.entity.BoardGame;
import com.example.application.entity.Book;
import com.example.application.entity.Item;
import com.example.application.entity.Magazine;
import com.example.application.service.implementation.ItemServiceImpl;
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

@Route(value = "item/:itemID", layout = MainLayout.class)
@PageTitle("Détails de l'Item")
@AnonymousAllowed
public class ItemDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private ItemServiceImpl itemService;
    private Item item;

    public ItemDetailView(ItemServiceImpl itemService) {
        this.itemService = itemService;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            Long itemId = Long.parseLong(parameter);
            item = itemService.findItemById(itemId);

            if (item != null) {
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
        add(new H1(item.getTitle()));
        add(new Paragraph("Type : " + item.getType()));
        add(new Paragraph("Catégorie : " + item.getCategory().getName()));
        add(new Paragraph("Éditeur : " + item.getPublisher().getName()));
        add(new Paragraph("Valeur : " + item.getValue()));

        // Afficher les détails spécifiques en fonction du type
        if ("Livre".equals(item.getType())) {
            Book book = itemService.findBookByItemId(item.getId());
            if (book != null) {
                add(new Paragraph("Auteur : " + book.getAuthor()));
                add(new Paragraph("ISBN : " + book.getIsbn()));
                add(new Paragraph("Date de Publication : " + book.getPublicationDate()));
            }
        } else if ("Revue".equals(item.getType())) {
            Magazine magazine = itemService.findMagazineByItemId(item.getId());
            if (magazine != null) {
                add(new Paragraph("ISNI : " + magazine.getIsni()));
                add(new Paragraph("Mois : " + magazine.getMonth()));
                add(new Paragraph("Date de Publication : " + magazine.getPublicationDate()));
            }
        } else if ("Jeu".equals(item.getType())) {
            BoardGame game = itemService.findBoardGameByItemId(item.getId());
            if (game != null) {
                add(new Paragraph("Nombre de Pièces : " + game.getNumberOfPieces()));
                add(new Paragraph("Âge Recommandé : " + game.getRecommendedAge()));
                add(new Paragraph("Règles du Jeu : " + game.getGameRules()));
            }
        }

        // Bouton pour retourner au catalogue
        Button backButton = new Button("Retour au Catalogue", e -> {
            getUI().ifPresent(ui -> ui.navigate(CatalogueView.class));
        });
        add(backButton);
    }
}
