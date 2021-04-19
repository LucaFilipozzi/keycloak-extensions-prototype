// Copyright (C) 2021 Luca Filipozzi. Some rights reserved.
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package com.github.lucafilipozzi.keycloak.protocol.util;

import java.util.UUID;

@SuppressWarnings("unused")
public class PrototypeAttributeResolver {

  public String resolveAttribute(String key) {
    return key + "." + UUID.randomUUID().toString();
  }
}