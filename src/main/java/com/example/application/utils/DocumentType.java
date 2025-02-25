public enum DocumentType {
    BOOK("book", "livre"),
    MAGAZINE("magazine", "revue"),
    BOARD_GAME("board_game", "jeu");

    private final String code;
    private final String label;

    DocumentType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}