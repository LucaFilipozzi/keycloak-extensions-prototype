// Copyright (C) 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.authentication.authenticators;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ImpersonationSessionNote;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.sessions.AuthenticationSessionModel;

public class ImpersonationPolicyEnforcer implements Authenticator {

  private static final Logger LOG = Logger.getLogger(ImpersonationPolicyEnforcer.class);

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    // check for a valid cookie
    KeycloakSession keycloakSession = context.getSession();
    RealmModel realm = context.getRealm();
    AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(keycloakSession, realm, true);
    if (authResult == null) {
      context.attempted();
      return;
    }

    // check whether re-authentication is required
    UserModel user = authResult.getUser();
    UserSessionModel userSession = authResult.getSession();
    AuthenticationSessionModel authSession = context.getAuthenticationSession();
    LoginProtocol loginProtocol = keycloakSession.getProvider(LoginProtocol.class, authSession.getProtocol());
    if (loginProtocol.requireReauthentication(userSession, authSession)) {
      context.attempted();
      return;
    }

    // enforce impersonation policy: impersonator must have assigned client impersonation role
    Map<String,String> userSessionNotes = userSession.getNotes();
    if (userSessionNotes.containsKey(ImpersonationSessionNote.IMPERSONATOR_ID.toString())) {
      // get the set of available client impersonator roles
      ClientModel client = authSession.getClient();
      Set<String> clientRoles = client.getRolesStream()
          .map(RoleModel::getName)
          .filter(name -> name.endsWith("Impersonator"))
          .collect(Collectors.toSet());

      // get the set of client impersonator roles assigned to the impersonator
      String impersonatorId = userSessionNotes.get(ImpersonationSessionNote.IMPERSONATOR_ID.toString());
      UserModel impersonator = keycloakSession.userLocalStorage().getUserById(realm, impersonatorId);
      Set<String> impersonatorRoles = impersonator.getClientRoleMappingsStream(client)
          .map(RoleModel::getName)
          .filter(name -> name.endsWith("Impersonator"))
          .collect(Collectors.toSet());

      // compute the intersection of the two sets
      Set<String> roleIntersection = impersonatorRoles.stream()
          .filter(clientRoles::contains)
          .collect(Collectors.toSet());

      // deny access if the intersection is empty
      if (roleIntersection.isEmpty()) {
        LOG.infof("deny access to impersonator user=%s client=%s impersonator=%s",
            user.getUsername(), client.getClientId(), impersonator.getUsername());
        Response response = context.form()
            .setError("impersonator access denied")
            .createErrorPage(Status.UNAUTHORIZED);
        context.forceChallenge(response);
        return;
      }

      // otherwise grant access
      String roles = String.join(",", roleIntersection);
      LOG.infof("grant access to impersonator user=%s client=%s impersonator=%s roles=%s",
          user.getUsername(), client.getClientId(), impersonator.getUsername(), roles);
      authSession .setUserSessionNote("IMPERSONATOR_ROLES", roles);
    }

    authSession.setAuthNote(AuthenticationManager.SSO_AUTH, "true");
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