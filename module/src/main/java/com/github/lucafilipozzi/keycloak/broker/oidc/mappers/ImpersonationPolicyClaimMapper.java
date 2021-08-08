// Copyright 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.broker.oidc.mappers;

import com.github.lucafilipozzi.keycloak.broker.util.ImpersonatorPolicyUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * Impersonation policy decision point.
 */
public class ImpersonationPolicyClaimMapper extends AbstractClaimMapper {

  public static final String PROVIDER_ID = "oidc-impersonation-policy-claim-mapper";

  protected static final String[] COMPATIBLE_PROVIDERS = { KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID };

  private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

  private static final Logger LOG = Logger.getLogger(ImpersonationPolicyClaimMapper.class);

  @Override
  public String[] getCompatibleProviders() {
    return COMPATIBLE_PROVIDERS;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return Collections.emptyList();
  }

  @Override
  public String getDisplayCategory() {
    return "Role Importer";
  }

  @Override
  public String getDisplayType() {
    return "Impersonation Policy Claim Mapper";
  }

  @Override
  public String getHelpText() {
    return "implements impersonation policy decision point";
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
    return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
  }

  @Override
  public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
    LOG.trace("importing new user");
    upsertRoles(realm, user, context);
  }

  @Override
  public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
    LOG.trace("updating new user");
    upsertRoles(realm, user, context);
  }

  private void upsertRoles(RealmModel realm, UserModel user, BrokeredIdentityContext context) {
    LOG.trace("upserting roles");
    Set<String> assertedGroupMemberships;
    Object claimValue = getClaimValue(context, "groupMembership");
    if (claimValue instanceof List) {
      assertedGroupMemberships = ((List<?>) claimValue).stream().map(String.class::cast).collect(Collectors.toSet());
    } else {
      assertedGroupMemberships = Collections.emptySet();
    }
    ImpersonatorPolicyUtil.upsertRole(realm, user, assertedGroupMemberships);
  }
}