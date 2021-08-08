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

  public static void upsertRole(RealmModel realm, UserModel user, Set<String> assertedGroupMemberships) {
    realm.getClientsStream().forEach(client ->
        client.getRolesStream()
          .filter(clientRole -> clientRole.getName().endsWith(CLIENT_ROLE_SUFFIX))
          .forEach(clientRole -> upsertRole(client, clientRole, user, assertedGroupMemberships))
    );
  }

  private static void upsertRole(ClientModel client, RoleModel clientRole, UserModel user, Set<String> assertedGroupMemberships) {
    Set<String> requiredGroupMemberships = clientRole.getAttributeStream("groupMembership").collect(Collectors.toSet());
    if (Sets.intersection(assertedGroupMemberships, requiredGroupMemberships).isEmpty()) {
      LOG.infof("delete mapping client=%s role=%s user=%s", client.getClientId(), clientRole.getName(), user.getUsername());
      user.deleteRoleMapping(clientRole);
    } else {
      LOG.infof("insert mapping client=%s role=%s user=%s", client.getClientId(), clientRole.getName(), user.getUsername());
      user.grantRole(clientRole);
    }
  }
}