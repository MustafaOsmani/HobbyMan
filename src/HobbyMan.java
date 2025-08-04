import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import java.awt.Font;
import java.awt.Color;

import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

public class HobbyMan {
    private JPanel timerPanel;
    private JPanel statsPanel;

    //window set size
    int boardWidth =950;
    int boardHeight = 600;

    //colors used
    Color customBabyBlue = new Color(137,207,240);
    Color customLightGray = new Color(212,212,210);
    Color customOliveGreen = new Color(99,107,47);
    Color customBrown = new Color(137,81,41);

    //UI Components
    JFrame frame = new JFrame("Hobby Man");
    //time variables
    int seconds = 0;
    int minutes = 0;
    int hours = 0;
    boolean running = false;

    //displays live timer
    JLabel displayLabel = new JLabel("HobbyMan");
    JPanel displayPanel = new JPanel();
    //Timer import to count every second and increment the counters
    Timer timer;
    //time at the start of every session, uses long because saved logs will add up
    long sessionStartTime;

    //buttons created for controlling timer
    JButton startButton = new JButton("Start");
    JButton stopButton = new JButton("Stop");
    JButton resetButton = new JButton("Reset");
    JButton statsButton = new JButton("Stats");

    //ComboBox for selecting activity-type before starting timed session
    JPanel comboPanel = new JPanel();
    String[] activityType = {"Watching", "Listening", "Reading", "Writing"};
    JComboBox<String> activityTypeCombo = new JComboBox<>(activityType);

    //session is logged and stored in LogEntry
    java.util.List<LogEntry> activityLog = new java.util.ArrayList<>();

    //CardLayout is used to switch between a timer and stat charts panel
    CardLayout cardLayout = new CardLayout();
    JPanel cardPanel = new JPanel(cardLayout);


    // Saves list of activities to the file called logs.json in a clean format
    private void saveLogsToJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // Opens a file called logs.json and converts activityLog to a JSON file
        try (FileWriter writer = new FileWriter("logs.json")) {
            gson.toJson(activityLog, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Checks if there is a previous session saved to continue progress. If not, it will start a new session.
    public void loadLogsFromJson() {
        Gson gson = new Gson();
        // Attempts to read a logs.json file and adds to it if found
        try (Reader reader = new FileReader("logs.json")) {
            // Deserializes JSON file to make it an array of LogEntry objects
            LogEntry[] logs = gson.fromJson(reader, LogEntry[].class);
            // If the file wasn't empty, adds to activity logs
            if (logs != null) {
                activityLog.addAll(Arrays.asList(logs));
                System.out.println("Loaded " + logs.length + " logs from JSON.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("No previous log found, starting a new one!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Creates a map to store activities with their total times
    private Map<String, Long> getTotalTimePerActivity() {
        Map<String, Long> totals = new HashMap<>();
        // loops through every session in activityLogs
        for (LogEntry entry : activityLog) {
            // Receives the current total from the map, then adds the total. Long 0 if there's no entry.
            totals.put(entry.getActivity(),
                    totals.getOrDefault(entry.getActivity(), 0L) + entry.getDuration());
        }
        return totals;
    }

    // Simplifies with streams. It takes the duration from every LogEntry and adds all of them to get a total
    private long getTotalTimeAllActivities() {
        // Stream processes the activityLog in a more concise way and avoids manual loops
        return activityLog.stream()
                // Takes each LogEntry and gets the duration of each, which returns a long to add up the total duration
                .mapToLong(LogEntry::getDuration)
                .sum();
    }

    //Custom design for the stat charts from FlatLaf
    public void applyCuteChartTheme() {
        StandardChartTheme theme = new StandardChartTheme("HobbyCute");

        theme.setPlotBackgroundPaint(customBabyBlue);
        theme.setChartBackgroundPaint(customBabyBlue);
        theme.setGridBandPaint(customLightGray);
        theme.setGridBandAlternatePaint(customOliveGreen);

        // custom font setup
        Font titleFont = new Font("Baloo 2", Font.BOLD, 18);
        Font axisFont = new Font("Baloo 2", Font.PLAIN, 14);
        Font tickFont = new Font("Baloo 2", Font.PLAIN, 12);
        // title
        theme.setExtraLargeFont(titleFont);
        // axis labels
        theme.setLargeFont(axisFont);
        // tick labels
        theme.setRegularFont(tickFont);
        //symbol
        theme.setSmallFont(tickFont);

        // Applying the theme
        ChartFactory.setChartTheme(theme);
    }

    //generates a bar chart for the stat page
    private void showStatsChart() {
        //alters design from the default look
        applyCuteChartTheme();
        statsPanel.removeAll();

        Map<String, Long> activityTotals = getTotalTimePerActivity();
        long grandTotal = getTotalTimeAllActivities();

        //stores data from activity logs
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Long> entry : activityTotals.entrySet()) {
            dataset.addValue(entry.getValue() / 60, "Minutes", entry.getKey());
        }

        //creates the chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Study Time per Activity",
                "Activity",
                "Time (minutes)",
                dataset

        );
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        renderer.setSeriesPaint(0, customOliveGreen);
        // Removes default shadows
        renderer.setDefaultShadowsVisible(false);
        renderer.setBarPainter(new StandardBarPainter());


        ChartPanel chartPanel = new ChartPanel(chart);
        JLabel totalLabel = new JLabel("Total time: " + (grandTotal / 60) + " minutes", SwingConstants.CENTER);

        // button created to return to the timer
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "TIMER"));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        bottomPanel.add(backButton, BorderLayout.SOUTH);

        statsPanel.add(chartPanel, BorderLayout.CENTER);
        statsPanel.add(bottomPanel, BorderLayout.SOUTH);
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    //private void showAnimation


    HobbyMan(){
        loadLogsFromJson();

        System.out.println("Loaded logs:");
        for (LogEntry entry : activityLog) {
            System.out.println(entry);
        }
        //Makes the window appear
        frame.setVisible(true);
        // Sets the dimensions
        frame.setSize(boardWidth, boardHeight);
        // Centers the window
        frame.setLocationRelativeTo(null);
        // Prevents user from resizing the window (temporary)
        frame.setResizable(false);
        // X button will close the application
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Divides into north, south, east, west
        frame.setLayout(new BorderLayout());

        //creates two separate panels, timer and stats, so buttons and labels can be placed in it
        timerPanel = new JPanel(new BorderLayout());
        statsPanel = new JPanel(new BorderLayout());

        // Design for drop down box
        activityTypeCombo.setBackground(customLightGray);
        activityTypeCombo.setForeground(customBrown);
        activityTypeCombo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        activityTypeCombo.setFocusable(false);

        //display of text and color for application
        displayLabel.setFont(new Font("Baloo 2", Font.BOLD, 60));
        displayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        displayLabel.setOpaque(true);
        displayLabel.setForeground(customOliveGreen);
        displayLabel.setBackground(customBabyBlue);

        displayLabel.setOpaque(true);
        // Goes from top to bottom
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.setBackground(customBabyBlue);

        JPanel labelPanel = new JPanel();
        labelPanel.setBackground(customBabyBlue);
        labelPanel.add(displayLabel);

        comboPanel.setBackground(customBabyBlue);
        comboPanel.add(activityTypeCombo);

        displayPanel.add(labelPanel);
        displayPanel.add(comboPanel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(customLightGray);

        //Shorten this to a loop
        startButton.setBackground(customOliveGreen);
        stopButton.setBackground(customOliveGreen);
        resetButton.setBackground(customOliveGreen);

        startButton.setForeground(Color.WHITE);
        stopButton.setForeground(Color.WHITE);
        resetButton.setForeground(Color.WHITE);
        statsButton.setBackground(Color.WHITE);

        startButton.setFocusPainted(false);
        stopButton.setFocusPainted(false);
        resetButton.setFocusPainted(false);
        statsButton.setFocusPainted(false);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(statsButton);

        // Label and ComboBox is in the center and buttons are on the bottom
        timerPanel.add(displayPanel, BorderLayout.CENTER);
        timerPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Timer ticks every 1000ms
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                seconds++;
                if (seconds == 60) {
                    seconds = 0;
                    minutes++;
                }
                if (minutes == 60) {
                    minutes = 0;
                    hours++;
                }
                updateDisplay();
            }
        });

        // When pressed, the timer will begin and running will be activated
        startButton.addActionListener(e -> {
            if (!running) {
                timer.start();
                sessionStartTime = System.currentTimeMillis();
                running = true;
                comboPanel.setVisible(false);
            }
        });

        // Checks if timer is running, then stops it. Logs the session's activity and duration and save.
        stopButton.addActionListener(e -> {
            if (running){
                timer.stop();

                running = false;
                comboPanel.setVisible(true);
                long sessionEndTime = System.currentTimeMillis();
                long duration = (sessionEndTime - sessionStartTime) / 1000;

                String selectedActivity = (String) activityTypeCombo.getSelectedItem();
                activityLog.add(new LogEntry(selectedActivity, duration));
                System.out.println("Session logged: " + activityLog.get(activityLog.size() - 1));
                saveLogsToJson();
            }
        });

        // Stops timer and goes back to 0
        resetButton.addActionListener(e -> {
            timer.stop();
            running = false;
            seconds = 0;
            minutes = 0;
            hours = 0;
            updateDisplay();
        });

        // Will display stat chart information. If the timer is running, it will continue.
        statsButton.addActionListener(e -> {
            showStatsChart();
            cardLayout.show(cardPanel, "STATS");
        });

        cardPanel.add(timerPanel, "TIMER");
        cardPanel.add(statsPanel, "STATS");
        frame.add(cardPanel);
        cardLayout.show(cardPanel, "TIMER");
        frame.setVisible(true);
    }
    // Updates displayLabel and will be in the timer format
    private void updateDisplay() {
        String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        displayLabel.setText(time);
    }
}