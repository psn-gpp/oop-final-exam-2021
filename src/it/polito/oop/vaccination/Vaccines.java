package it.polito.oop.vaccination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.*;
import java.util.stream.Stream;

public class Vaccines {

	public final static int CURRENT_YEAR = java.time.LocalDate.now().getYear();

	private int[][] ageIntervals;
	private Map<String, Person> people = new TreeMap<>();
	private Map<String, Hub> hubs = new TreeMap<>();
	private int[] hours;
	private BiConsumer<Integer, String> listener = null;

	// R1
	/**
	 * Add a new person to the vaccination system.
	 *
	 * Persons are uniquely identified by SSN (italian "codice fiscale")
	 *
	 * @param firstName first name
	 * @param lastName  last name
	 * @param ssn       italian "codice fiscale"
	 * @param year      birth year
	 * @return {@code false} if ssn is duplicate,
	 */
	public boolean addPerson(String firstName, String lastName, String ssn, int year) {
		if (people.containsKey(ssn)) {
			return false;
		}

		people.put(ssn, new Person(ssn, firstName, lastName, year));

		return true;
	}

	/**
	 * Count the number of people added to the system
	 *
	 * @return person count
	 */
	public int countPeople() {
		return people.size();
	}

	/**
	 * Retrieves information about a person. Information is formatted as ssn, last
	 * name, and first name separate by {@code ','} (comma).
	 *
	 * @param ssn "codice fiscale" of person searched
	 * @return info about the person
	 */
	public String getPerson(String ssn) {
		return people.get(ssn).toString();
	}

	/**
	 * Retrieves of a person given their SSN (codice fiscale).
	 *
	 * @param ssn "codice fiscale" of person searched
	 * @return age of person (in years)
	 */
	public int getAge(String ssn) {
		int year = people.get(ssn).getYear();

		return CURRENT_YEAR - year;
	}

	/**
	 * Define the age intervals by providing the breaks between intervals. The first
	 * interval always start at 0 (non included in the breaks) and the last interval
	 * goes until infinity (not included in the breaks). All intervals are closed on
	 * the lower boundary and open at the upper one.
	 * <p>
	 * For instance {@code setAgeIntervals(40,50,60)} defines four intervals
	 * {@code "[0,40)", "[40,50)", "[50,60)", "[60,+)"}.
	 *
	 * @param brks the array of breaks
	 */
	public void setAgeIntervals(int... brks) {
		ageIntervals = new int[brks.length + 1][2];

		for (int i = 0; i < ageIntervals.length; i++) {
			if (i == 0)
				ageIntervals[i][0] = 0;
			else
				ageIntervals[i][0] = brks[i - 1];

			if ((i) == brks.length)
				ageIntervals[i][1] = Integer.MAX_VALUE;
			else
				ageIntervals[i][1] = brks[i];
		}
	}

	/**
	 * Retrieves the labels of the age intervals defined.
	 *
	 * Interval labels are formatted as {@code "[0,10)"}, if the upper limit is
	 * infinity {@code '+'} is used instead of the number.
	 *
	 * @return labels of the age intervals
	 */
	public Collection<String> getAgeIntervals() {
		String s[] = new String[ageIntervals.length];

		for (int i = 0; i < ageIntervals.length; i++) {
			s[i] = "[" + ageIntervals[i][0] + "," + (((i + 1) == ageIntervals.length) ? "+" : ageIntervals[i][1]) + ")";
		}

		return Stream.of(s).collect(toList());
	}

	/**
	 * Retrieves people in the given interval.
	 *
	 * The age of the person is computed by subtracting the birth year from current
	 * year.
	 *
	 * @param interval age interval label
	 * @return collection of SSN of person in the age interval
	 */
	public Collection<String> getInInterval(String interval) {
		Pattern re = Pattern.compile("([0-9]+),([0-9]+|\\+)");
		Matcher m = re.matcher(interval);

		m.find();

		int lower = Integer.parseInt(m.group(1));
		int upper;
		if (m.group(2).equals("+"))
			upper = Integer.MAX_VALUE;
		else
			upper = Integer.parseInt(m.group(2));

		return people.values().stream().filter(a -> lower <= getAge(a.getSsn()) && getAge(a.getSsn()) < upper)
				.map(Person::getSsn).collect(toList());
	}

	// R2
	/**
	 * Define a vaccination hub
	 *
	 * @param name name of the hub
	 * @throws VaccineException in case of duplicate name
	 */
	public void defineHub(String name) throws VaccineException {
		if (hubs.containsKey(name))
			throw new VaccineException();

		hubs.put(name, new Hub(name));
	}

	/**
	 * Retrieves hub names
	 *
	 * @return hub names
	 */
	public Collection<String> getHubs() {
		return hubs.keySet();
	}

	/**
	 * Define the staffing of a hub in terms of doctors, nurses and other personnel.
	 *
	 * @param name    name of the hub
	 * @param doctors number of doctors
	 * @param nNurses number of nurses
	 * @param other   number of other personnel
	 * @throws VaccineException in case of undefined hub, or any number of personnel
	 *                          not greater than 0.
	 */
	public void setStaff(String name, int doctors, int nNurses, int other) throws VaccineException {
		if (!hubs.containsKey(name) || doctors <= 0 || nNurses <= 0 || other <= 0)
			throw new VaccineException();

		Hub h = hubs.get(name);

		h.setDoctors(doctors);
		h.setNurses(nNurses);
		h.setOther(other);
		h.setStaffSet(true);

	}

	/**
	 * Estimates the hourly vaccination capacity of a hub
	 *
	 * The capacity is computed as the minimum among 10*number_doctor,
	 * 12*number_nurses, 20*number_other
	 *
	 * @param hub name of the hub
	 * @return hourly vaccination capacity
	 * @throws VaccineException in case of undefined or hub without staff
	 */
	public int estimateHourlyCapacity(String hub) throws VaccineException {
		if (!hubs.containsKey(hub))
			throw new VaccineException();

		Hub h = hubs.get(hub);

		if (!h.isStaffSet())
			throw new VaccineException();

		int hourCapacity = Math.min(h.getDoctors() * 10, h.getNurses() * 12);
		hourCapacity = Math.min(hourCapacity, h.getOther() * 20);

		return hourCapacity;
	}

	// R3
	/**
	 * Load people information stored in CSV format.
	 *
	 * The header must start with {@code "SSN,LAST,FIRST"}. All lines must have at
	 * least three elements.
	 *
	 * In case of error in a person line the line is skipped.
	 *
	 * @param people {@code Reader} for the CSV content
	 * @return number of correctly added people
	 * @throws IOException      in case of IO error
	 * @throws VaccineException in case of error in the header
	 */
	public long loadPeople(Reader people) throws IOException, VaccineException {
		// Hint:
		BufferedReader br = new BufferedReader(people);

		int count = 0;
		int lineCount = 1; 

		Pattern intestazione = Pattern.compile("SSN,LAST,FIRST,YEAR");
		String line = br.readLine();
		Matcher m1 = intestazione.matcher(line);

		if (!m1.find()) {
			if(listener != null)
				listener.accept(lineCount, line);
			throw new VaccineException("Intestazione non valida");
		}

		Pattern re = Pattern.compile("(?<ssn>[A-Z0-9]+),(?<last>[^,]+),(?<first>[^,]+),(?<year>[0-9]+)");
		while ((line = br.readLine()) != null) {
			lineCount++;
			Matcher m2 = re.matcher(line);
			if (m2.find()) {
				String first = m2.group("first");
				String last = m2.group("last");
				String ssn = m2.group("ssn");
				int year = Integer.parseInt(m2.group("year"));

				if (!addPerson(first, last, ssn, year)) {
					if(listener != null)
						listener.accept(lineCount, line);
					continue;
				}

				count++;
			}
			else {
				if(listener != null)
					listener.accept(lineCount, line);
			}

		}

		br.close();

		return count;
	}

	// R4
	/**
	 * Define the amount of working hours for the days of the week.
	 *
	 * Exactly 7 elements are expected, where the first one correspond to Monday.
	 *
	 * @param hs workings hours for the 7 days.
	 * @throws VaccineException if there are not exactly 7 elements or if the sum of
	 *                          all hours is less than 0 ore greater than 24*7.
	 */
	public void setHours(int... hs) throws VaccineException {
		if (hs.length != 7)
			throw new VaccineException();

		for (int a : hs) {
			if (a >= 12)
				throw new VaccineException();
		}

		hours = hs;
	}

	/**
	 * Returns the list of standard time slots for all the days of the week.
	 *
	 * Time slots start at 9:00 and occur every 15 minutes (4 per hour) and they
	 * cover the number of working hours defined through method {@link #setHours}.
	 * <p>
	 * Times are formatted as {@code "09:00"} with both minuts and hours on two
	 * digits filled with leading 0.
	 * <p>
	 * Returns a list with 7 elements, each with the time slots of the corresponding
	 * day of the week.
	 *
	 * @return the list hours for each day of the week
	 */
	public List<List<String>> getHours() {
		List<List<String>> outerList = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			List<String> innerList = new ArrayList<>();
			for (int j = 0; j < hours[i]; j++) {
				int hour = 9;
				int quarter = 0;
				for(int k = 0; k < 4; k++) {
					innerList.add(String.format("%02d:%02d", hour, quarter));
					quarter += 15;
					hour++;
				}
			}
			outerList.add(i, innerList);
		}
		
		return outerList;
	}

	/**
	 * Compute the available vaccination slots for a given hub on a given day of the
	 * week
	 * <p>
	 * The availability is computed as the number of working hours of that day
	 * multiplied by the hourly capacity (see {@link #estimateCapacity} of the hub.
	 *
	 * @return
	 */
	public int getDailyAvailable(String hub, int day) {
		try {
			return hours[day] * estimateHourlyCapacity(hub);
		} catch (VaccineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}

	/**
     * Compute the available vaccination slots for each hub and for each day of the week
     * <p>
     * The method returns a map that associates the hub names (keys) to the lists
     * of number of available hours for the 7 days.
     * <p>
     * The availability is computed as the number of working hours of that day
     * multiplied by the capacity (see {@link #estimateCapacity} of the hub.
     *
     * @return
     */
    public Map<String, List<Integer>> getAvailable() {
        Map<String, List<Integer>> available = new HashMap<>();
        
        for(Hub h : hubs.values()) {
        	List<Integer> list = new ArrayList<>();
	        for(int i = 0; i < 7; i++) {
	        	list.add(i, getDailyAvailable(h.getName(), i));
	        }
	        available.put(h.getName(), list);
        }
        
    	return available;
    }

	/**
	 * Computes the general allocation plan a hub on a given day. Starting with the
	 * oldest age intervals 40% of available places are allocated to persons in that
	 * interval before moving the the next interval and considering the remaining
	 * places.
	 * <p>
	 * The returned value is the list of SSNs (codice fiscale) of the persons
	 * allocated to that day
	 * <p>
	 * <b>N.B.</b> no particular order of allocation is guaranteed
	 *
	 * @param hub name of the hub
	 * @param day day of week index (0 = Monday)
	 * @return the list of daily allocations
	 */
	public List<String> allocate(String hub, int day) {
		int n = getDailyAvailable(hub, day);
		
		List<String> selected = new ArrayList<>();
		
		for(int i = ageIntervals.length-1; i >= 0 && n > 0; i--) {
			int lower = ageIntervals[i][0];
			int upper = ageIntervals[i][1];
			
			long m = people.values().stream()
					.filter(a -> !a.isAllocated())
					.filter(a -> lower <= getAge(a.getSsn()) && getAge(a.getSsn()) < upper)
					.limit((long) (n * 0.4))
					.peek(a -> a.setAllocated(day, hub))
					.map(Person::getSsn)
					.peek(a -> selected.add(a))
					.count();
			n -= m;
		}
		
		for(int i = ageIntervals.length-1; i >= 0 && n > 0; i--) {
			int lower = ageIntervals[i][0];
			int upper = ageIntervals[i][1];
			
			long m = people.values().stream()
					.filter(a -> !a.isAllocated())
					.filter(a -> lower <= getAge(a.getSsn()) && getAge(a.getSsn()) < upper)
					.limit((long) n)
					.peek(a -> a.setAllocated(day, hub))
					.map(Person::getSsn)
					.peek(a -> selected.add(a))
					.count();	
			n -= m;
		}
		
		return selected;
	}

	/**
	 * Removes all people from allocation lists and clears their allocation status
	 */
	public void clearAllocation() {
		people.values().stream().forEach(a -> a.setAllocated(-1, null));
	}

	/**
	 * Computes the general allocation plan for the week. For every day, starting
	 * with the oldest age intervals 40% available places are allocated to persons
	 * in that interval before moving the the next interval and considering the
	 * remaining places.
	 * <p>
	 * The returned value is a list with 7 elements, one for every day of the week,
	 * each element is a map that links the name of each hub to the list of SSNs
	 * (codice fiscale) of the persons allocated to that day in that hub
	 * <p>
	 * <b>N.B.</b> no particular order of allocation is guaranteed but the same
	 * invocation (after {@link #clearAllocation}) must return the same allocation.
	 *
	 * @return the list of daily allocations
	 */
	public List<Map<String, List<String>>> weekAllocate() {
		List<Map<String, List<String>>> weekAllocate = new ArrayList<>();
		
		for(int i = 0; i < 7; i++) {
			int day = i;
			weekAllocate.add(day, hubs.values().stream()
					.collect(toMap(Hub::getName, a -> allocate(a.getName(), day))));
			
		}
		return weekAllocate;
	}

	// R5
	/**
	 * Returns the proportion of allocated people w.r.t. the total number of persons
	 * added in the system
	 *
	 * @return proportion of allocated people
	 */
	public double propAllocated() {
		return ((double)people.values().stream().filter(a -> a.isAllocated()).count())/people.size();
	}

	/**
	 * Returns the proportion of allocated people w.r.t. the total number of persons
	 * added in the system, divided by age interval.
	 * <p>
	 * The map associates the age interval label to the proportion of allocates
	 * people in that interval
	 *
	 * @return proportion of allocated people by age interval
	 */
	public Map<String, Double> propAllocatedAge() {
		List<String> intervalsString = new ArrayList<>(getAgeIntervals());
		Map<String, Double> propAllocatedAge = new HashMap<>();
		
		for(int i = 0; i < ageIntervals.length; i++) {
			int lower = ageIntervals[i][0];
			int upper = ageIntervals[i][1];
			
			double nAllocatedOnPlan = people.values().stream()
					.filter(a -> lower <= getAge(a.getSsn()) && getAge(a.getSsn()) < upper)
					.filter(a -> a.isAllocated())
					.count();
			double nTotalPeople = people.values().stream()
					.filter(a -> lower <= getAge(a.getSsn()) && getAge(a.getSsn()) < upper)
					.count();
			
			double m = nAllocatedOnPlan / nTotalPeople;
			
			propAllocatedAge.put(intervalsString.get(i), m);
		}
		
		return propAllocatedAge;
	}

	/**
	 * Retrieves the distribution of allocated persons among the different age
	 * intervals.
	 * <p>
	 * For each age intervals the map reports the proportion of allocated persons in
	 * the corresponding interval w.r.t the total number of allocated persons
	 *
	 * @return
	 */
	public Map<String, Double> distributionAllocated() {
		List<String> intervalsString = new ArrayList<>(getAgeIntervals());
		Map<String, Double> distributionAllocated = new HashMap<>();
		
		for(int i = 0; i < ageIntervals.length; i++) {
			int lower = ageIntervals[i][0];
			int upper = ageIntervals[i][1];
			
			double nAllocatedOnPlan = people.values().stream()
					.filter(a -> a.isAllocated())
					.filter(a -> lower <= getAge(a.getSsn()) && getAge(a.getSsn()) < upper)
					.count();
			
			double nTotalPeople = people.values().stream()
					.filter(a -> a.isAllocated())
					.count();
			
			double m = nAllocatedOnPlan / nTotalPeople;
			
			distributionAllocated.put(intervalsString.get(i), m);
		}
		
		return distributionAllocated;
	}

	// R6
	/**
	 * Defines a listener for the file loading method. The {@ accept()} method of
	 * the listener is called passing the line number and the offending line.
	 * <p>
	 * Lines start at 1 with the header line.
	 *
	 * @param lst the listener for load errors
	 */
	public void setLoadListener(BiConsumer<Integer, String> lst) {
		this.listener = lst;
	}
}
