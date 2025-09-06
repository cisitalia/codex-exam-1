package kr.co.ldk.common.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class EnvDebugController {

    private final Environment env;

    // @Value를 사용하여 spring-dotenv 적용 여부를 직접 확인
    @Value("${DB_HOST:}")
    private String dbHost;
    @Value("${DB_PORT:}")
    private String dbPort;
    @Value("${DB_NAME:}")
    private String dbName;
    @Value("${DB_USERNAME:}")
    private String dbUser;
    @Value("${DB_PASSWORD:}")
    private String dbPass;

    public EnvDebugController(Environment env) {
        this.env = env;
    }

    @GetMapping("/env")
    public Map<String, Object> showEnv() {
        Map<String, Object> m = new LinkedHashMap<>();

        // spring-dotenv가 주입한 값 (@Value)
        m.put("value.DB_HOST", dbHost);
        m.put("value.DB_PORT", dbPort);
        m.put("value.DB_NAME", dbName);
        m.put("value.DB_USERNAME", dbUser);
        m.put("value.DB_PASSWORD(masked)", mask(dbPass));

        // Environment에서 직접 조회한 값
        m.put("env.DB_HOST", env.getProperty("DB_HOST"));
        m.put("env.DB_PORT", env.getProperty("DB_PORT"));
        m.put("env.DB_NAME", env.getProperty("DB_NAME"));
        m.put("env.DB_USERNAME", env.getProperty("DB_USERNAME"));
        m.put("env.DB_PASSWORD(masked)", mask(env.getProperty("DB_PASSWORD")));

        // Datasource URL (placeholder가 해석된 결과)
        String dsUrl = env.getProperty("spring.datasource.url");
        m.put("spring.datasource.url", dsUrl);

        // 재구성한 URL(참고용)
        String rebuilt = String.format(
                "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Seoul&connectionTimeZone=SERVER",
                nz(dbHost, "localhost"), nz(dbPort, "3306"), nz(dbName, "ldk_common")
        );
        m.put("rebuilt.url.from.values", rebuilt);

        return m;
    }

    private static String mask(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.length() <= 2) return "*".repeat(s.length());
        return "*".repeat(s.length() - 2) + s.substring(s.length() - 2);
    }

    private static String nz(String v, String d) {
        return (v == null || v.isEmpty()) ? d : v;
    }
}

