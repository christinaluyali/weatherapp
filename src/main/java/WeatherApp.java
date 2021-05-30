package main.java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.xml.sax.SAXException;

public class WeatherApp {

	private static String city = "Sydney";
	
    public static void main(String[] args) throws Exception {
    	
    	// Create Swing GUI
		JFrame frame = new JFrame("WeatherApp");
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setSize(500,200);
    	frame.setResizable(false);
    	frame.setLayout(new BorderLayout());
    	
    	JPanel panel = new JPanel();
    	JLabel label = new JLabel("The next 5 days in " + city);
    	Font boldFont = new Font(label.getFont().getName(),Font.BOLD,label.getFont().getSize());
    	label.setFont(boldFont);
    	panel.add(label);
    	
    	JLabel lastUpdatedLabel = new JLabel("Last updated: " + new SimpleDateFormat("dd/MM/YYYY HH:mm:ss").format(new Date()));
    	panel.add(lastUpdatedLabel);

    	// Retrieve weather data
    	WeatherData weatherData = new WeatherData(city);
    	weatherData.updateWeatherData();
    	
    	// Create table for detailed weather data
    	String[][] data = new String[5][3];
		Iterator<String> itr = weatherData.getDates().iterator();
    	for (int i = 0; i < weatherData.getDates().size(); i++) {
    		String date = itr.next(); 
    		data[i][0] = new SimpleDateFormat("EEE dd/MM/yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(date));
    		data[i][1] = weatherData.getDateMaxTemperatures().get(date).toString();
    		data[i][2] = weatherData.getDateClearSkies().get(date) ? "Yes" : "No";
    				
    	}
    	String col[] = { "Date", "Max temp (°C)", "Sunny?" };
    	JTable table = new JTable(new DefaultTableModel(data,col));
    	table.setEnabled(false);
    	JTableHeader header = table.getTableHeader();
    	header.setBackground(Color.GRAY);
    	header.setFont(boldFont);
    	JScrollPane pane = new JScrollPane(table);

    	JPanel panel2 = new JPanel();
    	panel2.add(pane);
    	
    	// Display days above 20 degrees celsius and sunny days within 5 day period
    	JPanel panel3 = new JPanel();
    	JLabel statsLabel = new JLabel("Days above 20°C: " + weatherData.getNoOfDaysAbove20Degrees() + "    |    Sunny days: " + weatherData.getNoOfDaysWithClearSkies());
    	statsLabel.setFont(boldFont);
    	panel3.add(statsLabel);
    	
    	frame.add(panel, BorderLayout.NORTH);
    	frame.add(panel2, BorderLayout.CENTER);
    	frame.add(panel3, BorderLayout.SOUTH);
    	frame.setVisible(true);
    	
    	// Set timer to update the weather data dynamically
    	final Timer timer = new Timer(30000, null);
    	ActionListener listener = new ActionListener() {
    	    public void actionPerformed(ActionEvent e) {
    	    	try {
    	    		// Retrieve updated data
					weatherData.updateWeatherData();
				} catch (SAXException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				
    	    	// Update weather data on GUI
    	    	lastUpdatedLabel.setText("Last updated: " + new SimpleDateFormat("dd/MM/YYYY HH:mm:ss").format(new Date()));
    	    	statsLabel.setText("Days above 20°C: " + weatherData.getNoOfDaysAbove20Degrees() + "    |    Sunny days: " + weatherData.getNoOfDaysWithClearSkies());

		    	Iterator<String> itr = weatherData.getDates().iterator();
		    	for (int i = 0; i < weatherData.getDates().size(); i++) {
		    		String date = itr.next(); 
			    	try {
						table.setValueAt(new SimpleDateFormat("EEE dd/MM/yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(date)), i, 0);
					} catch (ParseException ex) {
						ex.printStackTrace();
					}
			    	table.setValueAt(weatherData.getDateMaxTemperatures().get(date).toString(), i, 1);
			    	table.setValueAt(weatherData.getDateClearSkies().get(date) ? "Yes" : "No", i, 2);
		    	}
    	    }
    	};
    	timer.addActionListener(listener);
    	timer.start();
    
    }
    
}
