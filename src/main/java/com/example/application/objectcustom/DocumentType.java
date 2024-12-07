package com.example.application.objectcustom;

import java.util.List;

public class DocumentType {
    private String displayName;
    private String returnValue;

    public DocumentType(String displayName, String returnValue) {
        this.displayName = displayName;
        this.returnValue = returnValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getReturnValue() {
        return returnValue;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static List getItemDocTypes() {
        return List.of(
                new DocumentType("livre", "book"),
                new DocumentType("magazine", "magazine"),
                new DocumentType("jeu", "board_game")
        );
    }
}
