/*
Created by Kiara Melocoton on July 15, 2021
Built for educational purposes only, I am not responsible for individuals' misuse of this program.

SSC Course Waitlist Notifier
============================
Allows user to input a specific course section URL, as well as their name, and email information in order to notify
them of seat availability in the course. The program will keep running until availability has been found, and will
refresh periodically every 15 minutes upon running.

Gmail account is required for the email notifications, and the unique authentication password for apps is used, and
must be first configured by the user through navigating: Gmail -> Account -> Security

 */

import java.io.IOException;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class WaitlistNotifier {
    // user information taken from inputs
    private String email;
    private String password;
    private String link;
    private String courseTitle;

    /**
     * Setters for user information from UI
     */
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setLink(String link) { this.link = link; }

    /**
     * Getters for information to print in UI
     */
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getLink() { return link; }

    /**
     * Gets course title using URL given
     * @return courseTitle
     */
    public String getCourseTitle() throws IOException {
        // gets webpage information using URL
        Document URL = Jsoup.connect(link).get();

        // gets course name
        String pageTitle = URL.title();
        int posOfTitle = pageTitle.indexOf(" -");
        courseTitle = pageTitle.substring(0,posOfTitle);
        return courseTitle;
    }

    /**
     * Gets remaining seats in the course
     * @return remainingSeats
     */
    public int getRemainingSeats() throws IOException {
        // gets webpage information using URL
        Document URL = Jsoup.connect(link).get();

        // creates string using text in the div element
        Elements titles = URL.getElementsByClass("content expand");
        StringBuilder bodyText = new StringBuilder();
        bodyText.append(titles.text());

        // finds number of seats remaining
        String str = "General Seats Remaining: ";
        int length = str.length();
        int posOfCurrent = bodyText.indexOf(" Restricted Seats Remaining*:");
        int posOfSeats = bodyText.indexOf(str);
        int remainingSeats = Integer.parseInt(bodyText.substring(posOfSeats + length, posOfCurrent));
        return remainingSeats;
    }

    /**
     * Sends email with course link when seat is available
     */
    public void sendEmail(String email, String courseName, String link, String password) {
        // sets up mail server
        String host = "smtp.gmail.com"; // sending from localhost
        Properties properties = System.getProperties(); // gets system properties
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // gets session object and passes email + password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email,password);
            }
        });

        //session.setDebug(true); // debugs SMTP issues

        try {
            // sets up the email information
            MimeMessage message = new MimeMessage(session); // creates new default MimeMessage
            message.setFrom(new InternetAddress(email)); // sets FROM
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email)); // sets TO
            message.setSubject("SEAT AVAILABLE IN " + courseName.toUpperCase() + "!"); // sets subject text
            message.setText("Register Now: " + link); // sets message text

            // sends the message
            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
