module Chess {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    exports pl.bartlomiejstepien.chess;
    exports pl.bartlomiejstepien.chess.piece;

    opens icons;
}