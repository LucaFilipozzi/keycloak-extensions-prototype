// Copyright 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.broker.util;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  public static void adjustUserClientRoleAssignments(RealmModel realm, UserModel user, Set<String> assertedValues, String regularExpression) {
    LOG.trace("adjust user client role assignments");
    Pattern pattern = Pattern.compile(regularExpression);
    Set<RoleModel> wantRoles = assertedValues.stream()
        .map(pattern::matcher)
        .filter(Matcher::matches)
        .flatMap(matcher ->
            realm.getClientsStream()
                .filter(client -> client.getClientId().equals(matcher.group(1)))
                .flatMap(ClientModel::getRolesStream)
                .filter(clientRole -> clientRole.getName().endsWith(CLIENT_ROLE_SUFFIX))
                .filter(clientRole -> clientRole.getName().equals(matcher.group(2))) )
        .collect(Collectors.toSet());
    Set<RoleModel> haveRoles = user.getRoleMappingsStream()
        .filter(RoleModel::isClientRole)
        .filter(clientRole -> clientRole.getName().endsWith(CLIENT_ROLE_SUFFIX))
        .collect(Collectors.toSet());
    Sets.difference(wantRoles, haveRoles).forEach(user::grantRole);
    Sets.difference(haveRoles, wantRoles).forEach(user::deleteRoleMapping);
  }
}