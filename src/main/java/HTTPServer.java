import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class HTTPServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("✅ HTTP Server đang chạy trên cổng 8080...");
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("Địa chỉ IPv4 của server: " + ip.getHostAddress());

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(new ClientHandler(client)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket client;

    public ClientHandler(Socket socket) {
        this.client = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             OutputStream out = client.getOutputStream()) {

            String line;
            StringBuilder request = new StringBuilder();
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                request.append(line).append("\n");
            }

            String requestLine = request.toString().split("\n")[0];
            System.out.println("📩 Nhận request: " + requestLine);

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;
            String method = parts[0];
            String url = parts[1];

            String body;
            if (url.startsWith("http://") || url.startsWith("https://")) {
                body = fetchRealURL(url);
            } else {
                body = """
                        <html><body>
                        <h1>Chào mừng đến HTTP Server VKU!</h1>
                        <p>Đây là nội dung mẫu do server gửi về qua TCP</p>
                        <div>Thẻ div thứ 1</div>
                        <div>Thẻ div thứ 2</div>
                        <span>Thẻ span</span>
                        <img src='test.png'>
                        </body></html>
                        """;
            }

            String header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "Connection: close\r\n\r\n";

            if (method.equals("HEAD")) {
                out.write(header.getBytes(StandardCharsets.UTF_8));
            } else {
                out.write(header.getBytes(StandardCharsets.UTF_8));
                out.write(body.getBytes(StandardCharsets.UTF_8));
            }

            out.flush();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String fetchRealURL(String urlString) {
        StringBuilder html = new StringBuilder();
        try {
            if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                urlString = "https://" + urlString; // ✅ mặc định dùng HTTPS
            }

            System.out.println("🌐 Truy cập thật: " + urlString);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/124.0 Safari/537.36");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            int responseCode = conn.getResponseCode();
            System.out.println("🟢 Mã phản hồi: " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                html.append(line).append("\n");
            }
            in.close();

            conn.disconnect();
            return html.toString();

        } catch (IOException e) {
            return "<html><body><h3>Lỗi truy cập: " + urlString + "</h3><p>" + e.getMessage() + "</p></body></html>";
        }
    }

}
