/**
 * Azure Active Directory Authentication Plugin for SonarQube
 * <p>
 * Copyright (c) 2016 Microsoft Corporation
 * All rights reserved.
 * <p>
 * The MIT License (MIT)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.almrangers.auth.aad;

import org.junit.Test;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.internal.MapSettings;

import static org.almrangers.auth.aad.AadSettings.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;

public class AadSettingsTest {
  MapSettings settings = new MapSettings(new PropertyDefinitions(AadSettings.authenticationProperties()));

  AadSettings underTest = new AadSettings(settings.asConfig());

  @Test
  public void is_enabled() {
    setSettings(true);
    assertThat(underTest.isEnabled()).isTrue();
  }

  @Test
  public void is_disabled() {
    setSettings(false);
    assertThat(underTest.isEnabled()).isFalse();
  }

  private void setSettings(boolean enabled) {
    settings.setProperty("sonar.auth.aad.clientId.secured", "id");
    settings.setProperty("sonar.auth.aad.clientSecret.secured", "secret");
    settings.setProperty("sonar.auth.aad.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);
    settings.setProperty("sonar.auth.aad.enabled", enabled);
  }

  @Test
  public void return_authorization_url_for_single_tenant_azureAd_app() {
    settings.setProperty("sonar.auth.aad.multiTenant", "false");
    settings.setProperty("sonar.auth.aad.tenantId", "tenantId");
    assertThat(underTest.authorizationUrl()).isEqualTo("https://login.microsoftonline.com/tenantId/oauth2/authorize");
  }

  @Test
  public void return_authorization_url_for_multi_tenant_azureAd_app() {
    settings.setProperty("sonar.auth.aad.multiTenant", "true");
    assertThat(underTest.authorizationUrl()).isEqualTo("https://login.microsoftonline.com/common/oauth2/authorize");
  }

  @Test
  public void return_correct_urls() {
    //Azure Default "Global"
    settings.setProperty("sonar.auth.aad.directoryLocation", DIRECTORY_LOC_GLOBAL);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(underTest.authorizationUrl().startsWith("https://login.microsoftonline.com")).isTrue();
    softly.assertThat(underTest.getGraphURL().startsWith("https://graph.microsoft.com")).isTrue();

    //Azure US Gov
    settings.setProperty("sonar.auth.aad.directoryLocation", DIRECTORY_LOC_USGOV);
    softly.assertThat(underTest.authorizationUrl().startsWith("https://login.microsoftonline.us")).isTrue();
    softly.assertThat(underTest.getGraphURL().startsWith("https://graph.microsoft.com")).isTrue();

    //Azure Germany
    settings.setProperty("sonar.auth.aad.directoryLocation", DIRECTORY_LOC_DE);
    softly.assertThat(underTest.authorizationUrl().startsWith("https://login.microsoftonline.de")).isTrue();
    softly.assertThat(underTest.getGraphURL().startsWith("https://graph.microsoft.de")).isTrue();

    //Azure China
    settings.setProperty("sonar.auth.aad.directoryLocation", DIRECTORY_LOC_CN);
    softly.assertThat(underTest.authorizationUrl().startsWith("https://login.chinacloudapi.cn")).isTrue();
    softly.assertThat(underTest.getGraphURL().startsWith("https://microsoftgraph.chinacloudapi.cn")).isTrue();
    softly.assertAll();
  }

  @Test
  public void is_enabled_always_return_false_when_client_id_is_null() {
    settings.setProperty("sonar.auth.aad.enabled", true);
    settings.setProperty("sonar.auth.aad.clientId.secured", (String) null);
    settings.setProperty("sonar.auth.aad.clientSecret.secured", "secret");
    settings.setProperty("sonar.auth.aad.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);

    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void is_enabled_always_return_false_when_client_secret_is_null() {
    settings.setProperty("sonar.auth.aad.enabled", true);
    settings.setProperty("sonar.auth.aad.clientId.secured", "id");
    settings.setProperty("sonar.auth.aad.clientSecret.secured", (String) null);
    settings.setProperty("sonar.auth.aad.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);

    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void default_login_strategy_is_unique_login() {
    assertThat(underTest.loginStrategy()).isEqualTo(AadSettings.LOGIN_STRATEGY_UNIQUE);
  }

  @Test
  public void return_client_id() {
    settings.setProperty("sonar.auth.aad.clientId.secured", "id");
    assertThat(underTest.clientId()).isEqualTo("id");
  }

  @Test
  public void return_client_secret() {
    settings.setProperty("sonar.auth.aad.clientSecret.secured", "secret");
    assertThat(underTest.clientSecret()).isEqualTo("secret");
  }

  @Test
  public void allow_users_to_sign_up() {
    settings.setProperty("sonar.auth.aad.allowUsersToSignUp", "true");
    assertThat(underTest.allowUsersToSignUp()).isTrue();
  }

  @Test
  public void prohibit_users_to_sign_up() {
    settings.setProperty("sonar.auth.aad.allowUsersToSignUp", "false");
    assertThat(underTest.allowUsersToSignUp()).isFalse();
  }
  
  @Test
  public void definitions() {
    assertThat(AadSettings.authenticationProperties()).hasSize(7);
  }
  
  @Test
  public void groupProperties() {
    assertThat(AadSettings.groupProperties()).hasSize(1);
  }

  @Test
  public void locationProperties() {
    assertThat(AadSettings.locationProperties()).hasSize(1);
  }
  
}
