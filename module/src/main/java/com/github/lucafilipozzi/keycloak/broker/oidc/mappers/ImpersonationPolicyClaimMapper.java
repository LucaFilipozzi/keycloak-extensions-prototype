// Copyright 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.broker.oidc.mappers;

import com.github.lucafilipozzi.keycloak.broker.util.ImpersonatorPolicyUtil;
import java.util.ArrayList;
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

  public static final String OIDC_CLAIM_NAME = "oidc-claim-name";

  public static final String CLIENT_ROLE_ATTRIBUTE_NAME = "client-role-attribute-name";

  protected static final String[] COMPATIBLE_PROVIDERS = { KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID };

  private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

  private static final Logger LOG = Logger.getLogger(ImpersonationPolicyClaimMapper.class);

  private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static {
    ProviderConfigProperty oidcClaimNameConfigProperty = new ProviderConfigProperty();
    oidcClaimNameConfigProperty.setName(OIDC_CLAIM_NAME);
    oidcClaimNameConfigProperty.setLabel("OIDC claim name");
    oidcClaimNameConfigProperty.setHelpText("name of OIDC claim to search");
    oidcClaimNameConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(oidcClaimNameConfigProperty);

    ProviderConfigProperty clientRoleAttributeNameConfigProperty = new ProviderConfigProperty();
    clientRoleAttributeNameConfigProperty.setName(CLIENT_ROLE_ATTRIBUTE_NAME);
    clientRoleAttributeNameConfigProperty.setLabel("client role attribute name");
    clientRoleAttributeNameConfigProperty.setHelpText("name of client role attribute to search");
    clientRoleAttributeNameConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(clientRoleAttributeNameConfigProperty);
  }

  @Override
  public String[] getCompatibleProviders() {
    return COMPATIBLE_PROVIDERS;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
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
  public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapper, BrokeredIdentityContext context) {
    LOG.trace("import new user");
    processUserClientRoleAssignments(realm, user, mapper, context);
  }

  @Override
  public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapper, BrokeredIdentityContext context) {
    LOG.trace("update new user");
    processUserClientRoleAssignments(realm, user, mapper, context);
  }

  private void processUserClientRoleAssignments(RealmModel realm, UserModel user, IdentityProviderMapperModel mapper, BrokeredIdentityContext context) {
    LOG.trace("process user role assignments");
    String oidcClaimName = mapper.getConfig().getOrDefault(OIDC_CLAIM_NAME, "");
    Set<String> assertedGroupMemberships;
    Object claimValue = getClaimValue(context, oidcClaimName);
    if (claimValue instanceof List) {
      assertedGroupMemberships = ((List<?>) claimValue).stream().map(String.class::cast).collect(Collectors.toSet());
    } else {
      assertedGroupMemberships = Collections.emptySet();
    }
    String clientRoleAttributeName = mapper.getConfig().getOrDefault(CLIENT_ROLE_ATTRIBUTE_NAME, "");
    ImpersonatorPolicyUtil.processUserRoleAssignments(realm, user, assertedGroupMemberships, clientRoleAttributeName);
  }
}