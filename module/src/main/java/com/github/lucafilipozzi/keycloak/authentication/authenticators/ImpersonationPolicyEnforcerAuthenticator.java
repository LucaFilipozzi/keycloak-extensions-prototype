// Copyright (C) 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.authentication.authenticators;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.sessions.AuthenticationSessionModel;

public class ImpersonationPolicyEnforcerAuthenticator implements Authenticator {

  private static final Logger LOG = Logger.getLogger(ImpersonationPolicyEnforcerAuthenticator.class);

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    KeycloakSession keycloakSession = context.getSession();
    RealmModel realm = context.getRealm();
    AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(keycloakSession, realm, true);
    if (authResult == null) {
      context.attempted();
      return;
    }

    UserSessionModel userSession = authResult.getSession();
    AuthenticationSessionModel clientSession = context.getAuthenticationSession();
    LoginProtocol loginProtocol = keycloakSession.getProvider(LoginProtocol.class, clientSession.getProtocol());
    if (loginProtocol.requireReauthentication(userSession, clientSession)) {
      context.attempted();
      return;
    }

    Map<String,String> userSessionNotes = userSession.getNotes();
    if (userSessionNotes.containsKey("IMPERSONATOR_ID")) {
      String impersonatorId = userSessionNotes.get("IMPERSONATOR_ID");
      ClientModel client = clientSession.getClient();
      Set<String> clientRoles = client.getRolesStream()
          .map(RoleModel::getName)
          .filter(name -> name.endsWith("Impersonator"))
          .collect(Collectors.toSet());
      UserModel impersonator = keycloakSession.userLocalStorage().getUserById(impersonatorId, realm);
      Set<String> impersonatorRoles = impersonator.getClientRoleMappingsStream(client)
          .map(RoleModel::getName)
          .filter(name -> name.endsWith("Impersonator"))
          .collect(Collectors.toSet());
      Set<String> roleIntersection = impersonatorRoles.stream()
          .filter(clientRoles::contains)
          .collect(Collectors.toSet());
      if (roleIntersection.isEmpty()) {
        LOG.infof("deny access to impersonator");
        context.failure(AuthenticationFlowError.CLIENT_DISABLED);
        return;
      }
      LOG.infof("grant access to impersonator");
      clientSession .setUserSessionNote("IMPERSONATOR_ROLES", String.join(",", impersonatorRoles));
    }

    clientSession.setAuthNote(AuthenticationManager.SSO_AUTH, "true");
    context.setUser(authResult.getUser());
    context.attachUserSession(userSession);
    context.success();
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
    return true;
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