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
public class ImpersonationPolicyClaimRegexMapper extends AbstractClaimMapper {

  public static final String PROVIDER_ID = "oidc-impersonation-policy-claim-regex-mapper";

  public static final String OIDC_CLAIM_NAME = "oidc-claim-name";

  public static final String REGULAR_EXPRESSION = "regular-expression";

  protected static final String[] COMPATIBLE_PROVIDERS = { KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID };

  private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

  private static final Logger LOG = Logger.getLogger(ImpersonationPolicyClaimRegexMapper.class);

  private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static {
    ProviderConfigProperty oidcClaimNameConfigProperty = new ProviderConfigProperty();
    oidcClaimNameConfigProperty.setName(OIDC_CLAIM_NAME);
    oidcClaimNameConfigProperty.setLabel("OIDC claim name");
    oidcClaimNameConfigProperty.setHelpText("name of OIDC claim to search");
    oidcClaimNameConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(oidcClaimNameConfigProperty);

    ProviderConfigProperty regularExpressionConfigProperty = new ProviderConfigProperty();
    regularExpressionConfigProperty.setName(REGULAR_EXPRESSION);
    regularExpressionConfigProperty.setLabel("regular expression");
    regularExpressionConfigProperty.setHelpText("regular expression to apply to OIDC claim");
    regularExpressionConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(regularExpressionConfigProperty);
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
    return "Impersonation Policy Claim Regex Mapper";
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
    LOG.trace("import user");
    processUser(realm, user, mapper, context);
  }

  @Override
  public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapper, BrokeredIdentityContext context) {
    LOG.trace("update user");
    processUser(realm, user, mapper, context);
  }

  @SuppressWarnings("DuplicatedCode")
  private void processUser(RealmModel realm, UserModel user, IdentityProviderMapperModel mapper, BrokeredIdentityContext context) {
    LOG.trace("process user");
    String oidcClaimName = mapper.getConfig().getOrDefault(OIDC_CLAIM_NAME, "");
    Set<String> assertedValues;
    Object claimValue = getClaimValue(context, oidcClaimName);
    if (claimValue instanceof List) {
      assertedValues = ((List<?>) claimValue).stream().map(String.class::cast).collect(Collectors.toSet());
    } else {
      assertedValues = Collections.emptySet();
    }
    String regularExpression = mapper.getConfig().getOrDefault(REGULAR_EXPRESSION, "");
    ImpersonatorPolicyUtil.foo(realm, user, assertedValues, regularExpression);
  }
}