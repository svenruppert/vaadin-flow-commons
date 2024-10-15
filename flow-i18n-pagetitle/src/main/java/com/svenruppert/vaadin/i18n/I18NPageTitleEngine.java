/**
 * Copyright © 2018 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.svenruppert.vaadin.i18n;

import com.svenruppert.dependencies.core.logger.HasLogger;
import com.svenruppert.functional.functions.CheckedFunction;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.server.*;

import java.util.List;
import java.util.Locale;

import static com.svenruppert.functional.matcher.Case.match;
import static com.svenruppert.functional.matcher.Case.matchCase;
import static com.svenruppert.functional.model.Result.failure;
import static com.svenruppert.functional.model.Result.success;


public class I18NPageTitleEngine implements VaadinServiceInitListener,
                                            UIInitListener,
                                            BeforeEnterListener,
    HasLogger {


  public static final String ERROR_MSG_NO_LOCALE = "no locale provided and i18nProvider #getProvidedLocales()# list is empty !! ";
  public static final String ERROR_MSG_NO_ANNOTATION = "no annotation found at class ";

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    Class<?> navigationTarget = event.getNavigationTarget();
    I18NPageTitle annotation = navigationTarget.getAnnotation(I18NPageTitle.class);
    match(
        matchCase(() -> success(annotation.messageKey())) ,
        matchCase(() -> annotation == null ,
                  () -> failure(ERROR_MSG_NO_ANNOTATION + navigationTarget.getName())) ,
        matchCase(() -> annotation.messageKey().isEmpty() ,
                  () -> success(annotation.defaultValue()))
    )
        .ifPresentOrElse(
            msgKey -> {
              final I18NProvider i18NProvider = VaadinService
                  .getCurrent()
                  .getInstantiator()
                  .getI18NProvider();
              final Locale locale = event.getUI().getLocale();
              final List<Locale> providedLocales = i18NProvider.getProvidedLocales();
              match(
                  matchCase(() -> success(providedLocales.get(0))) ,
                  matchCase(() -> locale == null && providedLocales.isEmpty() ,
                            () -> failure(ERROR_MSG_NO_LOCALE + i18NProvider.getClass().getName())) ,
                  matchCase(() -> locale == null ,
                            () -> success(providedLocales.get(0))) ,
                  matchCase(() -> providedLocales.contains(locale) ,
                            () -> success(locale))
              ).ifPresentOrElse(
                  finalLocale -> ((CheckedFunction<Class<? extends TitleFormatter>, TitleFormatter>) f -> f.getDeclaredConstructor().newInstance())
                      .apply(annotation.formatter())
                      .ifPresentOrElse(
                          formatter -> formatter
                              .apply(i18NProvider , finalLocale , msgKey).
                                  ifPresentOrElse(title -> UI.getCurrent()
                                                             .getPage()
                                                             .setTitle(title) ,
                                                  failed -> logger().info(failed)) ,
                          failed -> logger().info(failed)) ,
                  failed -> logger().info(failed));
            }
            , failed -> logger().info(failed));
  }

  @Override
  public void uiInit(UIInitEvent event) {
    final UI ui = event.getUI();
    ui.addBeforeEnterListener(this);
    //addListener(ui, PermissionsChangedEvent.class, e -> ui.getPage().reload());
  }

  @Override
  public void serviceInit(ServiceInitEvent event) {
    event
        .getSource()
        .addUIInitListener(this);
  }
}
