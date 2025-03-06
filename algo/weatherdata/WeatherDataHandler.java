package algo.weatherdata;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.PatternSyntaxException;

/**
 * Retrieves temperature data from a weather station file.
 */
public class WeatherDataHandler {

	//Populated with Weather objects from the data table
	private ArrayList<WeatherDataModel> weather = new ArrayList<WeatherDataModel>();

	/**
	 * Load weather data from file.
	 * 
	 * @param filePath path to file with weather data
	 * @throws IOException if there is a problem while reading the file
	 */
	public void loadData(String filePath) throws IOException {		
		List<String> fileData = Files.readAllLines(Paths.get(filePath)); //O(1)

		try {
			
			// Incoming String format -> {Datum;Tid (UTC);Lufttemperatur;Kvalitet}
			for (String line : fileData) { //O(n)
				String[] data = line.split(";"); //O(1)
	
				LocalDate newDate = LocalDate.parse(data[0]); //O(1)
				LocalTime newTime = LocalTime.parse(data[1]); //O(1)
				float newTemp = Float.parseFloat(data[2]); //O(1)
				boolean goodAir = data[3].equals("G"); //O(1)
	
				weather.add(new WeatherDataModel(newDate, newTime, newTemp, goodAir)); //O(1)
			}
		} catch (PatternSyntaxException e) {
			System.out.println("Could not split line:" + e.getLocalizedMessage());
		} catch (DateTimeParseException e) {
			System.out.println("Could not parse date:" + e.getLocalizedMessage());
		} catch (NumberFormatException e) {
			System.out.println("Could not parse temp:" + e.getLocalizedMessage());
		} catch (NullPointerException e) {
			System.out.println("Something could not be located: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Search for average temperature for all dates between the two dates (inclusive).
	 * Result is sorted by date (ascending). When searching from 2000-01-01 to 2000-01-03
	 * the result should be:
	 * 2000-01-01 average temperature: 0.42 degrees Celsius
	 * 2000-01-02 average temperature: 2.26 degrees Celsius
	 * 2000-01-03 average temperature: 2.78 degrees Celsius
	 * 
	 * @param dateFrom start date (YYYY-MM-DD) inclusive  
	 * @param dateTo end date (YYYY-MM-DD) inclusive
	 * @return average temperature for each date, sorted by date  
	 */
	public List<String> averageTemperatures(LocalDate dateFrom, LocalDate dateTo) {
		int firstIndex = findFirstIndexByDate(dateFrom, 0, weather.size()-1 , (weather.size() / 2)); // O(log n)
		int lastIndex = findLastIndexByDate(dateTo, 0, weather.size()-1, (weather.size() + firstIndex )/2); // O(log k) k = range of n
		
		List<String> result = new ArrayList<String>(); //O(n)
		try {
			
			TreeMap<LocalDate, List<Float>> rangeList = getRangeWeatherList(firstIndex, lastIndex); //O(k) -> k = range within weather array
			
			
			for (Map.Entry<LocalDate, List<Float>> entry : rangeList.entrySet()) { // O(n) -> n = total dates (inclusive) within range k
				double avgTemp = BigDecimal.valueOf(entry.getValue().stream().mapToDouble(d -> d).average().getAsDouble()).setScale(2, RoundingMode.HALF_UP).doubleValue(); //O(1)
				result.add(entry.getKey().toString() + " average temperature: " + String.valueOf(avgTemp) + " degrees Celsius"); //O(1)
			}
		} catch (IndexOutOfBoundsException e) { 
			System.out.println("Invalid date given"); //O(1)
		} catch (Exception e) {
			System.out.println("Something went wrong: " + e.getLocalizedMessage()); //O(1)
		}
		return result; //O(1)
	}

	/**
	 * Creates HashMap<Date, List<Temperatures> from weather ArrayList. 
	 * Each key is the date, with all of the temperatures for that day added in a list of values.
	 * Example: Key: {2021-12-26}: Values: {1.1, 2.2, 2.1, 2.0...} 
	 * @param firstIndex where to start in the weather ArrayList
	 * @param lastIndex where to end in the weather ArrayList
	 * @return 
	 */
	private TreeMap<LocalDate, List<Float>> getRangeWeatherList(int firstIndex, int lastIndex) {
		TreeMap<LocalDate, List<Float>> rangeList = new TreeMap<LocalDate, List<Float>>(); //O(1)

		try {
			
			for(int i = firstIndex; i <= lastIndex; i++) { //O(k) k = range 
				LocalDate date = weather.get(i).date; //O(1)
				float temp = weather.get(i).airTemp; //O(1)
				
				if (rangeList.containsKey(date)) { //O(1)
					rangeList.get(date).add(temp); //O(1)
				} else {
					rangeList.put(date, new ArrayList<Float>()); //O(1)
					rangeList.get(date).add(temp); //O(1)
				}
			}
		} catch (IndexOutOfBoundsException e) { //O(1)
			System.out.println("Not a valid date. Please try again"); //O(1)
		} 

		return rangeList; //O(1)
	}

	/**
	 * Returns the index of the last Weather object with the given date
	 * @param searchDate {@code LocalDate} date (YYYY-MM-DD) to search for 
	 * @param startIndex first possible index in weather ArrayList to search
	 * @param endIndex last possible index in weather ArrayList to search
	 * @param searchIndex current index in weather ArrayList to search
	 * @return
	 */
	private int findLastIndexByDate(LocalDate searchDate, int startIndex, int endIndex, int searchIndex) {
		if (startIndex < endIndex) { //O(1)
			
			LocalDate currenDate = weather.get(searchIndex).date; //O(1)
			
			if (currenDate.equals(searchDate)) { //O(1)
				if (currenDate.equals(weather.get(searchIndex + 1).date)) { //O(1)
					return findLastIndexByDate(searchDate, searchIndex, endIndex, searchIndex + 1); //O(log n)
				}
			return searchIndex; //O(1)
			} else if (currenDate.isAfter(searchDate)) { //O(1)
				endIndex = searchIndex - 1; //O(1)
				searchIndex = (startIndex + endIndex) / 2; //O(1)
				return findLastIndexByDate(searchDate, startIndex, endIndex, searchIndex); //O(log n)
			} else {
				startIndex = searchIndex + 1; //O(1)
				searchIndex = (startIndex + endIndex) / 2; //O(1)
				return findLastIndexByDate(searchDate, startIndex, endIndex, searchIndex); //O(log n)
			}
		}

		return -1; //No result
	}

	/**
	 * Search for the index of the first entry in the array with the given Date.
	 * @param dateFrom date (YYYY-MM-DD) to search for
	 * @param startIndex first possible index in weather ArrayList to search
	 * @param endIndex last possible index in weather ArrayList to search
	 * @param searchIndex current index in weather ArrayList to search
	 * @return index of first entry in Array with given Date
	 */
	private int findFirstIndexByDate(LocalDate dateFrom, int startIndex, int endIndex, int searchIndex) { 

		if (startIndex < endIndex) { //O(1)
			
			LocalDate indexDate = weather.get(searchIndex).date; //O(1)
			
			if (indexDate.equals(dateFrom)) { //O(1)
				if (weather.get(searchIndex - 1).date.equals(dateFrom)) { //O(1)
					return findFirstIndexByDate(dateFrom, startIndex, endIndex, searchIndex - 1);  //O(log n)		
				}
				return searchIndex; //O(1)
			} else if (indexDate.isAfter(dateFrom)) { //O(1)
				endIndex = searchIndex - 1; //O(1)
				searchIndex = (startIndex + endIndex)/2; //O(1)
				return findFirstIndexByDate(dateFrom, startIndex, endIndex, searchIndex); //O(log n)
			} else {
				startIndex = searchIndex + 1; //O(1)
				searchIndex = (startIndex + endIndex) / 2; //O(1) 
				return findFirstIndexByDate(dateFrom, startIndex, endIndex, searchIndex); //O(log n)
			}
		}

		return -1; //No result

	}

	/**
	 * Search for missing values between the two dates (inclusive) assuming there 
	 * should be 24 measurement values for each day (once every hour). Result is
	 * sorted by number of missing values (descending). When searching from
	 * 2000-01-01 to 2000-01-03 the result should be:
	 * 2000-01-02 missing 1 values
	 * 2000-01-03 missing 1 values
	 * 2000-01-01 missing 0 values
	 * 
	 * @param dateFrom start date (YYYY-MM-DD) inclusive  
	 * @param dateTo end date (YYYY-MM-DD) inclusive
	 * @return dates with missing values together with number of missing values for each date, sorted by number of missing values (descending)
	 */
	public List<String> missingValues(LocalDate dateFrom, LocalDate dateTo) {
		int startIndex = findFirstIndexByDate(dateFrom, 0, weather.size() - 1, weather.size() / 2); //O(log n)
		int endIndex = findLastIndexByDate(dateTo, startIndex, weather.size()-1, (startIndex + weather.size()) / 2); //O(log k) k = range of n
		
		TreeMap<LocalDate, List<Float>> days = getRangeWeatherList(startIndex, endIndex); //O(k) k = range in weather collection

		ArrayList<String> result = new ArrayList<>(); //O(1)
		
		for (Map.Entry<LocalDate, List<Float>> entry : days.entrySet()) { //O(n) n = total dates (inclusive) in TreeMap days
			result.add(entry.getKey().toString() + " missing " + (24 - entry.getValue().size()) + " values"); // O(1)
		}
	
		return result; //O(1)
	}
	/**
	 * Search for percentage of approved values between the two dates (inclusive).
	 * When searching from 2000-01-01 to 2000-01-03 the result should be:
	 * Approved values between 2000-01-01 and 2000-01-03: 32.86 %
	 * 
	 * @param dateFrom start date (YYYY-MM-DD) inclusive  
	 * @param dateTo end date (YYYY-MM-DD) inclusive
	 * @return period and percentage of approved values for the period  
	 */
	public List<String> approvedValues(LocalDate dateFrom, LocalDate dateTo) {
		int totalRecords = 0, approvedRecords = 0; // O(1)
		int startIndex = findFirstIndexByDate(dateFrom, 0, weather.size()-1, weather.size()/2); //O(log n)
		int endIndex = findLastIndexByDate(dateTo, startIndex, weather.size() -1,( weather.size() + startIndex) / 2); //O(log k) k = range of n 
		
		List<String> result = new ArrayList<String>(1); //O(1)
		for (int i = startIndex; i <= endIndex; i++) { //O(k) k = range in weather collection
			if (weather.get(i).approved) { // O(1)
				approvedRecords++; //O(1)
			}
			totalRecords++; //O(1)
		}

		double percentage = ((double)approvedRecords / totalRecords) * 100; //O(1)
		percentage = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP).doubleValue(); //O(1)
		result.add("Approved values between " + dateFrom.toString() + " and " + dateTo.toString() + ": " + percentage + " %"); //O(1)

		return result; //O (1)
	}
}