package main.java;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WeatherData {

	private String city;
	private String apiKey = "a61d871b5f61392c6fa903af9067cf16";

	private LinkedHashMap<String, Double> dateMaxTemperatures;
	private LinkedHashMap<String, Boolean> dateClearSkies;

	public WeatherData(String city) {
		this.city = city;
		dateMaxTemperatures = new LinkedHashMap<>();
		dateClearSkies = new LinkedHashMap<>();
	}

	public Document getWeatherDataXML() throws SAXException, IOException {

		URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey + "&units=metric&mode=xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;

		try {
			builder = factory.newDocumentBuilder();
			InputStream in = url.openStream();
			doc = builder.parse(in);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return doc;
	}

	public void updateWeatherData() throws SAXException, IOException {

		// Clear existing data
		dateMaxTemperatures.clear();
		dateClearSkies.clear();
		updateDateMaxTemperatures();
		updateDateClearSkies();

		// Don't include first entry in weather data (usually today)
		String firstEntry = dateMaxTemperatures.keySet().iterator().next();
		dateMaxTemperatures.remove(firstEntry);
		dateClearSkies.remove(firstEntry);
	}

	private LinkedHashMap<String, Boolean> updateDateClearSkies() throws SAXException, IOException {

		Document doc = getWeatherDataXML();
		NodeList nodes = doc.getElementsByTagName("time");

		// Loop through the nodes
		for (int i = 0; i < nodes.getLength(); i++) {

			Node currentNode = nodes.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) currentNode;
				String date = currentNode.getAttributes().getNamedItem("from").getNodeValue().substring(0, 10);
				String condition = element.getElementsByTagName("symbol").item(0).getAttributes().getNamedItem("name")
						.getNodeValue();

				// Store whether day is every sunny for each date
				boolean isSunny = condition.equals("clear sky");
				if (dateClearSkies.isEmpty()) {
					dateClearSkies.put(date, isSunny);
				} else {
					if (dateClearSkies.containsKey(date)) {
						if (!dateClearSkies.get(date) && isSunny) {
							dateClearSkies.put(date, isSunny);
						}
					} else {
						dateClearSkies.put(date, isSunny);
					}
				}
			}
		}

		return dateClearSkies;
	}

	private LinkedHashMap<String, Double> updateDateMaxTemperatures() throws SAXException, IOException {

		Document doc = getWeatherDataXML();
		NodeList nodes = doc.getElementsByTagName("time");

		// Loop through the nodes
		for (int i = 0; i < nodes.getLength(); i++) {

			Node currentNode = nodes.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {

				Element element = (Element) currentNode;
				String date = currentNode.getAttributes().getNamedItem("from").getNodeValue().substring(0, 10);
				Double temperature = Double.valueOf(element.getElementsByTagName("temperature").item(0).getAttributes()
						.getNamedItem("max").getNodeValue());

				// Store max temperatures for each date
				if (dateMaxTemperatures.isEmpty()) {
					dateMaxTemperatures.put(date, temperature);
				} else {
					if (dateMaxTemperatures.containsKey(date)) {
						if (dateMaxTemperatures.get(date) < temperature) {
							dateMaxTemperatures.put(date, temperature);
						}
					} else {
						dateMaxTemperatures.put(date, temperature);
					}
				}

			}
		}
		return dateMaxTemperatures;
	}

	public int getNoOfDaysAbove20Degrees() {

		int noOfDaysAbove20Degrees = 0;

		// Add number of days above 20 degrees in the next 5 days
		for (String date : dateMaxTemperatures.keySet()) {
			if (dateMaxTemperatures.get(date) > 20.0) {
				noOfDaysAbove20Degrees++;
			}
		}
		return noOfDaysAbove20Degrees;
	}

	public int getNoOfDaysWithClearSkies() {

		int noOfDaysWithClearSkies = 0;

		// Add number of days above 20 degrees in the next 5 days
		for (String date : dateClearSkies.keySet()) {
			if (dateClearSkies.get(date)) {
				noOfDaysWithClearSkies++;
			}
		}
		return noOfDaysWithClearSkies;
	}

	public LinkedHashMap<String, Double> getDateMaxTemperatures() {
		return dateMaxTemperatures;
	}

	public LinkedHashMap<String, Boolean> getDateClearSkies() {
		return dateClearSkies;
	}

	public Set<String> getDates() {
		return dateMaxTemperatures.keySet();
	}
}
