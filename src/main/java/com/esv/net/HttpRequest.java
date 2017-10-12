/* 
 * Copyright © 2017-2017 Elton Santos Vianna. Distributed under GNU General Public License v3.0.
 */
package com.esv.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import com.esv.utile.utils.IOUtils;

/**
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 18/09/2017
 */
public final class HttpRequest {
    
    private static final ThreadLocal<HttpRequest> currentRequest = new ThreadLocal<>();
    
    private final String requestId;
    private transient final Socket socket;
    private final String requestMethod;
    private final String requestLine;
    private final String requestURI;
    private final String pathInfo;
    private final int pathLevel;
    private final Map<String, String> headers;
    private final String accept;
    private transient final OutputStream outputStream;
    private final boolean restRequest;

    /**
     * 
     * @param socket
     * @throws RuntimeException
     */
    private HttpRequest(final Socket socket) throws RuntimeException  {
        this.requestId = UUID.randomUUID().toString();
        this.socket = socket;
        try {
            final BufferedReader reader = IOUtils.toBufferedReader(socket.getInputStream());
            this.requestLine = reader.readLine();
            final StringTokenizer tokenizedLine = new StringTokenizer(requestLine);
            this.requestMethod = tokenizedLine.nextToken();
            this.requestURI = tokenizedLine.nextToken();
            final int idx = requestURI.indexOf('?');
            this.pathInfo = idx == -1 ? requestURI : requestURI.substring(0, idx);
            this.pathLevel = (int) this.pathInfo.chars().filter(c -> c == '/').count();
            this.headers = HttpRequest.getHeaders(reader);
            this.accept = headers.get("Accept");
            this.restRequest = null != accept && accept.startsWith("application/json");
            this.outputStream = socket.getOutputStream();
            // set this new request object as thread local
            HttpRequest.currentRequest.set(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /**
     * @param reader
     * @return
     * @throws IOException
     */
    private static Map<String, String> getHeaders(final BufferedReader reader) throws IOException {
        final Map<String, String> headers = new HashMap<>();
        String header = reader.readLine();
        while (null != header && header.length() > 0) {
            final int idx = header.indexOf(":");
            if (idx == -1) {
                break;
            }
            final String key = header.substring(0, idx).trim();
            final String value = header.substring(idx + 1, header.length()).trim();
            headers.put(key, value);
            header = reader.readLine();
        }
        return headers;
    }

    
    /**
     * 
     * @param socket
     * @return
     */
    public static HttpRequest newInstance(final Socket socket) {
        return new HttpRequest(socket);
    }
    
    
    /**
     * @return the currentrequest
     */
    public static HttpRequest getCurrentRrequest() {
        return HttpRequest.currentRequest.get();
    }

    /**
     * {@link Socket#getChannel()}
     */
    public SocketChannel getChannel() {
        return socket.getChannel();
    }

    /**
     * {@link Socket#getInetAddress()}
     */
    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    /**
     * {@link Socket#getInputStream()}
     */
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    /**
     * {@link Socket#getKeepAlive()}
     */
    public boolean getKeepAlive() throws SocketException {
        return socket.getKeepAlive();
    }

    /**
     * {@link Socket#getLocalAddress()}
     */
    public InetAddress getLocalAddress() {
        return socket.getLocalAddress();
    }

    /**
     * {@link Socket#getLocalPort()}
     */
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    /**
     * {@link Socket#getPort()}
     */
    public int getPort() {
        return socket.getPort();
    }

    /**
     * {@link Socket#isBound()}
     */
    public boolean isBound() {
        return socket.isBound();
    }

    /**
     * {@link Socket#isClosed()}
     */
    public boolean isClosed() {
        return socket.isClosed();
    }

    /**
     * {@link Socket#isClosed()}
     */
    public boolean isConnected() {
        return socket.isConnected();
    }
    
    /**
     * @return
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * {@link Socket#isClosed()}
     */
    public Socket getSocket() {
        return socket;
    }
    
    /**
     * @return
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * @return
     */
    public String getRequestLine() {
        return requestLine;
    }

    /**
     * @return
     */
    public String getRequestURI() {
        return requestURI;
    }
    
    /**
     * @return
     */
    public String getPathInfo() {
        return pathInfo;
    }
    
    /**
     * @return
     */
    public int getPathLevel() {
        return pathLevel;
    }

    /**
     * @return
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @return
     */
    public String getAccept() {
        return accept;
    }
    
    /**
     * @return
     */
    public boolean isRestRequest() {
        return restRequest;
    }

    /**
     * @return
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accept == null) ? 0 : accept.hashCode());
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        result = prime * result + ((pathInfo == null) ? 0 : pathInfo.hashCode());
        result = prime * result + pathLevel;
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        result = prime * result + ((requestLine == null) ? 0 : requestLine.hashCode());
        result = prime * result + ((requestMethod == null) ? 0 : requestMethod.hashCode());
        result = prime * result + ((requestURI == null) ? 0 : requestURI.hashCode());
        result = prime * result + (restRequest ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HttpRequest other = (HttpRequest) obj;
        if (accept == null) {
            if (other.accept != null)
                return false;
        } else if (!accept.equals(other.accept))
            return false;
        if (headers == null) {
            if (other.headers != null)
                return false;
        } else if (!headers.equals(other.headers))
            return false;
        if (pathInfo == null) {
            if (other.pathInfo != null)
                return false;
        } else if (!pathInfo.equals(other.pathInfo))
            return false;
        if (pathLevel != other.pathLevel)
            return false;
        if (requestId == null) {
            if (other.requestId != null)
                return false;
        } else if (!requestId.equals(other.requestId))
            return false;
        if (requestLine == null) {
            if (other.requestLine != null)
                return false;
        } else if (!requestLine.equals(other.requestLine))
            return false;
        if (requestMethod == null) {
            if (other.requestMethod != null)
                return false;
        } else if (!requestMethod.equals(other.requestMethod))
            return false;
        if (requestURI == null) {
            if (other.requestURI != null)
                return false;
        } else if (!requestURI.equals(other.requestURI))
            return false;
        if (restRequest != other.restRequest)
            return false;
        return true;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "HttpRequest [requestId=" + requestId + ", requestMethod=" + requestMethod + ", requestLine="
                + requestLine + ", requestURI=" + requestURI + ", pathInfo=" + pathInfo + ", pathLevel=" + pathLevel
                + ", headers=" + headers + ", accept=" + accept + ", restRequest=" + restRequest + "]";
    }
}