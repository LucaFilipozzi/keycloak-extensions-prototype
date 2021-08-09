// Copyright 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.broker.saml.mappers;

import com.github.lucafilipozzi.keycloak.broker.util.ImpersonatorPolicyUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.saml.SAMLEndpoint;
import org.keycloak.broker.saml.SAMLIdentityProviderFactory;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * Impersonation policy decision point.
 */
public class ImpersonationPolicyAttributeMapper extends AbstractIdentityProviderMapper {

  public static final String PROVIDER_ID = "saml-impersonation-policy-attribute-mapper";

  public static final String SAML_ATTRIBUTE_NAME = "saml-attribute-name";

  public static final String REGULAR_EXPRESSION = "regular-expression";

  protected static final String[] COMPATIBLE_PROVIDERS = {SAMLIdentityProviderFactory.PROVIDER_ID};

  private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

  private static final Logger LOG = Logger.getLogger(ImpersonationPolicyAttributeMapper.class);

  private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static {
    ProviderConfigProperty samlAttributeNameConfigProperty = new ProviderConfigProperty();
    samlAttributeNameConfigProperty.setName(SAML_ATTRIBUTE_NAME);
    samlAttributeNameConfigProperty.setLabel("SAML attribute name");
    samlAttributeNameConfigProperty.setHelpText("name of SAML attribute to search (friendly or otherwise)");
    samlAttributeNameConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(samlAttributeNameConfigProperty);

    ProviderConfigProperty regularExpressionConfigProperty = new ProviderConfigProperty();
    regularExpressionConfigProperty.setName(REGULAR_EXPRESSION);
    regularExpressionConfigProperty.setLabel("regular expression");
    regularExpressionConfigProperty.setHelpText("regular expression to apply to SAML attribute; must specify two named-capturing groups: client and role");
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
    return "Impersonation Policy Attribute Mapper";
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

  private void processUser(RealmModel realm, UserModel user, IdentityProviderMapperModel mapper, BrokeredIdentityContext context) {
    LOG.trace("process user");
    String samlAttributeName = mapper.getConfig().getOrDefault(SAML_ATTRIBUTE_NAME, "");
    AssertionType assertion = (AssertionType) context.getContextData().get(SAMLEndpoint.SAML_ASSERTION);
    Set<String> assertedValues = assertion.getAttributeStatements().stream()
        .flatMap(statement -> statement.getAttributes().stream())
        .filter(choice -> choice.getAttribute().getFriendlyName().equals(samlAttributeName) || choice.getAttribute().getName().equals(samlAttributeName))
        .flatMap(choice -> choice.getAttribute().getAttributeValue().stream())
        .map(Object::toString)
        .collect(Collectors.toSet());
    String regularExpression = mapper.getConfig().getOrDefault(REGULAR_EXPRESSION, "");
    ImpersonatorPolicyUtil.adjustUserClientRoleAssignments(realm, user, assertedValues, regularExpression);
  }
}