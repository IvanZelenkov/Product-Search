package project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductSearchController implements Initializable {

    @FXML
    private TableView<ProductSearchModel> productTableView;

    @FXML
    private TableColumn<ProductSearchModel, Integer> productIDTableColumn;

    @FXML
    private TableColumn<ProductSearchModel, String> brandTableColumn;

    @FXML
    private TableColumn<ProductSearchModel, String> modelNumberTableColumn;

    @FXML
    private TableColumn<ProductSearchModel, Integer> modelYearTableColumn;

    @FXML
    private TableColumn<ProductSearchModel, String> productNameTableColumn;

    @FXML
    private TableColumn<ProductSearchModel, String> descriptionTableColumn;

    @FXML
    private TextField keywordTextField;


    ObservableList<ProductSearchModel> productSearchModelObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resource) {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getDBConnection();

        // SQL Query - Executed in the backend database
        String productViewQuery = "SELECT * FROM ProductTable";

        try {
            Statement statement = connectDB.createStatement();
            ResultSet queryOutput = statement.executeQuery(productViewQuery);

            while(queryOutput.next()) {
                Integer queryProductID = queryOutput.getInt("ProductId");
                String queryBrand = queryOutput.getString("Brand");
                String queryModelNumber = queryOutput.getString("ModelNumber");
                Integer queryModelYear = queryOutput.getInt("ModelYear");
                String queryProductName = queryOutput.getString("ProductName");
                String queryDescription = queryOutput.getString("Description");

                // Populate the observable list
                productSearchModelObservableList.add(new ProductSearchModel(queryProductID, queryBrand, queryModelNumber, queryModelYear, queryProductName, queryDescription));
            }

            // PropertyValueFactory corresponds to the new ProductSearchModel fields
            // The table column is the one you annotate above
            productIDTableColumn.setCellValueFactory(new PropertyValueFactory<>("productID"));
            brandTableColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
            modelNumberTableColumn.setCellValueFactory(new PropertyValueFactory<>("modelNumber"));
            modelYearTableColumn.setCellValueFactory(new PropertyValueFactory<>("modelYear"));
            productNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
            descriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            productTableView.setItems(productSearchModelObservableList);

            FilteredList<ProductSearchModel> filteredData = new FilteredList<>(productSearchModelObservableList, b -> true);

            keywordTextField.textProperty().addListener((observable, oldValue, newValue) ->
                    filteredData.setPredicate(productSearchModel -> {

                // if no search value, then display all records or whatever records it currently have, no changes
                if (newValue.isEmpty() || newValue.isBlank() || newValue == null) {
                    return true;
                }

                String searchKeyword = newValue.toLowerCase();

                if (productSearchModel.getProductName().toLowerCase().contains(searchKeyword)) {
                    return true; // means that it found a match in ProductName
                } else if (productSearchModel.getDescription().toLowerCase().contains(searchKeyword)) {
                    return true; // means that it found a match in Description
                } else if (productSearchModel.getBrand().toLowerCase().contains(searchKeyword)) {
                    return true; // means that it found a match in Brand
                } else if (productSearchModel.getModelNumber().toLowerCase().contains(searchKeyword)) {
                    return true; // means that it found a match in Description
                } else if (productSearchModel.getModelYear().toString().toLowerCase().contains(searchKeyword)) {
                    return true; // means that it found a match in ModelYear
                } else
                    return false; // no match found
            }));

            SortedList<ProductSearchModel> sortedData = new SortedList<>(filteredData);

            // Bind sorted result with Table View
            sortedData.comparatorProperty().bind(productTableView.comparatorProperty());

            // Apply filtered and sorted data to the Table View
            productTableView.setItems(sortedData);

        } catch (SQLException e) {
            Logger.getLogger(ProductSearchController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
