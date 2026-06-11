package ru.tardyon.maven.telegram.notifier.telegram;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tardyon.maven.telegram.notifier.core.config.NotifierConfig;

final class Socks5TelegramRequestExecutor implements DefaultTelegramSender.TelegramRequestExecutor {
  private static final String TELEGRAM_HOST = "api.telegram.org";
  private static final int TELEGRAM_PORT = 443;
  private static final int SOCKS_VERSION = 5;
  private static final int SOCKS_AUTH_NONE = 0x00;
  private static final int SOCKS_AUTH_PASSWORD = 0x02;
  private static final int SOCKS_CMD_CONNECT = 0x01;
  private static final int SOCKS_ADDR_DOMAIN = 0x03;
  private static final int CONNECT_TIMEOUT_MILLIS = 30_000;

  private final String token;
  private final String proxyHost;
  private final int proxyPort;
  private final String username;
  private final String password;
  private final SocketFactory socketFactory;
  private final SSLSocketFactory sslSocketFactory;

  Socks5TelegramRequestExecutor(NotifierConfig config) {
    this(
        requireText(config.botToken(), "botToken"),
        requireText(config.proxyHost(), "proxyHost"),
        requirePort(config.proxyPort()),
        requireText(config.proxyUsername(), "proxyUsername"),
        config.proxyPassword() == null ? "" : config.proxyPassword(),
        SocketFactory.getDefault(),
        (SSLSocketFactory) SSLSocketFactory.getDefault());
  }

  Socks5TelegramRequestExecutor(
      String token,
      String proxyHost,
      int proxyPort,
      String username,
      String password,
      SocketFactory socketFactory,
      SSLSocketFactory sslSocketFactory) {
    this.token = requireText(token, "token");
    this.proxyHost = requireText(proxyHost, "proxyHost");
    this.proxyPort = requirePort(proxyPort);
    this.username = requireText(username, "proxyUsername");
    this.password = password == null ? "" : password;
    this.socketFactory = Objects.requireNonNull(socketFactory, "socketFactory");
    this.sslSocketFactory = Objects.requireNonNull(sslSocketFactory, "sslSocketFactory");
  }

  @Override
  public void execute(SendMessage sendMessage) throws TelegramApiException {
    try {
      HttpResponse response = executePost(sendMessage);
      if (response.statusCode < 200 || response.statusCode >= 300) {
        throw new TelegramApiException(
            "Telegram API returned HTTP " + response.statusCode + ": " + response.body);
      }
      if (response.body.contains("\"ok\":false")) {
        throw new TelegramApiException("Telegram API error response: " + response.body);
      }
    } catch (IOException ex) {
      throw new TelegramApiException("Unable to execute sendmessage method", ex);
    }
  }

  private HttpResponse executePost(SendMessage sendMessage) throws IOException {
    byte[] requestBody = buildJson(sendMessage).getBytes(StandardCharsets.UTF_8);
    try (Socket proxySocket = socketFactory.createSocket()) {
      proxySocket.connect(new InetSocketAddress(proxyHost, proxyPort), CONNECT_TIMEOUT_MILLIS);
      proxySocket.setSoTimeout(0);
      establishSocksTunnel(proxySocket, TELEGRAM_HOST, TELEGRAM_PORT);
      try (Socket sslSocket =
          sslSocketFactory.createSocket(proxySocket, TELEGRAM_HOST, TELEGRAM_PORT, true)) {
        sslSocket.setSoTimeout(0);
        writeHttpRequest(sslSocket.getOutputStream(), requestBody);
        return readHttpResponse(sslSocket.getInputStream());
      }
    }
  }

  private void establishSocksTunnel(Socket socket, String targetHost, int targetPort)
      throws IOException {
    BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
    BufferedInputStream in = new BufferedInputStream(socket.getInputStream());

    out.write(new byte[] {SOCKS_VERSION, 2, SOCKS_AUTH_NONE, SOCKS_AUTH_PASSWORD});
    out.flush();

    byte[] greeting = readFully(in, 2);
    if (greeting[0] != SOCKS_VERSION) {
      throw new IOException("Invalid SOCKS5 greeting version: " + (greeting[0] & 0xff));
    }
    if ((greeting[1] & 0xff) == 0xff) {
      throw new IOException("SOCKS5 proxy rejected available authentication methods");
    }
    if ((greeting[1] & 0xff) == SOCKS_AUTH_PASSWORD) {
      authenticate(out, in);
    } else if ((greeting[1] & 0xff) != SOCKS_AUTH_NONE) {
      throw new IOException("Unsupported SOCKS5 authentication method: " + (greeting[1] & 0xff));
    }

    byte[] hostBytes = targetHost.getBytes(StandardCharsets.UTF_8);
    if (hostBytes.length == 0 || hostBytes.length > 255) {
      throw new IOException("SOCKS5 target host must be between 1 and 255 bytes");
    }

    out.write(SOCKS_VERSION);
    out.write(SOCKS_CMD_CONNECT);
    out.write(0);
    out.write(SOCKS_ADDR_DOMAIN);
    out.write(hostBytes.length);
    out.write(hostBytes);
    out.write((targetPort >> 8) & 0xff);
    out.write(targetPort & 0xff);
    out.flush();

    byte[] replyHeader = readFully(in, 4);
    if (replyHeader[0] != SOCKS_VERSION) {
      throw new IOException("Invalid SOCKS5 reply version: " + (replyHeader[0] & 0xff));
    }
    if (replyHeader[1] != 0x00) {
      throw new IOException("SOCKS5 connect failed with status: " + (replyHeader[1] & 0xff));
    }

    skipAddress(in, replyHeader[3] & 0xff);
    readFully(in, 2);
  }

  private void authenticate(OutputStream out, InputStream in) throws IOException {
    byte[] userBytes = username.getBytes(StandardCharsets.UTF_8);
    byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
    if (userBytes.length == 0 || userBytes.length > 255) {
      throw new IOException("SOCKS5 proxy username must be between 1 and 255 bytes");
    }
    if (passwordBytes.length > 255) {
      throw new IOException("SOCKS5 proxy password must be at most 255 bytes");
    }

    out.write(0x01);
    out.write(userBytes.length);
    out.write(userBytes);
    out.write(passwordBytes.length);
    out.write(passwordBytes);
    out.flush();

    byte[] authReply = readFully(in, 2);
    if (authReply[1] != 0x00) {
      throw new IOException("SOCKS5 authentication failed with status: " + (authReply[1] & 0xff));
    }
  }

  private void writeHttpRequest(OutputStream outputStream, byte[] body) throws IOException {
    BufferedOutputStream out = new BufferedOutputStream(outputStream);
    String path = "/bot" + token + "/sendMessage";
    out.write(("POST " + path + " HTTP/1.1\r\n").getBytes(StandardCharsets.US_ASCII));
    out.write(("Host: " + TELEGRAM_HOST + "\r\n").getBytes(StandardCharsets.US_ASCII));
    out.write("Connection: close\r\n".getBytes(StandardCharsets.US_ASCII));
    out.write("Content-Type: application/json; charset=UTF-8\r\n".getBytes(StandardCharsets.US_ASCII));
    out.write(("Content-Length: " + body.length + "\r\n").getBytes(StandardCharsets.US_ASCII));
    out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
    out.write(body);
    out.flush();
  }

  private static HttpResponse readHttpResponse(InputStream inputStream) throws IOException {
    BufferedInputStream in = new BufferedInputStream(inputStream);
    String statusLine = readLine(in);
    if (statusLine == null || statusLine.isBlank()) {
      throw new EOFException("Unexpected end of stream while reading HTTP status line");
    }
    String[] parts = statusLine.split(" ", 3);
    if (parts.length < 2) {
      throw new IOException("Malformed HTTP status line: " + statusLine);
    }
    int statusCode = Integer.parseInt(parts[1]);

    Map<String, List<String>> headers = new LinkedHashMap<>();
    String line;
    while ((line = readLine(in)) != null && !line.isEmpty()) {
      int separator = line.indexOf(':');
      if (separator <= 0) {
        continue;
      }
      String name = line.substring(0, separator).trim();
      String value = line.substring(separator + 1).trim();
      headers.computeIfAbsent(name, key -> new java.util.ArrayList<>()).add(value);
    }

    byte[] body = readBody(in, headers);
    return new HttpResponse(statusCode, new String(body, StandardCharsets.UTF_8));
  }

  private static byte[] readBody(InputStream in, Map<String, List<String>> headers) throws IOException {
    String transferEncoding = firstHeader(headers, "Transfer-Encoding");
    if (transferEncoding != null && transferEncoding.toLowerCase(Locale.ROOT).contains("chunked")) {
      return readChunkedBody(in);
    }

    String contentLength = firstHeader(headers, "Content-Length");
    if (contentLength != null) {
      return readFully(in, Integer.parseInt(contentLength.trim()));
    }

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    in.transferTo(buffer);
    return buffer.toByteArray();
  }

  private static byte[] readChunkedBody(InputStream in) throws IOException {
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    while (true) {
      String sizeLine = readLine(in);
      if (sizeLine == null) {
        throw new EOFException("Unexpected end of stream while reading chunk size");
      }
      int separator = sizeLine.indexOf(';');
      String sizeValue = separator >= 0 ? sizeLine.substring(0, separator) : sizeLine;
      int chunkSize = Integer.parseInt(sizeValue.trim(), 16);
      if (chunkSize == 0) {
        while (true) {
          String trailer = readLine(in);
          if (trailer == null || trailer.isEmpty()) {
            return body.toByteArray();
          }
        }
      }
      body.write(readFully(in, chunkSize));
      readLine(in);
    }
  }

  private static String readLine(InputStream in) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int current;
    while ((current = in.read()) != -1) {
      if (current == '\r') {
        int next = in.read();
        if (next == '\n') {
          return buffer.toString(StandardCharsets.ISO_8859_1.name());
        }
        if (next != -1) {
          buffer.write(current);
          buffer.write(next);
        }
      } else if (current == '\n') {
        return buffer.toString(StandardCharsets.ISO_8859_1.name());
      } else {
        buffer.write(current);
      }
    }
    if (buffer.size() == 0) {
      return null;
    }
    return buffer.toString(StandardCharsets.ISO_8859_1.name());
  }

  private static byte[] readFully(InputStream in, int length) throws IOException {
    byte[] buffer = new byte[length];
    int offset = 0;
    while (offset < length) {
      int read = in.read(buffer, offset, length - offset);
      if (read < 0) {
        throw new EOFException("Unexpected end of stream");
      }
      offset += read;
    }
    return buffer;
  }

  private static String firstHeader(Map<String, List<String>> headers, String name) {
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      if (name.equalsIgnoreCase(entry.getKey()) && !entry.getValue().isEmpty()) {
        return entry.getValue().get(0);
      }
    }
    return null;
  }

  private static void skipAddress(InputStream in, int addressType) throws IOException {
    if (addressType == 0x01) {
      readFully(in, 4);
      return;
    }
    if (addressType == 0x04) {
      readFully(in, 16);
      return;
    }
    if (addressType == SOCKS_ADDR_DOMAIN) {
      int length = in.read();
      if (length < 0) {
        throw new EOFException("Unexpected end of stream while reading domain length");
      }
      readFully(in, length);
      return;
    }
    throw new IOException("Unsupported SOCKS5 address type: " + addressType);
  }

  private static String buildJson(SendMessage sendMessage) {
    StringBuilder json = new StringBuilder();
    json.append('{');
    appendJsonField(json, "chat_id", sendMessage.getChatId());
    json.append(',');
    appendJsonField(json, "text", sendMessage.getText());
    if (sendMessage.getParseMode() != null) {
      json.append(',');
      appendJsonField(json, "parse_mode", sendMessage.getParseMode());
    }
    if (sendMessage.getDisableWebPagePreview() != null) {
      json.append(',');
      json
          .append('"')
          .append("disable_web_page_preview")
          .append('"')
          .append(':')
          .append(sendMessage.getDisableWebPagePreview());
    }
    json.append('}');
    return json.toString();
  }

  private static void appendJsonField(StringBuilder json, String name, String value) {
    json.append('"').append(name).append('"').append(':').append('"').append(escapeJson(value)).append('"');
  }

  private static String escapeJson(String value) {
    StringBuilder escaped = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (ch == '"' || ch == '\\') {
        escaped.append('\\').append(ch);
      } else if (ch == '\b') {
        escaped.append("\\b");
      } else if (ch == '\f') {
        escaped.append("\\f");
      } else if (ch == '\n') {
        escaped.append("\\n");
      } else if (ch == '\r') {
        escaped.append("\\r");
      } else if (ch == '\t') {
        escaped.append("\\t");
      } else if (ch < 0x20) {
        escaped.append(String.format("\\u%04x", (int) ch));
      } else {
        escaped.append(ch);
      }
    }
    return escaped.toString();
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value.trim();
  }

  private static int requirePort(int port) {
    if (port < 1 || port > 65_535) {
      throw new IllegalArgumentException("proxyPort must be in range 1..65535");
    }
    return port;
  }

  private static final class HttpResponse {
    private final int statusCode;
    private final String body;

    private HttpResponse(int statusCode, String body) {
      this.statusCode = statusCode;
      this.body = body;
    }
  }
}
