package com.svenruppert.vaadin.builder;

import com.vaadin.flow.component.textfield.TextField;

public class TextFieldBuilder extends AbstractFlowComponentBuilder<TextFieldBuilder,TextField>{

  @Override
  protected TextFieldBuilder self() {
    return this;
  }

  @Override
  public TextField build() {
    TextField textField = new TextField();
    textField.setId(id);
    textField.setLabel(text);

    return textField;
  }
}
