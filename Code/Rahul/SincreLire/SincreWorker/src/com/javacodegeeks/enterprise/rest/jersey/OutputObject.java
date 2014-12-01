package com.javacodegeeks.enterprise.rest.jersey;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OutputObject {
	private String name;
	private String url;

	public OutputObject() {

	}

	public OutputObject(String id, String summary) {
		this.name = id;
		this.url = summary;
	}

	public String getId() {
		return name;
	}

	public void setId(String id) {
		this.name = id;
	}

	public String getSummary() {
		return url;
	}

	public void setSummary(String summary) {
		this.url = summary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OutputObject [name=" + name + ", url=" + url + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OutputObject other = (OutputObject) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	

}