import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UserInterface {
    // creates a new thread pool to refresh the site at scheduled times
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int count = 0;

    // information input fields
    @FXML
    private TextField name = null;
    @FXML
    private TextField email = null;
    @FXML
    private PasswordField password = null;
    @FXML
    private TextField link = null;

    public TextArea console;

    /**
     * Links to app password on gmail help site
     * @param mouseEvent - "?" button clicked
     */
    public void onClickHelpEvent(MouseEvent mouseEvent) throws URISyntaxException {
        try {
            Desktop.getDesktop().browse(new URL("https://support.google.com/accounts/answer/185833?hl=en").toURI());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Runs the program when button is clicked
     * @param mouseEvent - "Run Program" button clicked
     */
    public void onClickRunEvent(MouseEvent mouseEvent) throws IOException, NullPointerException {

        // new object
        WaitlistNotifier course = new WaitlistNotifier();

        // checks if inputs are empty
        if (name.getText().isBlank() || email.getText().isBlank() || password.getText().isBlank() || link.getText().isBlank()) {
            console.appendText("Error: Missing information in the input field. Please fill out all fields and try again.\n");
        }
        else {
            // gets information from input text -- sets in WaitlistNotifier.java
            course.setEmail(email.getText());
            course.setPassword(password.getText());
            course.setLink(link.getText());

            // welcome
            console.appendText("Welcome, " + name.getText() + "!\n");
            console.appendText(course.getCourseTitle() + ":\n");

            // creates a new thread to run every 15 min.
            final Runnable refresher = () -> {
                console.appendText("-------------------REFRESH_" + count + "------------------------\n");
                count++;

                try {
                    // if remaining seats > 0, send email to notify user
                    if (course.getRemainingSeats() > 0) {
                        course.sendEmail(course.getEmail(), course.getCourseTitle(), course.getLink(), course.getPassword());
                        console.appendText("Email Sent! Seat Available in " + course.getCourseTitle() + "!\n");
                        console.appendText("Program ending...\n");
                        scheduler.shutdown();
                    } else {
                        console.appendText("No seats available... keep program running.\n");
                    }

                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            };

            final ScheduledFuture<?> refreshHandle = scheduler.scheduleAtFixedRate(refresher, 0, 15, TimeUnit.MINUTES);

            /*
            scheduler.schedule(() -> {
                refreshHandle.cancel(true);
            }, 12, TimeUnit.HOURS);*/
        }
    }
}
