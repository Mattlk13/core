/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.environment.servlet.test.bootstrap.enhanced.shutdown;

import static org.jboss.weld.environment.servlet.test.util.Deployments.DEFAULT_WEB_XML;
import static org.jboss.weld.environment.servlet.test.util.Deployments.DEFAULT_WEB_XML_BODY;
import static org.jboss.weld.environment.servlet.test.util.Deployments.DEFAULT_WEB_XML_START;
import static org.jboss.weld.environment.servlet.test.util.Deployments.DEFAULT_WEB_XML_SUFFIX;
import static org.jboss.weld.environment.servlet.test.util.Deployments.toContextParam;
import static org.jboss.weld.environment.servlet.test.util.Deployments.toListener;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.test.util.ActionSequence;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 *
 * @author Martin Kouba
 * @see WELD-1808
 */
public class EnhancedListenerShutdownTestBase {

    protected static final String TEST = "test";
    protected static final String ASSERT = "assert";

    protected static final Asset TEST_WEB_XML = new StringAsset(DEFAULT_WEB_XML_START + DEFAULT_WEB_XML_BODY + toListener(TestListener.class.getName())
            + toContextParam("WELD_CONTEXT_ID_KEY", TEST) + DEFAULT_WEB_XML_SUFFIX);

    protected static final Asset ASSERT_WEB_XML = new StringAsset(DEFAULT_WEB_XML_START + DEFAULT_WEB_XML_BODY + toContextParam("WELD_CONTEXT_ID_KEY", ASSERT)
            + DEFAULT_WEB_XML_SUFFIX);

    public static WebArchive createTestArchive() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "app-test.war").addAsWebInfResource(new BeansXml(), "beans.xml").setWebXML(TEST_WEB_XML);
        war.addClasses(InitServlet.class, InfoClient.class, Foo.class, TestListener.class);
        return war;
    }

    public static WebArchive createAssertArchive() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "app-assert.war").addAsWebInfResource(new BeansXml(), "beans.xml").setWebXML(DEFAULT_WEB_XML);
        war.addClasses(InfoServlet.class, ActionSequence.class);
        return war;
    }

    @ArquillianResource
    Deployer deployer;

    /**
     * This is not a real test method.
     *
     * @see #testEnhancedListenerDoesNotDestroyWeldIfListenerRegistered(URL)
     */
    @Test
    @InSequence(1)
    public void deployArchives() {
        // In order to use @ArquillianResource URLs we need to deploy both test archives first
        deployer.deploy(TEST);
        deployer.deploy(ASSERT);
    }

    @Test
    @InSequence(2)
    public void testEnhancedListenerNotDestroyingWeldIfListenerRegistered(@ArquillianResource @OperateOnDeployment(TEST) URL testContext,
            @ArquillianResource @OperateOnDeployment(ASSERT) URL assertContext) throws IOException {

        // Init foo - set info archive deployment url
        WebClient webClient = new WebClient();
        webClient.setThrowExceptionOnFailingStatusCode(true);
        webClient.getPage(testContext + "init?url=" + URLEncoder.encode(assertContext.toExternalForm(), "UTF-8"));

        // Undeploy TEST
        deployer.undeploy(TEST);

        // Test that Foo is destroyed after TestListener is notified
        TextPage info = webClient.getPage(assertContext + "info?action=get");
        List<String> data = ActionSequence.buildFromCsvData(info.getContent()).getData();
        assertEquals(2, data.size());
        assertEquals(TestListener.class.getSimpleName(), data.get(0));
        assertEquals(Foo.class.getSimpleName(), data.get(1));

        // Undeploy ASSERT
        deployer.undeploy(ASSERT);
    }
}