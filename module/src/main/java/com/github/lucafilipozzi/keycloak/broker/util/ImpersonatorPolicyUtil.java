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
import org.keycloak.models.RealmModel;
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

  public static void assignClientRolesToUser(RealmModel realm, UserModel user, Set<String> assertedValues, String clientRoleAttributeName) {
    LOG.trace("assign client roles to user");
    realm.getClientsStream().forEach(client -> client.getRolesStream()
      .filter(clientRole -> clientRole.getName().endsWith(CLIENT_ROLE_SUFFIX))
      .forEach(clientRole ->
        {
          Set<String> requiredValues = clientRole.getAttributeStream(clientRoleAttributeName).collect(Collectors.toSet());
          if (Sets.intersection(assertedValues, requiredValues).isEmpty()) {
            if (user.hasRole(clientRole)) {
              LOG.infof("delete mapping client=%s role=%s user=%s", client.getClientId(), clientRole.getName(), user.getUsername());
              user.deleteRoleMapping(clientRole);
            }
          } else {
            if (!user.hasRole(clientRole)) {
              LOG.infof("insert mapping client=%s role=%s user=%s", client.getClientId(), clientRole.getName(), user.getUsername());
              user.grantRole(clientRole);
            }
          }
        }
      )
    );
  }
}