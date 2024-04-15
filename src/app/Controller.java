/**
 * File: Controller.java
 * Author: Kevin Tran
 * Created on: March, 13, 2024
 * Last Modified: April, 04, 2024
 * Description: Controller class of Currency Converter App
 */
package app;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.util.ArrayList;

public class Controller {
    private String currencyOne, currencyTwo, apiKey;

    @FXML
    private ImageView logo;

    @FXML
    private TextField enterAmountField;

    @FXML
    private ComboBox<String> currencyBoxOne, currencyBoxTwo;

    @FXML
    private Label resultLabel;

    private OkHttpClient client = new OkHttpClient().newBuilder().build();


    public void initialize() {
        // Retrieve API key from text file
        getApiKey();
        System.out.println(apiKey);

        // Load Logo from Path
        loadLogo();

        // Retrieving list of currencies through API
        Task<ArrayList<String>> loadCurrenciesTask = new Task<>() {
            @Override
            protected ArrayList<String> call() throws Exception {
                return loadCurrencyList();
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                ObservableList<String> currencies = FXCollections.observableArrayList(getValue());
                currencyBoxOne.setItems(currencies);
                currencyBoxTwo.setItems(currencies);
            }

            @Override
            protected void failed() {
                super.failed();
                System.out.println("Failed to load currencies: " + getException());
            }
        };

        Thread thread = new Thread(loadCurrenciesTask);
        thread.setDaemon(true);
        thread.start();

    }

    public void setCurrencyOne() {
        currencyOne = currencyBoxOne.getValue();
    }

    public void setCurrencyTwo() {
        currencyTwo = currencyBoxTwo.getValue();
    }

    private void getApiKey() {
        // Use try-with-resources to ensure the InputStream and BufferedReader are closed automatically
        try (InputStream inputStream = getClass().getResourceAsStream("/resources/apiKey.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            apiKey = reader.readLine();
            if (apiKey == null) {
                System.out.println("API key file is empty or not found");
            }
        } catch (IOException e) {
            System.out.println("Error reading API key: " + e.getMessage());
        }
    }


    private void loadLogo() {
        // Use getResourceAsStream to load the logo as an InputStream
        InputStream logoStream = getClass().getResourceAsStream("/resources/logo.png");
        if (logoStream != null) {
            // Create an Image from the InputStream
            logo.setImage(new Image(logoStream));
        } else {
            System.out.println("Logo file not found");
        }
    }

    private ArrayList<String> loadCurrencyList() throws IOException {
        // Retrieve list of currency through API Call
        Request request = new Request.Builder()
                .url("https://api.apilayer.com/currency_data/list")
                .addHeader("apikey", apiKey)
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();

        // Parsing the JSON response to extract the currency list
        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(response.body().charStream(), JsonElement.class);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        ArrayList<String> currencyList = new ArrayList<>();

        for (String currency : jsonObject.getAsJsonObject("currencies").keySet()) {
            currencyList.add(currency);
        }
        return currencyList;
    }

    public void convertCurrency() {
        // Ensure the input field is not empty or null
        if (enterAmountField.getText() == null || enterAmountField.getText().trim().isEmpty()) {
            resultLabel.setText("Please enter an amount to convert.");
            return;
        }

        if (currencyOne == null || currencyTwo == null) {
            resultLabel.setText("Please select both currencies.");
            return;
        }

        try {
            float conversionRate = getConversionRate();
            // Attempt to parse the user input as a float
            float enteredAmount = Float.parseFloat(enterAmountField.getText().trim());
            // Perform the conversion
            float conversionResult = enteredAmount * conversionRate;
            // Update the UI with the conversion result
            resultLabel.setText(String.format("%.2f %s", conversionResult, currencyTwo));
        } catch (NumberFormatException e) {
            // Handle parsing errors
            resultLabel.setText("Invalid amount entered. Please enter a valid number.");
        } catch (IOException e) {
            // Handle errors from the conversion rate fetching
            resultLabel.setText("Failed to fetch conversion rate.");
            e.printStackTrace(); // Consider logging this error as well
        }
    }


    private float getConversionRate() throws IOException {

        Request request = new Request.Builder()
                .url("https://api.apilayer.com/currency_data/live?source=" + currencyOne + "&currencies=" +
                        currencyTwo)
                .addHeader("apikey", apiKey)
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();

        // Parsing the JSON response to extract the conversion rate
        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(response.body().charStream(), JsonElement.class);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // Return the Conversion Rate
        String key = currencyOne + currencyTwo;

        return Float.parseFloat(jsonObject.getAsJsonObject("quotes").get(key).getAsString());
    }
}