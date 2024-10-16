/**
 * Copyright © 2017 Sven Ruppert (sven.ruppert@gmail.com)
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
package com.svenruppert.vaadin.security.authorization;

import com.svenruppert.dependencies.core.logger.HasLogger;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import org.jetbrains.annotations.NotNull;
import com.svenruppert.vaadin.security.authorization.api.SessionAccessor;

import java.lang.annotation.Annotation;


/**
 * The final implementation inside a project must be "activated" with its FQN
 * in a file META-INF/services/org.rapidpm.vaadin.security.authorization.LoginListener.
 *
 * @param <U> the type that is used for the subject in the current project. It is the same as
 *            the result type of the method - public Class U  subjectType();
 */
public abstract class LoginListener<U>
    implements BeforeEnterListener, HasLogger {
  @Override
  public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    final Class<?> navigationTarget = beforeEnterEvent.getNavigationTarget();
    final boolean  isLoginView      = navigationTarget.equals(loginNavigationTarget());
    final boolean  isRestrictedPage = navigationTarget.isAnnotationPresent(restrictionAnnotation());
    if (isRestrictedPage) {
      isARestrictedPage(beforeEnterEvent, isLoginView);
    } else {
      notARestrictedTarget(navigationTarget);
    }
  }

  private void isARestrictedPage(BeforeEnterEvent beforeEnterEvent, boolean isLoginView) {
    SessionAccessor.<U>currentSubject().ifPresentOrElse(ok -> {
      logger().info("User is already logged in ");
      subjectIsAvailableInSession(beforeEnterEvent, isLoginView, ok);
    }, failed -> subjectIsNotAvailableInSession(beforeEnterEvent, isLoginView));
  }

  public abstract void notARestrictedTarget(Class<?> navigationTarget);

  public void subjectIsNotAvailableInSession(BeforeEnterEvent beforeEnterEvent, boolean isLoginView) {
    logger().info("Login required..");
    if (isLoginView) {
      logger().info(".. on LoginView..  start login process");
    } else {
      logger().info(".. start forwarding to LoginView..");
      beforeEnterEvent.forwardTo(loginNavigationTarget());
    }
  }

  public void subjectIsAvailableInSession(BeforeEnterEvent beforeEnterEvent, boolean isLoginView, U subject) {
    logger().info("User is already logged in ");
    if (isLoginView) {
      logger().info(".. forwarding from LoginView to Default");
      beforeEnterEvent.forwardTo(defaultNavigationTarget());
    }
  }

  /**
   * This is the Annotation - Type that is used for the restriction declaration on class-level.
   * For Example VisibleFor.class
   *
   * @return the restriction Annotation class for the current project
   */
  @NotNull
  public abstract Class<? extends Annotation> restrictionAnnotation();

  /**
   * The LoginView that should be used.
   *
   * @return the class with the Route Annotation for the login view
   */
  @NotNull
  public abstract Class<? extends LoginView> loginNavigationTarget();


  /**
   * The class with the Route Annotation that will point to the default view in the current project.
   * Mostly this is the view that should be used after a login.
   *
   * @return the class with the Route Annotation for the default (main) view
   */
  @NotNull
  public abstract Class<? extends Component> defaultNavigationTarget();
}
