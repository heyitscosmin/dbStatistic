package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.geometry.*;
import java.sql.*;
import java.util.*;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.util.Callback;


public class Main extends Application {

    public Statement statement;
    Connection conn = null;
    //TABLE VIEW AND DATA
    private ObservableList<ObservableList> data;
    private TableView tableview;


    @Override
    public void start(Stage primaryStage) {
        loginDialog();

        primaryStage.setTitle("Main Window");
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 600, 250, Color.WHITE);
        tableview = new TableView();

        Pane area = new Pane();
        root.setCenter(area);

        root.setCenter(tableview);

        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        root.setTop(menuBar);

        // File menu - exit
        Menu fileMenu = new Menu("_File");
        MenuItem exit = new MenuItem("_Exit");
        exit.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+E")
        );
        exit.setOnAction(actionEvent -> {
                    Platform.exit();
                    System.exit(0);
                }
        );

        fileMenu.getItems().addAll(exit);

        Menu view = new Menu("_View");
        MenuItem alert1 = new MenuItem("Alert1");
        alert1.setOnAction(actionEvent -> loginDialog() );

        MenuItem alert2 = new MenuItem("Alert2");
        alert2.setOnAction(actionEvent -> loginDialog() );

        MenuItem alert3 = new MenuItem("Data");
        alert3.setOnAction(actionEvent ->  buildData(getDatabaseTableName()) );



        view.getItems().addAll(alert1,alert2,alert3);

        menuBar.getMenus().addAll(fileMenu,view);

        primaryStage.setScene(scene);
        primaryStage.show();



    }
    public void loginDialog(){
        // Create the custom dialog
        Dialog<ArrayList<String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Look, a Custom Login Dialog");


        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField dbHostname = new TextField();
        dbHostname.setPromptText("Hostname");
        TextField dbName = new TextField();
        dbName.setPromptText("Database Name");
        TextField dbUsername = new TextField();
        dbUsername.setPromptText("Username");
        PasswordField dbPassword = new PasswordField();
        dbPassword.setPromptText("Password");

        // For faster connectivity
        dbHostname.setText("localhost");
        dbName.setText("classicmodels");
        dbUsername.setText("root");
        dbPassword.setText("");

        grid.add(new Label("Hostname: "), 0, 0);
        grid.add(dbHostname, 1, 0);
        grid.add(new Label("Database name: "), 0, 1);
        grid.add(dbName, 1, 1);
        grid.add(new Label("Username:"), 0, 2);
        grid.add(dbUsername, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        grid.add(dbPassword, 1, 3);

        // Enable/Disable login button depending on whether a username was entered.
        /*Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);*/

        // Do some validation (using the Java 8 lambda syntax).
        /*dbHostname.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });*/

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> dbHostname.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                String a = dbHostname.getText();
                String b = dbName.getText();
                String c = dbUsername.getText();
                String d = dbPassword.getText();
                DatabaseConnect(a,b,c,d);
            }
            return null;
        });

        Optional<ArrayList<String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            System.out.println("Username= " + usernamePassword.get(0) + ", Password= " + usernamePassword.get(1));
        });
    }
    public String getDatabaseTableName() {
        try {

            DatabaseMetaData dbmd = conn.getMetaData();
            String[] types = {"TABLE"};
            ResultSet rs = dbmd.getTables(null, null, "%", types);
            while (rs.next()) {
                System.out.println(rs.getString("TABLE_NAME"));
                String tableName = rs.getString("TABLE_NAME");
                return tableName;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void showAlert(String str) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(str);
        a.showAndWait();
    }
    //CONNECTION DATABASE
    public void DatabaseConnect(String dbHostname, String dbName, String dbUsername, String dbPassword) {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+dbHostname+"/" + dbName, dbUsername, dbPassword);
            statement = conn.createStatement();
            System.out.print("+Database connected\n");
            showAlert("Database Connected");
        } catch (SQLException e) {
            System.out.print("*Couldn't connect to database\n" + e);
            showAlert("Couldn't connect to database\n" + e);
        }
    }
    //SHOW DATA INTO TABLEVIEW

    public void buildData(String tableName) {
        data = FXCollections.observableArrayList();
        try {
            //SQL FOR SELECTING ALL OF CUSTOMER
            String SQL = "SELECT * FROM " +tableName;
            //ResultSet
            ResultSet rs = statement.executeQuery(SQL);

            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                //We are using non property style for making dynamic table
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                tableview.getColumns().addAll(col);
                System.out.println("Column [" + i + "] ");
            }


            while (rs.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }

            //FINALLY ADDED TO TableView
            tableview.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
