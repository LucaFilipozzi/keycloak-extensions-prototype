// Copyright 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.authentication.authenticators;

import java.util.Collections;
import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public class PostBrokerAuthenticatorFactory implements AuthenticatorFactory {

  public static final String PROVIDER_ID = "prototype-post-broker-authenticator";

  protected static final String LOG_MESSAGE = "log message";

  private static final Logger LOG = Logger.getLogger(PostBrokerAuthenticatorFactory.class);

  @Override
  public String getDisplayType() {
    return "Prototype Post Broker Authenticator";
  }

  @Override
  public String getReferenceCategory() {
    return null;
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public String getHelpText() {
    return "this is the prototype post broker authenticator";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    ProviderConfigProperty providerConfigProperty = new ProviderConfigProperty(
        LOG_MESSAGE,
        "message to log",
        "message to log",
        ProviderConfigProperty.STRING_TYPE,
        null);
    return Collections.singletonList(providerConfigProperty);
  }

  @Override
  public Authenticator create(KeycloakSession keycloakSession) {
    LOG.trace("instantiating a PostBrokerAuthenticator object");
    return new PostBrokerAuthenticator();
  }

  @Override
  public void init(Scope scope) {
    // intentionally empty
  }

  @Override
  public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    // intentionally empty
  }

  @Override
  public void close() {
    // intentionally empty
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}