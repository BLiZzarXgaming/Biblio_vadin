package com.example.application.components;

import com.example.application.entity.BoardGame;
import com.example.application.service.implementation.BoardGameServiceImpl;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

public class BoardGamesForm extends VerticalLayout {
    // number_of_pieces
    // recommended_age
    // game_rules
    // gtin
    // item_id
    private BoardGameServiceImpl boardGameService;

    private IntegerField numberOfPiecesField;
    private IntegerField recommendedAgeField;
    private TextField gameRulesField;
    private TextField gtinField;
    private Long item_id;

    private Notification notification;
    private Long itemId;

    private boolean disableNotification = false;

    public BoardGamesForm(BoardGameServiceImpl boardGameService) {
        this.boardGameService = boardGameService;
        item_id = null;

        // Form layout
        FormLayout formLayout = new FormLayout();

        // Number of pieces field
        numberOfPiecesField = new IntegerField("Nombre de pièces");
        formLayout.add(numberOfPiecesField);

        // Recommended age field
        recommendedAgeField = new IntegerField("Âge recommandé");
        formLayout.add(recommendedAgeField);

        // Game rules field
        gameRulesField = new TextField("Règles du jeu");
        formLayout.add(gameRulesField);

        // GTIN field
        gtinField = new TextField("GTIN");
        formLayout.add(gtinField);

        add(formLayout);
    }

    public void saveBoardGame() {
        Integer numberOfPieces = numberOfPiecesField.getValue();
        Integer recommendedAge = recommendedAgeField.getValue();
        String gameRules = gameRulesField.getValue();
        String gtin = gtinField.getValue();

        if (numberOfPieces == null || recommendedAge == null || gameRules.isEmpty() || gtin.isEmpty() || item_id == null) {
            sendNotification("Veuillez remplir tout les champs du jeu de société", "error", 5000);
            return;
        }

        BoardGame boardGame = new BoardGame();
        boardGame.setNumberOfPieces(numberOfPieces);
        boardGame.setRecommendedAge(recommendedAge);
        boardGame.setGameRules(gameRules);
        boardGame.setGtin(gtin);
        boardGame.setItemId(itemId);

        boardGameService.save(boardGame);
        sendNotification("Le jeu de société a été enregistré", "success", 5000);
    }

    public BoardGame searchBoardGame() {
        String gtin = gtinField.getValue();
        BoardGame boardGame = boardGameService.findByGtin(gtin);
        if (boardGame != null) {
            fillFields(boardGame);
            return boardGame;
        } else {
            sendNotification("Jeu de société non trouvé", "error", 5000);
            return null;
        }
    }

    public void setBoardGameItemId(Long itemId) {
        this.itemId = itemId;
    }

    public void setBoardGameByGtin(String gtin) {
        BoardGame boardGame = boardGameService.findByGtin(gtin);
        if (boardGame != null) {
            fillFields(boardGame);
        }
    }

    public void setBoardGameByItemId(Long itemId) {
        BoardGame boardGame = boardGameService.findByItemId(itemId);
        if (boardGame != null) {
            fillFields(boardGame);
        }
    }

    public BoardGame getBoardGame() {
        BoardGame boardGame = new BoardGame();
        boardGame.setNumberOfPieces(numberOfPiecesField.getValue());
        boardGame.setRecommendedAge(recommendedAgeField.getValue());
        boardGame.setGameRules(gameRulesField.getValue());
        boardGame.setGtin(gtinField.getValue());
        boardGame.setItemId(item_id);

        return boardGame;
    }

    private void fillFields(BoardGame boardGame) {
        numberOfPiecesField.setValue(boardGame.getNumberOfPieces());
        recommendedAgeField.setValue(boardGame.getRecommendedAge());
        gameRulesField.setValue(boardGame.getGameRules());
        gtinField.setValue(boardGame.getGtin());
        item_id = boardGame.getItemId();

        setFieldReadOnly(true);
    }

    public void setFieldReadOnly(boolean readOnly) {
        numberOfPiecesField.setReadOnly(readOnly);
        recommendedAgeField.setReadOnly(readOnly);
        gameRulesField.setReadOnly(readOnly);
        gtinField.setReadOnly(readOnly);
    }

    private void sendNotification(String message, String type, int duration) {
        if (type.equals("success")) {
            message = "Succès: " + message;
        } else if (type.equals("error")) {
            message = "Erreur: " + message;
        }

        notification = new Notification(message, duration, Notification.Position.BOTTOM_START);

        if (type.equals("success")) {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else if (type.equals("error")) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        if (!disableNotification || type.equals("error")) {
            notification.open();
        }
    }

    public void setDisableNotification(boolean disableNotification) {
        this.disableNotification = disableNotification;
    }

}
