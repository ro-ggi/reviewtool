package de.setsoftware.reviewtool.model;

public class TicketInfo {

	private final String key;
	private final String description;
	private final String state;
	private final String component;

	public TicketInfo(String key, String summary, String state, String component) {
		this.key = key;
		this.description = summary;
		this.state = state;
		this.component = component;
	}

	public String getID() {
		return this.key;
	}

	public String getSummary() {
		return this.description;
	}

	public String getState() {
		return this.state;
	}

	public String getComponent() {
		return this.component;
	}

}
