/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.client5.http.impl.routing;

import java.net.InetAddress;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.UnsupportedSchemeException;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.annotation.Immutable;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

/**
 * Default implementation of an {@link HttpRoutePlanner}. It will not make use of
 * any Java system properties, nor of system or browser proxy settings.
 *
 * @since 4.3
 */
@Immutable
public class DefaultRoutePlanner implements HttpRoutePlanner {

    private final SchemePortResolver schemePortResolver;

    public DefaultRoutePlanner(final SchemePortResolver schemePortResolver) {
        super();
        this.schemePortResolver = schemePortResolver != null ? schemePortResolver :
            DefaultSchemePortResolver.INSTANCE;
    }

    @Override
    public HttpRoute determineRoute(
            final HttpHost host,
            final HttpRequest request,
            final HttpContext context) throws HttpException {
        Args.notNull(request, "Request");
        if (host == null) {
            throw new ProtocolException("Target host is not specified");
        }
        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final RequestConfig config = clientContext.getRequestConfig();
        final InetAddress local = config.getLocalAddress();
        HttpHost proxy = config.getProxy();
        if (proxy == null) {
            proxy = determineProxy(host, request, context);
        }

        final HttpHost target;
        if (host.getPort() <= 0) {
            try {
                target = new HttpHost(
                        host.getHostName(),
                        this.schemePortResolver.resolve(host),
                        host.getSchemeName());
            } catch (final UnsupportedSchemeException ex) {
                throw new HttpException(ex.getMessage());
            }
        } else {
            target = host;
        }
        final boolean secure = target.getSchemeName().equalsIgnoreCase("https");
        if (proxy == null) {
            return new HttpRoute(target, local, secure);
        } else {
            return new HttpRoute(target, local, proxy, secure);
        }
    }

    /**
     * This implementation returns null.
     *
     * @throws HttpException may be thrown if overridden
     */
    protected HttpHost determineProxy(
            final HttpHost target,
            final HttpRequest request,
            final HttpContext context) throws HttpException {
        return null;
    }

}
