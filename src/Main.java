//Mustafa Osmani
/*HobbyMan - Inspired by DuoLingo and Toggle Track, HobbyMan is a timer application that assists with language learning by using Steve Kaufmann's study method,
Input-Based Learning. It is a Java Swing program allows users to time and log their activities, such as watching or reading. After a session, the logs
are saved to a JSON file and displays statistic summaries by using a bar chart with JFreeChart.
*/
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {

        try {
            // Changes look and feel to FlatLightLaf, a cleaner look
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Loads a custom font and registers it so it can be used in the UI
            Font baloo = Font.createFont(Font.TRUETYPE_FONT, new File("Baloo_2/Baloo2-VariableFont_wght.ttf")).deriveFont(16f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baloo);

            // Set globally
            UIManager.put("defaultFont", baloo);
        } catch (Exception e) {
            System.err.println("FlatLaf failed to initialize");
        }
        // Runs on Event Dispatch Thread to create and update Swing components
        SwingUtilities.invokeLater(() -> {
            new HobbyMan();
        });
    }
}