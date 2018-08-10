package eu.arrowhead.core.systemregistry.model;

import java.net.MalformedURLException;
import java.net.URL;

public class HttpEndpoint extends Endpoint {

  public static final String ENDPOINT_TYPE = "HttpEndpoint";

  private String host;
  private int port;
  private String path;
  private boolean secure;

  /**
   * @param host hostname of endpoint
   * @param port port of endpoint
   * @param path path appended to
   * @param secure true on https endpoint
   */
  public HttpEndpoint(String host, int port, String path, boolean secure) {
    this.host = host;
    this.port = port;
    this.path = path;
    this.secure = secure;
  }

  /**
   * Creates a plain http endpoint
   */
  public HttpEndpoint(String host, int port, String path) {
    this(host, port, path, false);
  }

  public HttpEndpoint() {
    super();
  }

  @Override
  public String getType() {
    return ENDPOINT_TYPE;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  /**
   * @return true on https endpoint, false in http endpoint
   */
  public boolean isSecure() {
    return secure;
  }

  /**
   * @param secure true on https connection
   */
  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  /**
   * Creates an url from this endpoint
   *
   * @return the full url to this endpoint
   */
  public URL toURL() throws MalformedURLException {
    String protocol = isSecure() ? "https" : "http";
    return new URL(protocol, host, port, path);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((host == null) ? 0 : host.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + port;
    result = prime * result + (secure ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    HttpEndpoint other = (HttpEndpoint) obj;
    if (host == null) {
      if (other.host != null) {
        return false;
      }
    } else if (!host.equals(other.host)) {
      return false;
    }
    if (path == null) {
      if (other.path != null) {
        return false;
      }
    } else if (!path.equals(other.path)) {
      return false;
    }
    if (port != other.port) {
      return false;
    }
    if (secure != other.secure) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "HttpEndpoint [host=" + host + ", port=" + port + ", path=" + path + ", secure=" + secure + "]";
  }

  /**
   * Creates an endpoint from a URL
   *
   * @param urlStr Complete URL string
   *
   * @return An endpoint created from the argument or null if no such endpoint could be created
   */
  public static HttpEndpoint createFromString(String urlStr) {
    HttpEndpoint result = null;

    try {
      URL url = new URL(urlStr);
      result = new HttpEndpoint(url.getHost(), url.getPort(), url.getFile(), url.getProtocol().equals("https"));
    } catch (MalformedURLException e) {
    }

    return result;
  }
}