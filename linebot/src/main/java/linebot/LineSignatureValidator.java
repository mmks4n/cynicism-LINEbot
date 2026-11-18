package linebot;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class LineSignatureValidator {

    public boolean isValid(String requestBody, String signature) throws Exception {

        String channelSecret =
                System.getenv("LINE_CHANNEL_SECRET");

        if (channelSecret == null || signature == null) {
            return false;
        }

        // HMAC-SHA256の計算器を作成
        Mac mac = Mac.getInstance("HmacSHA256");

        // Channel secretを鍵として設定
        SecretKeySpec secretKey =
                new SecretKeySpec(
                        channelSecret.getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"
                );

        mac.init(secretKey);

        // リクエスト本文から署名を計算
        byte[] hash = mac.doFinal(
                requestBody.getBytes(StandardCharsets.UTF_8)
        );

        // Base64形式に変換
        String calculatedSignature =
                Base64.getEncoder().encodeToString(hash);

        // 受信した署名と比較
        return MessageDigest.isEqual(
                calculatedSignature.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        );
    }
}