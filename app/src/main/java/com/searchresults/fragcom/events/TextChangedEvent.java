package com.searchresults.fragcom.events;

public class TextChangedEvent {
  public String newText;
  public TextChangedEvent(String newText) {
      this.newText = newText;
  }
}