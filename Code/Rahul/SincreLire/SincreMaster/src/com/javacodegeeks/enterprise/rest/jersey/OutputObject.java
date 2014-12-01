package com.javacodegeeks.enterprise.rest.jersey;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Rahul
 *
 */
@SuppressWarnings("restriction")
@XmlRootElement
public class OutputObject {
  private String id;
  private String summary;
  
  public OutputObject(){
    
  }
  public OutputObject (String id, String summary){
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
  
} 