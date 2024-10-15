package com.svenruppert.vaadin.builder;

import com.vaadin.flow.component.Component;

public abstract class AbstractFlowComponentBuilder<
    T extends AbstractFlowComponentBuilder<T, C>,
    C extends Component> {

  protected String id;
  protected String text;

  protected abstract T self();

  public abstract C build();

  public T withId(String id) {
    this.id = id;
    return self();
  }
}
