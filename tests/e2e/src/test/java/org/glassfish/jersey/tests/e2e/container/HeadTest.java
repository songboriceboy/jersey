/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.jersey.tests.e2e.container;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.simple.SimpleTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import jersey.repackaged.com.google.common.base.Function;
import jersey.repackaged.com.google.common.collect.Lists;

/**
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class HeadTest extends JerseyContainerTest {

    private static final List<TestContainerFactory> FACTORIES = Arrays.asList(
            new GrizzlyTestContainerFactory(),
            new InMemoryTestContainerFactory(),
            new SimpleTestContainerFactory(),
            new JettyTestContainerFactory());

    @Parameterized.Parameters(name = "{0}")
    public static Collection<TestContainerFactory[]> parameters() throws Exception {
        return Lists.transform(FACTORIES, new Function<TestContainerFactory, TestContainerFactory[]>() {

            @Override
            public TestContainerFactory[] apply(final TestContainerFactory input) {
                return new TestContainerFactory[]{input};
            }
        });
    }

    @Path("/")
    public static class Resource {

        @Path("string")
        @GET
        public String getString() {
            return "GET";
        }

        @Path("byte")
        @GET
        public byte[] getByte() {
            return "GET".getBytes();
        }

        @Path("ByteArrayInputStream")
        @GET
        public InputStream getInputStream() {
            return new ByteArrayInputStream("GET".getBytes());
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Test
    public void testHeadString() {
        _testHead("string", MediaType.TEXT_PLAIN_TYPE);
    }

    @Test
    public void testHeadByte() {
        _testHead("byte", MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    @Test
    public void testHeadByteArrayInputStream() {
        _testHead("ByteArrayInputStream", MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    private void _testHead(final String path, final MediaType mediaType) {
        final Response response = target(path).request(mediaType).head();
        assertThat(response.getStatus(), is(200));

        final String lengthStr = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
        assertThat(lengthStr, notNullValue());
        assertThat(Integer.parseInt(lengthStr), is(3));
        assertThat(response.getMediaType(), is(mediaType));
        assertFalse(response.hasEntity());
    }
}
