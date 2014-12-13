package com.javacodegeeks.enterprise.rest.jersey;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * this is object that is sent to the client with the list of videos
 * 
 * @author Rahul
 * 
 */
@SuppressWarnings("restriction")
@XmlRootElement
public class OutputObject {
	private String id;
	private String summary;

	public OutputObject() {

	}

	public OutputObject(String id, String summary) {
		this.id = id;
		this.summary = summary;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((summary == null) ? 0 : summary.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OutputObject other = (OutputObject) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (summary == null) {
			if (other.summary != null)
				return false;
		} else if (!summary.equals(other.summary))
			return false;
		return true;
	}

}