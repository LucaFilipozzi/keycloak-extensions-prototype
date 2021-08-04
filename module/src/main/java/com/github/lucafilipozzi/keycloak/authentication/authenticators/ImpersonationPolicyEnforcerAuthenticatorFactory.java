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

public class ImpersonationPolicyEnforcerAuthenticatorFactory implements AuthenticatorFactory {

  public static final String PROVIDER_ID = "impersonation-policy-enforcer";

  private static final Logger LOG = Logger.getLogger(ImpersonationPolicyEnforcerAuthenticatorFactory.class);

  @Override
  public String getDisplayType() {
    return "Impersonation Policy Enforcer";
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
    return "this is the impersonator policy enforcer authenticator";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return Collections.emptyList();
  }

  @Override
  public Authenticator create(KeycloakSession keycloakSession) {
    LOG.trace("instantiating an ImpersonationPolicyEnforcerAuthenticator object");
    return new ImpersonationPolicyEnforcerAuthenticator();
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