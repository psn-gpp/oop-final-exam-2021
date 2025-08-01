package it.polito.oop.vaccination;

public class Person {
	private String ssn;
	private String firstName, lastName;
	private int year;
	private int allocatedDay = -1;
	private String allocatedHub = null;
	
	public Person(String ssn, String firstName, String lastName, int year) {
		super();
		this.ssn = ssn;
		this.firstName = firstName;
		this.lastName = lastName;
		this.year = year;
	}

	public boolean isAllocated() {
		return (allocatedDay != -1 && allocatedHub != null);
	}
	
	public int getAllocatedDay() {
		return allocatedDay;
	}
	
	public String getAllocatedHub() {
		return allocatedHub;
	}
	
	public void setAllocated(int day, String hub) {
		this.allocatedDay = day;
		this.allocatedHub = hub;
	}

	public String getSsn() {
		return ssn;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public int getYear() {
		return year;
	}
	
	@Override
	public String toString() {
		return ssn +"," + lastName + "," + firstName;
	}
}
