package se.su.inlupp;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.scene.paint.*;
import java.io.File;
import java.util.Optional;

public class Gui extends Application {

  private class Location extends Pane {
    private Circle circle;
    private TextField label;
    private double x;
    private double y;

    public Location(String name, double x, double y) {
      this.x = x;
      this.y = y;

      this.circle = new Circle(10, Color.CRIMSON);

      this.label = new TextField(name);
      this.label.setLayoutX(15);
      this.label.setLayoutY(15);

      getChildren().addAll(circle, this.label);
      // getChildren().add(circle);
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }
  }

  Stage stage;
  Boolean hasClickedNewPlace = false;
  Pane imageContainer = new Pane();

  public void start(Stage stage) {
    this.stage = stage;
    Graph<String> graph = new ListGraph<String>();

    BorderPane root = new BorderPane();
    Scene scene = new Scene(root);

    //
    // DEFINE FILE MENU
    //
    MenuItem newMap = new MenuItem("New Map");
    MenuItem openMap = new MenuItem("Open");
    MenuItem saveMap = new MenuItem("Save");
    MenuItem saveImage = new MenuItem("Save Image");
    MenuItem exitMap = new MenuItem("Exit");

    Menu menu = new Menu("File");
    menu.getItems().addAll(newMap, openMap, saveMap, saveImage, exitMap);

    MenuBar bar = new MenuBar();
    bar.getMenus().add(menu);

    //
    // DEFINE MAP MENU
    //
    HBox hbox = new HBox();
    hbox.setAlignment(Pos.CENTER);
    hbox.setSpacing(10);

    var findPath = new Button("Find Path");
    var showConn = new Button("Show Connection");
    var newPlace = new Button("New Place");
    var newConn = new Button("New Connection");
    var changeConn = new Button("Change Connection");

    hbox.getChildren().addAll(findPath, showConn, newPlace, newConn, changeConn);

    VBox vbox = new VBox();
    vbox.getChildren().addAll(bar, hbox);

    root.setTop(vbox);

    //
    // IMAGE CONTAINER BEHAVIOR
    //
    imageContainer.setOnMouseEntered(enter -> {
      if (hasClickedNewPlace)
        scene.setCursor(Cursor.CROSSHAIR);
    });
    imageContainer.setOnMouseExited(exit -> {
      scene.setCursor(Cursor.DEFAULT);
    });
    imageContainer.setOnMouseClicked(click -> {
      if (!hasClickedNewPlace)
        return;

      var placeName = getPlaceName();

      hasClickedNewPlace = false;
      newPlace.setDisable(false);

      if (placeName.isEmpty())
        return;

      String name = placeName.get();

      Location loc = new Location(name, click.getX(), click.getY());
      loc.setLayoutX(loc.getX());
      loc.setLayoutY(loc.getY());
      imageContainer.getChildren().add(loc);
      loc.setBorder(new Border(new BorderStroke[10]));

    });

    //
    // FILE MENU BEHAVIOR
    //
    newMap.setOnAction(event -> {
      File file = getFile();
      Image image = new Image(file.toURI().toString());
      ImageView imageView = new ImageView(image);

      imageContainer.getChildren().add(imageView);
      imageContainer.setMaxSize(image.getWidth(), image.getHeight());
      imageContainer.setMinSize(image.getWidth(), image.getHeight());

      root.setCenter(imageContainer);

      stage.sizeToScene();
    });

    //
    // MAP MENU BEHAVIOR
    //
    newPlace.setOnAction(event -> {
      hasClickedNewPlace = true;
      newPlace.setDisable(true);
    });

    stage.setScene(scene);
    stage.show();
  }

  //
  // UTIL
  //
  private File getFile() {
    FileChooser fileChooser = new FileChooser();

    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Gif", "*.gif"),
        new FileChooser.ExtensionFilter("Graph", "*.graph"));

    File selectedFile = fileChooser.showOpenDialog(stage);

    return selectedFile;
  }

  private Optional<String> getPlaceName() {
    TextInputDialog tid = new TextInputDialog();
    tid.setTitle("Name");
    tid.setHeaderText("Name of place: ");

    Button okButton = (Button) tid.getDialogPane().lookupButton(ButtonType.OK);

    TextField input = tid.getEditor();

    okButton.setDisable(true);

    input.textProperty().addListener((obs, oldText, newText) -> {
      okButton.setDisable(newText.trim().isEmpty());
    });

    return tid.showAndWait();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
