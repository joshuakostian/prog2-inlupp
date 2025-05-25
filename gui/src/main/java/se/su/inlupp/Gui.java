package se.su.inlupp;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.*;
import java.io.File;
import java.util.Optional;
import java.util.Set;

public class Gui extends Application {

  Stage stage;
  Boolean hasClickedNewPlace = false;
  Pane imageContainer = new Pane();
  Graph<Location> graph = new ListGraph<Location>();

  public void start(Stage stage) {
    this.stage = stage;

    //Initialize JavaFX
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
    // findPath.setDisable(true);
    // showConn.setDisable(true);
    // newPlace.setDisable(true);
    // newConn.setDisable(true);
    // changeConn.setDisable(true);

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
    imageContainer.setOnMouseClicked(click ->mapContainerClickedHandler(click,newPlace));
    //
    // FILE MENU BEHAVIOR
    //
    newMap.setOnAction(event -> newMapButtonHandler(root));
    newConn.setOnAction(event ->newConnectionButtonHandler());
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
  private void createErrorPopup(AlertType type, String headerText, String contentText) {
    Alert error = new Alert(type);
    error.setHeaderText(headerText);
    error.setContentText(contentText);
    error.showAndWait();
  }
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
  private void newConnectionButtonHandler(){
      Optional<Location[]> test = getTwoMarkedCircles();
      if (test.isEmpty()) {
        createErrorPopup(AlertType.ERROR, "Error", "Two places must be selected");
        return;
      }
      Location[] compare = test.get();

      // getedgebetween returnar null om det INTE finns. Därför gör vi negation.
      if (graph.getEdgeBetween(compare[0], compare[1]) != null) {
        createErrorPopup(AlertType.ERROR, "Error!", "A connection already exists.");
      } else {
        Location from = compare[0];
        Location to = compare[1];
        String name;
        int weight = 0;

        // skapar dialog som ber om och tar inputs. Sparar i input[0,1]
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Connection");
        dialog.setHeaderText("headerText");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        TextField nameInputField = new TextField();
        grid.add(nameInputField, 1, 0);
        grid.add(new Label("Time:"), 0, 1);
        TextField timeInputField = new TextField();
        grid.add(timeInputField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(
            button -> button == ButtonType.OK ? new String[] { nameInputField.getText(), timeInputField.getText() }
                : null);
        String input[] = dialog.showAndWait().orElse(null);

        // Mest of koden är lite shit här ngl ngl ngl. Men det funkar. Error handlingen
        // är suprisingly okej tho
        if (input[0].trim().isBlank()) {
          // det här är temp, gör GÄRNA OM createErrorPopup();
          createErrorPopup(AlertType.ERROR, "Fel input i namn", "skriv in ett namn.");
          return;
        }
        name = input[0].trim();
        try {
          weight = Integer.parseInt(input[1].trim());
        } catch (Exception e) {
          createErrorPopup(AlertType.ERROR, "Fel input i tid", "Skriv in ett nummer som tid.");
          return;
        }
        if (weight <= 0) {
          createErrorPopup(AlertType.ERROR, "Fel input i tid", "Tiden får inte vara 0 eller negativt.");
          return;
        }
        graph.connect(from, to, name, weight);
        System.out.println(graph.toString()); // debug
        DrawEdge(from, to, imageContainer); 
        //Den här är inte kopplad till något. Man kan inte ta bort en connection. Den bara ritar.
        //Kanske lägga till funktionalitet på edge???
      }

    
  }
  private void newMapButtonHandler(BorderPane root){
    File file = getFile();
      // System.out.println(file.toURI().toString());
      Image image = new Image(file.toURI().toString());
      ImageView imageView = new ImageView(image);

      imageContainer.getChildren().add(imageView);
      imageContainer.setMaxSize(image.getWidth(), image.getHeight());
      imageContainer.setMinSize(image.getWidth(), image.getHeight());

      root.setCenter(imageContainer);

      stage.sizeToScene();
  }
  private void mapContainerClickedHandler(MouseEvent click, Button newPlace){
    if (!hasClickedNewPlace)
        return;

      var placeName = getPlaceName();

      hasClickedNewPlace = false;
      newPlace.setDisable(false);

      if (placeName.isEmpty())
        return;

      String name = placeName.get();

      Location loc = new Location(name, click.getX(), click.getY());

      // check if it exists already
      int boolInt = 0;
      for (Location l : graph.getNodes()) {
        if (l.equals(loc)) {
          boolInt = 1;
          createErrorPopup(AlertType.ERROR, "Error!", "Node Already Exists");
          break;
        }
      }
      if (boolInt == 0) {
        loc.setLayoutX(loc.getX());
        loc.setLayoutY(loc.getY());
        graph.add(loc);
        imageContainer.getChildren().add(loc);
      }

      // debug
      Set<Location> test = graph.getNodes();
      System.out.println(test.toString());
  }
  // Returnar Empty om det är mindre än två.
  // Returnar objekten i en array [0,1] om det finns två.
  public Optional<Location[]> getTwoMarkedCircles() {
    Location[] returnArray = new Location[2];
    int counter = 0;

    for (Location l : graph.getNodes()) {
      if (l.getClickCounter() % 2 == 1) {
        if (counter < 2) {
          returnArray[counter] = l;
          counter++;
        } else {
          // More than two, cancel.
          return Optional.empty();
        }
      }
    }
    // return if two were found.
    return (counter == 2) ? Optional.of(returnArray) : Optional.empty();
  }
  public void DrawEdge(Location loc1, Location loc2, Pane containerToAddTo) {
    Line line = new Line(loc1.getX(), loc1.getY(), loc2.getX(), loc2.getY());
    line.setStrokeWidth(5);
    containerToAddTo.getChildren().add(line);
  }
  public static void main(String[] args) {
    launch(args);
  }
  private class Location extends Pane {
    private Circle circle;
    private Label label;
    private double x;
    private double y;
    private final int size = 10;
    private int clickCounter = 0;

    public Location(String name, double x, double y) {
      this.x = x;
      this.y = y;

      this.setPrefSize(size, size);
      this.setMinSize(size, size);
      this.setMaxSize(size, size);

      this.circle = new Circle(size, Color.CRIMSON);

      this.label = new Label(name);
      this.label.setFont(Font.font("System", FontWeight.BOLD, 14));
      this.label.setTextFill(Color.BLACK);
      this.label.setLayoutX(-20);
      this.label.setLayoutY(-30);
      this.label.setEffect(new DropShadow(2, Color.WHITESMOKE));

      circle.setOnMouseClicked(click -> {
        boolean isMarked = clickCounter % 2 == 1;

        if (!isMarked && getTwoMarkedCircles().isPresent()) {
          // Redan två markerade och denna är omarkerad → gör inget
          return;
        }

        clickCounter++;

        if (clickCounter % 2 == 1) {
          circle.setFill(Color.BLUE);
        } else {
          circle.setFill(Color.CRIMSON);
        }
      });

      getChildren().addAll(circle, label);
    }

    @Override
    // Två Locations är "samma" om label.getText(); är samma. Ignore case.
    public boolean equals(Object o) {
      if (this == o)
        return true; // samma objekt
      if (o == null || getClass() != o.getClass())
        return false;
      Location other = (Location) o;

      if (this.label.getText().equalsIgnoreCase(other.label.getText())) {
        return true;
      }
      return false;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    public int getClickCounter() {
      return clickCounter;
    }

    @Override
    public String toString() {
      return "\n" + label.getText() + ":" + x + ":" + y;
    }
  }

}
