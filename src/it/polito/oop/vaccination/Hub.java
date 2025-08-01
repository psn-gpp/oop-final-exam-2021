package it.polito.oop.vaccination;

public class Hub {
	private String name;
	private int doctors, nurses, other;
	private boolean staffSet = false;
	
	
	public int getDoctors() {
		return doctors;
	}

	public int getNurses() {
		return nurses;
	}

	public int getOther() {
		return other;
	}

	public boolean isStaffSet() {
		return staffSet;
	}

	public void setStaffSet(boolean staffSet) {
		this.staffSet = staffSet;
	}

	public void setDoctors(int doctors) {
		this.doctors = doctors;
	}

	public void setNurses(int nurses) {
		this.nurses = nurses;
	}

	public void setOther(int other) {
		this.other = other;
	}

	public Hub(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	
}
