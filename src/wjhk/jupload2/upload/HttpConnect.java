//
// $Id$
//
// jupload - A file upload applet.
//
// Copyright 2007 The JUpload Team
//
// Created: 07.05.2007
// Creator: felfert
// Last modified: $Date$
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

package wjhk.jupload2.upload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class implements the task of connecting to a HTTP(S) url using a proxy.
 * 
 * @author felfert
 */
public class HttpConnect {

    private UploadPolicy uploadPolicy;

    /**
     * An implementation of {@link javax.net.ssl.X509TrustManager} which accepts
     * any certificate.
     */
    protected final class DummyTrustManager implements X509TrustManager {
        /**
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String)
         */
        @SuppressWarnings("unused")
        public void checkClientTrusted(@SuppressWarnings("unused")
        X509Certificate[] arg0, @SuppressWarnings("unused")
        String arg1) throws CertificateException {
            // Nothing to do.
        }

        /**
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String)
         */
        @SuppressWarnings("unused")
        public void checkServerTrusted(@SuppressWarnings("unused")
        X509Certificate[] chain, @SuppressWarnings("unused")
        String authType) throws CertificateException {
            // Nothing to do.
        }

        /**
         * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
         */
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * Helper function for perforing a proxy CONNECT request.
     * 
     * @param proxy The proxy to use.
     * @param host The destination's hostname.
     * @param port The destination's port
     * @return An established socket connection to the proxy.
     * @throws ConnectException if the proxy response code is not 200
     * @throws UnknownHostException
     * @throws IOException
     */
    private Socket HttpProxyConnect(Proxy proxy, String host, int port)
            throws UnknownHostException, IOException, ConnectException {
        InetSocketAddress sa = (InetSocketAddress) proxy.address();
        String phost = (sa.isUnresolved()) ? sa.getHostName() : sa.getAddress()
                .getHostAddress();
        int pport = sa.getPort();
        // 
        Socket proxysock = new Socket(phost, pport);
        String req = "CONNECT " + host + ":" + port + " HTTP/1.1\r\n\r\n";
        proxysock.getOutputStream().write(req.getBytes());
        BufferedReader proxyIn = new BufferedReader(new InputStreamReader(
                proxysock.getInputStream()));
        // We expect exactly one line: the proxy response
        String line = proxyIn.readLine();
        if (!line.matches("^HTTP/\\d\\.\\d\\s200\\s.*"))
            throw new ConnectException("Proxy response: " + line);
        this.uploadPolicy.displayDebug("Proxy response: " + line, 40);
        proxyIn.readLine(); // eat the header delimiter
        // we now are connected ...
        return proxysock;
    }

    /**
     * Connects to a given URL.
     * 
     * @param url The URL to connect to
     * @param proxy The proxy to be used, may be null if direct connection is
     *            needed
     * @return A socket, connected to the specified URL. May be null if an error
     *         occurs.
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     * @throws UnknownHostException
     * @throws ConnectException
     */
    public Socket Connect(URL url, Proxy proxy)
            throws NoSuchAlgorithmException, KeyManagementException,
            ConnectException, UnknownHostException, IOException {
        // Temporary socket for SOCKS support
        Socket tsock;
        Socket ret = null;
        String host = url.getHost();
        int port;
        boolean useProxy = ((proxy != null) && (proxy.type() != Proxy.Type.DIRECT));

        // Check if SSL connection is needed
        if (url.getProtocol().equals("https")) {
            port = (-1 == url.getPort()) ? 443 : url.getPort();
            SSLContext context = SSLContext.getInstance("SSL");
            // Allow all certificates
            context.init(null, new X509TrustManager[] {
                new DummyTrustManager()
            }, null);
            if (useProxy) {
                if (proxy.type() == Proxy.Type.HTTP) {
                    // First establish a CONNECT, then do a normal SSL
                    // thru that connection.
                    this.uploadPolicy.displayDebug(
                            "Using SSL socket, via HTTP proxy", 20);
                    ret = context.getSocketFactory().createSocket(
                            HttpProxyConnect(proxy, host, port), host, port,
                            true);
                } else if (proxy.type() == Proxy.Type.SOCKS) {
                    this.uploadPolicy.displayDebug(
                            "Using SSL socket, via SOCKS proxy", 20);
                    tsock = new Socket(proxy);
                    tsock.connect(new InetSocketAddress(host, port));
                    ret = context.getSocketFactory().createSocket(tsock, host,
                            port, true);
                } else
                    throw new ConnectException("Unkown proxy type "
                            + proxy.type());
            } else {
                // If port not specified then use default https port
                // 443.
                this.uploadPolicy.displayDebug(
                        "Using SSL socket, direct connection", 20);
                ret = context.getSocketFactory().createSocket(host, port);
            }
        } else {
            // If we are not in SSL, just use the old code.
            port = (-1 == url.getPort()) ? 80 : url.getPort();
            if (useProxy) {
                if (proxy.type() == Proxy.Type.HTTP) {
                    InetSocketAddress sa = (InetSocketAddress) proxy.address();
                    host = (sa.isUnresolved()) ? sa.getHostName() : sa
                            .getAddress().getHostAddress();
                    port = sa.getPort();
                    this.uploadPolicy.displayDebug(
                            "Using non SSL socket, proxy=" + host + ":" + port,
                            20);
                    ret = new Socket(host, port);
                } else if (proxy.type() == Proxy.Type.SOCKS) {
                    this.uploadPolicy.displayDebug(
                            "Using non SSL socket, via SOCKS proxy", 20);
                    tsock = new Socket(proxy);
                    tsock.connect(new InetSocketAddress(host, port));
                    ret = tsock;
                } else
                    throw new ConnectException("Unkown proxy type "
                            + proxy.type());
            } else {
                this.uploadPolicy.displayDebug(
                        "Using non SSL socket, direct connection", 20);
                ret = new Socket(host, port);
            }
        }
        return ret;
    }

    /**
     * Connects to a given URL automatically using a proxy.
     * 
     * @param url The URL to connect to
     * @return A socket, connected to the specified URL. May be null if an error
     *         occurs.
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws URISyntaxException
     */
    public Socket Connect(URL url) throws NoSuchAlgorithmException,
            KeyManagementException, ConnectException, UnknownHostException,
            IOException, URISyntaxException {
        Proxy proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
        return Connect(url, proxy);
    }

    /**
     * Creates a new instance.
     * 
     * @param policy The UploadPolicy to be used for logging.
     */
    public HttpConnect(UploadPolicy policy) {
        this.uploadPolicy = policy;
    }
}
