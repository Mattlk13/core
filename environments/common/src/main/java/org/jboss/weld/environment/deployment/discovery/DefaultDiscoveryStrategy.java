/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.deployment.discovery;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * The implementation does not support bean-discovery-mode="annotated" and is suitable for Java SE applications with no bytecode scanning library on the
 * classpath.
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
public class DefaultDiscoveryStrategy extends AbstractDiscoveryStrategy {

    public DefaultDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap) {
        super(resourceLoader, bootstrap);
        registerHandler(new FileSystemBeanArchiveHandler());
    }

    @Override
    protected WeldBeanDeploymentArchive processAnnotatedDiscovery(BeanArchiveBuilder builder) {
        throw CommonLogger.LOG.annotatedBeanDiscoveryNotSupported();
    }

}
