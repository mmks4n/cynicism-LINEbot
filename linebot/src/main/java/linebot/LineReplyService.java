package linebot;

import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class LineReplyService {

    private final JsonMapper jsonMapper;
    private final HttpClient httpClient;

    public LineReplyService(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void reply(String replyToken, String messageText) throws Exception {

        // アクセストークンを取得
        String accessToken =
                System.getenv("LINE_CHANNEL_ACCESS_TOKEN");

        // 返信内容を作成
        Map<String, Object> body = Map.of(
                "replyToken", replyToken,
                "messages", List.of(
                        Map.of(
                                "type", "text",
                                "text", messageText
                        )
                )
        );

        // JavaのデータをJSONに変換
        String json = jsonMapper.writeValueAsString(body);

        // HTTPリクエストを作成
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.line.me/v2/bot/message/reply"
                ))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        json, StandardCharsets.UTF_8
                ))
                .build();

        // LINEにリクエストを送信
        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println("LINE APIのステータス：" +
                response.statusCode());
    }
}