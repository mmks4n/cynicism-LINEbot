
package linebot;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
public class LineWebhookController {

    private final JsonMapper jsonMapper;
    private final LineReplyService lineReplyService;
    private final LineSignatureValidator signatureValidator;

    public LineWebhookController(
            JsonMapper jsonMapper,
            LineReplyService lineReplyService,
            LineSignatureValidator signatureValidator) {

        this.jsonMapper = jsonMapper;
        this.lineReplyService = lineReplyService;
        this.signatureValidator = signatureValidator;
    }

    @PostMapping("/webhook")
    public String webhook(
            @RequestBody String requestBody,
            @RequestHeader("X-Line-Signature") String signature
    ) throws Exception {

        if (!signatureValidator.isValid(requestBody, signature)) {
            System.out.println("署名検証に失敗しました");
            return "Invalid signature";
        }

        System.out.println("署名検証に成功しました");
        String token = System.getenv("LINE_CHANNEL_ACCESS_TOKEN");

        System.out.println(
                token == null ? "トークンを取得できません" : "トークンを取得できました"
        );

        // JSONを解析する
        JsonNode root = jsonMapper.readTree(requestBody);

        // events配列を取り出す
        JsonNode events = root.path("events");

        // イベントを1つずつ処理する
        for (JsonNode event : events) {

            // 返信用トークンを取得
            String replyToken = event.path("replyToken").asText();

            System.out.println("replyToken：" + replyToken);

            // メッセージイベントか確認
            if ("message".equals(event.path("type").asText())) {

                JsonNode message = event.path("message");

                // テキストメッセージか確認
                if ("text".equals(message.path("type").asText())) {
                    String text = message.path("text").asText();

                    System.out.println("受信したメッセージ：" + text);

                    lineReplyService.reply(
                            replyToken,
                            "受け取ったよ：" + text
                    );
                }
            }
        }

        return "OK";
    }
}
