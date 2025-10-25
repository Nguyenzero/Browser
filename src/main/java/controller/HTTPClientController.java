package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;

import java.io.*;
import java.net.*;
import java.util.regex.*;

public class HTTPClientController {

    @FXML private TextField txtIP;
    @FXML private TextField txtPort;
    @FXML private TextField txtURL;
    @FXML private ComboBox<String> cbMethod;
    @FXML private TextArea txtResponse;
    @FXML private Button btnSend;
    @FXML private WebView webView;

    @FXML
    public void initialize() {
        cbMethod.getItems().addAll("GET", "POST", "HEAD");
        cbMethod.setValue("GET");
        txtIP.setText("192.168.33.1");
        txtPort.setText("8080");
        txtURL.setText("https://");

        // Chặn mọi tương tác với WebView (chỉ hiển thị)
        webView.addEventFilter(MouseEvent.ANY, e -> e.consume());
        webView.addEventFilter(KeyEvent.ANY, e -> e.consume());
    }

    @FXML
    private void onSendRequest() {
        String serverIP = txtIP.getText().trim();
        String url = txtURL.getText().trim();
        String method = cbMethod.getValue();
        int port;

        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException e) {
            txtResponse.setText("⚠️ Port không hợp lệ!");
            return;
        }

        try (Socket socket = new Socket(serverIP, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Gửi request
            out.println(method + " " + url + " HTTP/1.1");
            out.println("Host: " + serverIP);
            out.println("Connection: close");
            out.println();

            // Nhận phản hồi
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                resp.append(line).append("\n");
            }

            txtResponse.setText(resp.toString());

            if (method.equals("GET") || method.equals("POST")) {
                countTags(resp.toString());
                displayWebPage(url);
            }

        } catch (IOException e) {
            txtResponse.setText("❌ Lỗi: " + e.getMessage());
        }
    }

    /** Đếm số thẻ HTML **/
    private void countTags(String html) {
        StringBuilder sb = new StringBuilder("\n=== Phân tích HTML ===\n");
        sb.append("Độ dài nội dung: ").append(html.getBytes().length).append(" bytes\n");

        String[] tags = {"p", "div", "span", "img"};
        for (String tag : tags) {
            Pattern pattern = Pattern.compile("<\\s*" + tag + "(\\s+[^>]*)?>", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(html);
            int count = 0;
            while (matcher.find()) count++;
            sb.append("<").append(tag).append(">: ").append(count).append("\n");
        }

        txtResponse.appendText(sb.toString());
    }

    /** Hiển thị trang thật bằng WebView (chỉ xem, không tương tác) **/
    private void displayWebPage(String url) {
        WebEngine engine = webView.getEngine();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        engine.load(url);
    }
}
