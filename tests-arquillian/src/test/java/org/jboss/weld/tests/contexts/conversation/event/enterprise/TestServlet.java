/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.contexts.conversation.event.enterprise;

import java.io.IOException;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.weld.test.util.ActionSequence;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/*")
public class TestServlet extends HttpServlet {

    @Inject
    ConversationBean bean;

    @Inject
    Conversation conversation;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        if ("/begin".equals(req.getPathInfo())) {
            conversation.begin();
            // Force initialization of the lazy conversation context
            bean.ping();
            resp.getWriter().println(conversation.getId());
        } else if ("/end".equals(req.getPathInfo())) {
            ActionSequence.reset();
            conversation.end();
            resp.getWriter().println("OK");
        } else {
            resp.getWriter().println(ActionSequence.getSequence().dataToCsv());
        }
    }
}
