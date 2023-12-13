package cs1302.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import java.util.List;
import java.util.ArrayList;
import com.google.gson.JsonSyntaxException;

/**
 * Gets US zip code from user, and converts into latitude and longitude coordinates.
 Using those coordinates, an API ueses them to find Sunset and Sunrise times for than location.
*/
public class ApiApp extends Application {


    private Stage stage;
    private Scene scene;
    private VBox root;
    private Label resultLabel;
    private TextField zipInput;
    private Label sunriseLabel;
    private Label sunsetLabel;


    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox(10);
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Label zipLabel = new Label("Enter US Zip Code");
        Label descriptionLabel = new Label("Welcome to the Sunrise and Sunset Time App!\n"
                                           + "Enter your US zip code and click 'Get Sun Stats'.\n"
                                           + "The app will get the sunrise and sunset times.");
        descriptionLabel.setWrapText(true);
        Label exampleLabel = new Label("Example: Zip Code: 30041");
        exampleLabel.setStyle("-fx-font-style: italic;");
        zipInput = new TextField();
        zipInput.setPromptText("Zip Code");
        Label sunriseLabel = new Label("Sunrise Time: ");
        Label sunsetLabel = new Label("Sunset Time: ");
        Button getSunButton = new Button("Get Sun Stats");
        getSunButton.setOnAction(event -> {
            String zipCodeFromInput = zipInput.getText();
            try {
                String firstApiEndpoint = "https://api.zippopotam.us/us/" + zipCodeFromInput;
                String jsonResponse = makeApiCall(firstApiEndpoint);
                System.out.println("First API Response: " + jsonResponse);
                PlaceInfo placeInfo = new Gson().fromJson(jsonResponse, PlaceInfo.class);
                if (placeInfo != null && placeInfo.getPlaces()
                    != null && !placeInfo.getPlaces().isEmpty()) {
                    Place place = placeInfo.getPlaces().get(0);
                    double latitude = Double.parseDouble(place.getLatitude());
                    double longitude = Double.parseDouble(place.getLongitude());
                    String secondApiEndpoint = "https://api.sunrise-sunset.org/json?lat=" + latitude
                        + "&lng=" + longitude;
                    String sunriseSunsetResponse = makeApiCall(secondApiEndpoint);
                    System.out.println("Second API Response: " + sunriseSunsetResponse);
                    SunriseSunsetInfo sunriseSunsetInfo = new Gson().
                        fromJson(sunriseSunsetResponse, SunriseSunsetInfo.class);
                    if (sunriseSunsetInfo != null && sunriseSunsetInfo.getResults() != null) {
                        String sunriseTime = sunriseSunsetInfo.getResults().getSunrise();
                        String sunsetTime = sunriseSunsetInfo.getResults().getSunset();
                        System.out.println("Sunrise Time: " + sunriseTime);
                        System.out.println("Sunset Time: " + sunsetTime);
                        // Display sunrise and sunset times in labels
                        sunriseLabel.setText("Sunrise Time: " + sunriseTime);
                        sunsetLabel.setText("Sunset Time: " + sunsetTime);
                    } else {
                        System.out.println("Invalid response from second API");
                    }
                } else {
                    System.out.println("Invalid response from first API");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        root.getChildren().addAll(descriptionLabel,
                                  exampleLabel, zipInput, getSunButton, sunriseLabel, sunsetLabel);
        scene = new Scene(root);
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
    }

    /**
     * Files API request.
     * @return the API response when called.
     * @param apiEndpoint is a api end point.
     */
    private String makeApiCall(String apiEndpoint) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiEndpoint))
            .build();

        HttpResponse<String> response = httpClient.send(request,
                                                        HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Recieves API info from ZIP Code.
     */
    static class PlaceInfo {
        private String postCode;
        private String country;
        private String countryAbbreviation;
        private java.util.List<Place> places;

        /**
         * Simple return method to get post code.
         * @return post code when method is called.
         */
        public String getPostCode() {
            return postCode;
        }

        /**
         * Returns country.
         * @return the country when called.
         */
        public String getCountry() {
            return country;
        }

        /**
         * Returns Country abbreviation.
         * @return the country abbreviation when called.
         */
        public String getCountryAbbreviation() {
            return countryAbbreviation;
        }

        /**
         * Returns Places.
         * @return the places when called.
         */
        public java.util.List<Place> getPlaces() {
            return places;
        }
    }

    /**
     * Recieves more place info such as state from ZIP Code.
     */
    static class Place {
        private String placeName;
        private String longitude;
        private String state;
        private String stateAbbreviation;
        private String latitude;

        /**
         * Returns Place name.
         * @return the place name when called.
         */
        public String getPlaceName() {
            return placeName;
        }

        /**
         * Returns longitude.
         * @return the longitude when called.
         */
        public String getLongitude() {
            return longitude;
        }

        /**
         * Returns state.
         * @return the state when called.
         */
        public String getState() {
            return state;
        }

        /**
         * Returns state abbreviation.
         * @return the state abbreviationwhen called.
         */
        public String getStateAbbreviation() {
            return stateAbbreviation;
        }

        /**
         * Returns Postcode.
         * @return the latitude  when called.
         */
        public String getLatitude() {
            return latitude;
        }
    }

    /**
     * Holds Sunrise and Sunset Info.
     */
    static class SunriseSunsetInfo {
        private Results results;

        /**
         * Returns results.
         * @return results when called.
         */
        public Results getResults() {
            return results;
        }
    }

    /**
     * Holds sunrise and sunset variables.
     */
    static class Results {
        private String sunrise;
        private String sunset;

        /**
         * Returns sunrise.
         * @return sunrise when called.
         */
        public String getSunrise() {
            return sunrise;
        }

        /**
         * Returns sunset.
         * @return sunset
         */
        public String getSunset() {
            return sunset;
        }
    }


} // ApiApp
