// Copyright (C) 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.authentication.authenticators;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class PrototypeFirstBrokerAuthenticator implements Authenticator {

  private static final Logger LOG = Logger.getLogger(PrototypeFirstBrokerAuthenticator.class);

  @Override
  public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
    if (authenticationFlowContext.getAuthenticatorConfig() != null
        && authenticationFlowContext.getAuthenticatorConfig().getConfig().containsKey(PrototypeFirstBrokerAuthenticatorFactory.LOG_MESSAGE)) {
      LOG.infof("%s", authenticationFlowContext.getAuthenticatorConfig().getConfig().get(PrototypeFirstBrokerAuthenticatorFactory.LOG_MESSAGE));
    } else {
      LOG.infof("log message not configured!");
    }
    authenticationFlowContext.success();
  }

  @Override
  public void action(AuthenticationFlowContext authenticationFlowContext) {
    // intentionally empty
  }

  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    return false;
  }

  @Override
  public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    // intentionally empty
  }

  @Override
  public void close() {
    // intentionally empty
  }
}