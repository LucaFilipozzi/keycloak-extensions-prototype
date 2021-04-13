// Copyright (C) 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.protocol.oidc.mappers;

import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

public class PrototypeOIDCProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

  public static final String PROVIDER_ID = "prototype-oidc-protocol-mapper";

  private static final Logger LOG = Logger.getLogger(PrototypeOIDCProtocolMapper.class);

  private static final List<ProviderConfigProperty> PROVIDER_CONFIG_PROPERTIES = new ArrayList<>();

  static {
    ProviderConfigProperty providerConfigProperty;
    providerConfigProperty = new ProviderConfigProperty();
    providerConfigProperty.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
    providerConfigProperty.setLabel(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_LABEL);
    providerConfigProperty.setHelpText(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_HELP_TEXT);
    providerConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    PROVIDER_CONFIG_PROPERTIES.add(providerConfigProperty);
  }

  @Override
  public String getDisplayCategory() {
    return "prototype mapper";
  }

  @Override
  public String getDisplayType() {
    return "Prototype OIDC Protocol Mapper";
  }

  @Override
  public String getHelpText() {
    return "this is a prototype mapper for the OIDC protocol";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return PROVIDER_CONFIG_PROPERTIES;
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionContext) {
    LOG.infof("setClaim");
    // TODO this is where the magic happens
  }
}