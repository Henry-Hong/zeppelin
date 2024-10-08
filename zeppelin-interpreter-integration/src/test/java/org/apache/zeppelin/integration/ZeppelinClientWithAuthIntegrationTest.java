/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zeppelin.integration;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.zeppelin.MiniZeppelinServer;
import org.apache.zeppelin.client.ClientConfig;
import org.apache.zeppelin.client.ZeppelinClient;
import org.apache.zeppelin.conf.ZeppelinConfiguration;
import org.apache.zeppelin.rest.AbstractTestRestApi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ZeppelinClientWithAuthIntegrationTest extends AbstractTestRestApi {

  private static final Logger LOG = LoggerFactory.getLogger(ZeppelinClientWithAuthIntegrationTest.class);

  private static ClientConfig clientConfig;
  private static ZeppelinClient zeppelinClient;
  private static MiniZeppelinServer zepServer;

  @BeforeAll
  static void init() throws Exception {
    zepServer = new MiniZeppelinServer(ZeppelinClientWithAuthIntegrationTest.class.getSimpleName());
    zepServer.addInterpreter("md");
    zepServer.addConfigFile("shiro.ini", AbstractTestRestApi.ZEPPELIN_SHIRO);
    zepServer.copyBinDir();
    zepServer.getZeppelinConfiguration().setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_HELIUM_REGISTRY.getVarName(),
        "helium");
    zepServer.getZeppelinConfiguration().setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_ALLOWED_ORIGINS.getVarName(), "*");
    zepServer.start();
    clientConfig = new ClientConfig("http://localhost:" + zepServer.getZeppelinConfiguration().getServerPort());
    zeppelinClient = new ZeppelinClient(clientConfig);
  }

  @AfterAll
  static void destroy() throws Exception {
    zepServer.destroy();
  }

  @BeforeEach
  void setup() {
    zConf = zepServer.getZeppelinConfiguration();
  }

  @Test
  void testZeppelinVersion() throws Exception {
    String version = zeppelinClient.getVersion();
    LOG.info("Zeppelin version: " + version);
    assertNotNull(version);
  }

  @Test
  void testCreateNoteWithoutLogin() throws Exception {
    try {
      zeppelinClient.createNote("/note_1");
      fail("Should fail due to not login");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("login first"), e.getMessage());
    }
  }

  @Test
  void testCreateNoteAfterLogin() throws Exception {
    zeppelinClient.login("admin", "password1");
    String response = zeppelinClient.createNote("/note_2");
    assertNotNull(response);
  }

  @Test
  void testLoginFailed() throws Exception {
    // wrong password
    try {
      zeppelinClient.login("admin", "invalid_password");
      fail("Should fail to login");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Forbidden"), e.getMessage());
    }

    // wrong username
    try {
      zeppelinClient.login("invalid_user", "password1");
      fail("Should fail to login");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Forbidden"), e.getMessage());
    }
  }
}

