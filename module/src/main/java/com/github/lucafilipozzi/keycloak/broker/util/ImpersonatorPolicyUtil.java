// Copyright 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.broker.util;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

/**
 * Utilities for impersonation policy decision points.
 */
public final class ImpersonatorPolicyUtil {

  public static final String CLIENT_ROLE_SUFFIX = "Impersonator";

  private static final Logger LOG = Logger.getLogger(ImpersonatorPolicyUtil.class);

  private ImpersonatorPolicyUtil() {
    throw new UnsupportedOperationException();
  }

  public static void processUserRoleAssignments(RealmModel realm, UserModel user, Set<String> assertedGroupMemberships, String clientRoleAttributeName) {
    realm.getClientsStream().forEach(client -> client
      .getRolesStream()
      .filter(clientRole -> clientRole.getName().endsWith(CLIENT_ROLE_SUFFIX))
      .forEach(clientRole ->
        {
          if (clientRole.getAttributeStream(clientRoleAttributeName).anyMatch(assertedGroupMemberships::contains)) {
            LOG.infof("delete mapping client=%s role=%s user=%s", client.getClientId(), clientRole.getName(), user.getUsername());
            user.deleteRoleMapping(clientRole);
          } else {
            LOG.infof("insert mapping client=%s role=%s user=%s", client.getClientId(), clientRole.getName(), user.getUsername());
            user.grantRole(clientRole);
          }
        }
      )
    );
  }

  public static void processUserRoleAssignmentsOld(RealmModel realm, UserModel user, Set<String> assertedGroupMemberships, String clientRoleAttributeName) {
    realm.getClientsStream().forEach(client ->
        client.getRolesStream()
          .filter(clientRole -> clientRole.getName().endsWith(CLIENT_ROLE_SUFFIX))
          .forEach(clientRole -> processUserRoleAssignments(client, clientRole, user, assertedGroupMemberships, clientRoleAttributeName))
    );
  }

  private static void processUserRoleAssignments(ClientModel client, RoleModel clientRole, UserModel user, Set<String> assertedGroupMemberships, String clientRoleAttributeName) {
    LOG.info("process user role assignments (private)");
    LOG.info(clientRoleAttributeName);
    Set<String> requiredGroupMemberships = clientRole.getAttributeStream(clientRoleAttributeName).collect(Collectors.toSet());
    LOG.info(requiredGroupMemberships);
    if (Sets.intersection(assertedGroupMemberships, requiredGroupMemberships).isEmpty()) {
      LOG.infof("delete mapping client=%s role=%s user=%s", client.getClientId(), clientRole.getName(), user.getUsername());
      user.deleteRoleMapping(clientRole);
    } else {
      LOG.infof("insert mapping client=%s role=%s user=%s", client.getClientId(), clientRole.getName(), user.getUsername());
      user.grantRole(clientRole);
    }
  }
}