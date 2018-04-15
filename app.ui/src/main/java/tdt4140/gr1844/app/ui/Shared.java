package tdt4140.gr1844.app.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;
import tdt4140.gr1844.app.client.WebCalls;

public class Shared {
    private Button deletePatientButton;
    private TextField deletePatientID;
    private Button buttonName;

    // Active user panel
    @FXML
    private Label activePatientNameLabel;
    @FXML
    private Label lastRatingLabel;
    @FXML
    private Label ratingAvgLabel;
    @FXML
    private Label activePatientIDLabel;
    @FXML
    private TextField patientName;
    @FXML
    private TextField patientEmail;
    @FXML
    private TextField patientPassword;


    // Left sidebar
    @FXML
    private VBox patientListBox;

    // Right sidebar
    @FXML
    private TextArea feedbackTextField;


    private Main main = new Main();

    void setInfo(Button deletePatientButton, TextField deletePatientID, Label activePatientNameLabel, Label lastRatingLabel, Label ratingAvgLabel, Label activePatientIDLabel, TextField patientName, TextField patientEmail, TextField patientPassword, VBox patientListBox, TextArea feedbackTextField){
        this.deletePatientButton = deletePatientButton;
        this.deletePatientID = deletePatientID;
        this.activePatientNameLabel = activePatientNameLabel;
        this.lastRatingLabel = lastRatingLabel;
        this.ratingAvgLabel = ratingAvgLabel;
        this.activePatientIDLabel = activePatientIDLabel;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPassword = patientPassword;
        this.patientListBox = patientListBox;
        this.feedbackTextField = feedbackTextField;
    }

    void updatePatientList(JSONArray patients) throws Exception {
        listPatients(patients);
    }



    private void listPatients(JSONArray patients) throws Exception {
        patientListBox.getChildren().clear();
        for(Object patient : patients){
            Button btnNumber = createPatient((JSONObject) patient);
            patientListBox.getChildren().add(btnNumber);
        }
    }

    private Button createPatient(JSONObject patient) throws Exception {
        final Button button = new Button(patient.getString("name"));
        JSONArray feelings = WebCalls.sendGET(
                "action=listFeelings" +
                        "&patientID=" + patient.getInt("id") +
                        "&orderBy=desc" +
                        "&cookie=" + main.getCookie()
        ).getJSONArray("feelings");

        int lastRating = 0;
        int ratingAvg = 0;
        if (feelings.length() != 0) {
            ratingAvg = getAverageRating(feelings);
            lastRating = feelings.getJSONObject(0).getInt("rating");
            if (ratingAvg < 2) {
                button.setId("unhealthyRating");
            } else if(ratingAvg < 3.5) {
                button.setId("averageHealthRating");
            } else {
                button.setId("healthyRating");
            }

        }
        button.setPrefSize(200, 20);
        int finalRatingAvg = ratingAvg;
        int finalLastRating = lastRating;
        button.setOnMouseClicked(event -> {
            buttonName = button;
            updateActivePatient(
                    patient.getString("name"),
                    patient.getInt("id"),
                    finalLastRating,
                    finalRatingAvg
            );
        });
        return button;
    }

    private int getAverageRating(JSONArray feelings) {
        int sum = 0;
        for (Object feelingObject : feelings) {
            JSONObject feeling = (JSONObject) feelingObject;
            sum += feeling.getInt("rating");
        }
        return sum/feelings.length();
    }

    private void updateActivePatient(String patientName, int patientID, int lastRating, int ratingAvg){
        activePatientNameLabel.setText("Patient's name: " + patientName);
        activePatientIDLabel.setText("Patients's ID: " + patientID);
        String finalLastRating = lastRating == 0 ? "no ratings yet" : Integer.toString(lastRating);
        String finalRatingAvg = ratingAvg== 0 ? "no ratings yet" : Integer.toString(ratingAvg);
        lastRatingLabel.setText("Last rating: " + finalLastRating);
        ratingAvgLabel.setText("Rating average: " + finalRatingAvg);
    }

    void registerPatient() throws Exception {
        String userEmail = patientEmail.getText();
        String userName = patientName.getText();
        String userPassword = patientPassword.getText();

        JSONObject response = main.createPatient(
                userName,
                userEmail,
                userPassword,
                main.getUserID()
        );
        //TODO Lag at det kommer en alert om det var sukssess eller ikke
        if (response.getString("status").equals("OK")) {
            patientEmail.clear();
            patientName.clear();
            patientPassword.clear();
        } else {
            System.out.println(response.getString("message"));
        }
    }


    void sendFeedback() throws Exception {
        JSONObject response = main.sendFeedback(feedbackTextField.getText());
        if (response.getString("status").equals("OK")) {
            System.out.println("Feedback sent");
            feedbackTextField.clear();
        } else {
            System.out.println(response.getString("message"));
        }
    }

    void setPatientListBox(VBox patientListBox) {
        this.patientListBox = patientListBox;
    }
}
