module com.pjs {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;

    requires static lombok;
    requires org.apache.commons.codec;
    requires org.apache.commons.lang3;

    opens com.pjs to javafx.fxml;
    opens com.pjs.ui to javafx.graphics;

    exports com.pjs;
    exports com.pjs.model;
}
