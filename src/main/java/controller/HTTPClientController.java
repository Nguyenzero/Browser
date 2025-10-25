package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    @FXML
    public void initialize() {
        cbMethod.getItems().addAll("GET", "POST", "HEAD");
        cbMethod.setValue("GET");
        txtIP.setText("127.0.0.1");
        txtPort.setText("8080");
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
            out.println(); // kết thúc header

            // Nhận phản hồi
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                resp.append(line).append("\n");
            }

            txtResponse.setText(resp.toString());

            if (method.equals("GET") || method.equals("POST")) {
                countTags(resp.toString());
            }

        } catch (IOException e) {
            txtResponse.setText("❌ Lỗi: " + e.getMessage());
        }
    }

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
}
