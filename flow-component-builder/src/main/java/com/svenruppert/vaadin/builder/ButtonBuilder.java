package com.svenruppert.vaadin.builder;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;

public class ButtonBuilder extends AbstractFlowComponentBuilder<ButtonBuilder,Button> {

  private String text;
  private ComponentEventListener<ClickEvent<Button>> clickListener;

  private ButtonBuilder() {
  }

  public static ButtonBuilder buttonBuilder() {
    return new ButtonBuilder();
  }


  @Override
  protected ButtonBuilder self() {
    return this;
  }

  public Button build() {
    Button button = new Button();
    button.setText(text);
    button.setId(id);
    button.addClickListener(clickListener);
    return button;
  }

  public ButtonBuilder withText(String text) {
    this.text = text;
    return this;
  }



  public ButtonBuilder withClickListener(ComponentEventListener<ClickEvent<Button>> clickListener) {
    this.clickListener = clickListener;
    return this;
  }

}
