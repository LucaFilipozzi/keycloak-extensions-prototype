[![license][license-img]][license-url]
[![latest tag][latest-tag-img]][latest-tag-url]
[![latest release][latest-release-img]][latest-release-url]

[![build][build-img]][build-url]
[![analyze][analyze-img]][analyze-url]
[![dependabot][dependabot-img]][dependabot-url]

[![languages][languages-img]][languages-url]
[![alerts][alerts-img]][alerts-url]
[![code quality][code-quality-img]][code-quality-url]

[![maintainability][maintainability-img]][maintainability-url]
[![technical debt][technical-debt-img]][technical-debt-url]
[![vulnerabilities][vulnerabilities-img]][vulnerabilities-url]

# keycloak-extensions-prototype

prototype of [keycloak][keycloak] extensions

## quick start

To use this project:

1. build the EAR
2. deploy the EAR into `/path/to/keycloak/standalone/deployments/`
3. use the authenticators and mappers in flows and ampping rules

## classes

### authenticators

The following authenticators are available:

* PrototypeBrowserAuthenticator
* PrototypeFirstBrokerAuthenticator
* PrototypePostBrokerAuthenticator

There's really no difference between them... I just wanted separate classes so that they are easily
differentiable in server logs.

### mappers

The following mappers are available.

* PrototypeOIDCProtocolMapper
* PrototypeSAMLProtocolMapper

One is for OIDC and the other is for SAML. :)

## development

### classes

1. create new authenticator or mapper classes
   1. authenticators go in `module/src/main/java/com/github/lucafilipozzi/keycloak/authentication/authenticators/`
   2. mappers go in `module/src/main/java/com/github/lucafilipozzi/keycloak/protocol/[oidc,saml]/mappers/`
2. register the classes as follows:
   1. authenticators go in `bundle/src/main/application/META-INF/services/org.keycloak.authantication.AuthenticatorFactory`
   2. mappers go in `bundle/src/main/application/META-INF/services/org.keycloak.protocol.ProtocolMapper`

### dependencies

1. define dependencies in `pom.xml` and/or `module/pom.xml`
2. register the dependencies by adding them to `bundle/src/main/application/META-INF/jboss-deployment-structure.xml`

Note that it is not necessary to modify keycloak's `standalone.xml` configuration: just deploy the
EAR into `/path/to/keycloak/standalone/deployments`.

Note that an EAR is used so that any necessary 3rd party JARs may be bundled into the EAR.

---
Copyright (C) 2021 Luca Filipozzi. Some rights reserved.

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at https://mozilla.org/MPL/2.0/.

[keycloak]: https://keycloak.org/

[latest-release-img]: https://badgen.net/github/release/LucaFilipozzi/keycloak-extensions-prototype?icon=github&label=latest%20release
[latest-release-url]: https://github.com/LucaFilipozzi/keycloak-extensions-prototype/releases/latest
[latest-tag-img]: https://badgen.net/github/tag/LucaFilipozzi/keycloak-extensions-prototype?icon=github
[latest-tag-url]: https://github.com/LucaFilipozzi/keycloak-extensions-prototype/tags
[license-img]: https://badgen.net/github/license/LucaFilipozzi/keycloak-extensions-prototype?icon=github
[license-url]: https://github.com/LucaFilipozzi/keycloak-extensions-prototype/blob/main/LICENSE.md

[analyze-img]: https://github.com/LucaFilipozzi/keycloak-extensions-prototype/actions/workflows/analyze.yml/badge.svg
[analyze-url]: https://github.com/LucaFilipozzi/keycloak-extensions-prototype/actions/workflows/analyze.yml
[build-img]: https://github.com/LucaFilipozzi/keycloak-extensions-prototype/actions/workflows/build.yml/badge.svg
[build-url]: https://github.com/LucaFilipozzi/keycloak-extensions-prototype/actions/workflows/build.yml
[dependabot-img]: https://badgen.net/github/dependabot/LucaFilipozzi/keycloak-extensions-prototype?icon=dependabot
[dependabot-url]: https://github.com/LucaFilipozzi/keycloak-extensions-prototype/network/dependencies

[languages-img]: https://badgen.net/lgtm/langs/g/LucaFilipozzi/keycloak-extensions-prototype?icon=lgtm
[languages-url]: https://lgtm.com/projects/g/LucaFilipozzi/keycloak-extensions-prototype/logs/languages/lang:java
[alerts-img]: https://badgen.net/lgtm/alerts/g/LucaFilipozzi/keycloak-extensions-prototype/java?icon=lgtm
[alerts-url]: https://lgtm.com/projects/g/LucaFilipozzi/keycloak-extensions-prototype/alerts
[code-quality-img]: https://badgen.net/lgtm/grade/g/LucaFilipozzi/keycloak-extensions-prototype/java?icon=lgtm
[code-quality-url]: https://lgtm.com/projects/g/LucaFilipozzi/keycloak-extensions-prototype/context:java

[maintainability-img]: https://badgen.net/codeclimate/maintainability/LucaFilipozzi/keycloak-extensions-prototype?icon=codeclimate
[maintainability-url]: https://codeclimate.com/github/LucaFilipozzi/keycloak-extensions-prototype/maintainability
[technical-debt-img]: https://badgen.net/codeclimate/tech-debt/LucaFilipozzi/keycloak-extensions-prototype?icon=codeclimate
[technical-debt-url]: https://codeclimate.com/github/LucaFilipozzi/keycloak-extensions-prototype/maintainability
[vulnerabilities-img]: https://badgen.net/snyk/LucaFilipozzi/keycloak-extensions-prototype/main/pom.xml
[vulnerabilities-url]: https://snyk.io/test/github/lucafilipozzi/keycloak-extensions-prototype?targetFile=pom.xml
