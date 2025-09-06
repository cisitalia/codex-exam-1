package kr.co.ldk.common.web;

import kr.co.ldk.sido.service.SidoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class DbDebugController {
    private final SidoService sidoService;

    public DbDebugController(SidoService sidoService) {
        this.sidoService = sidoService;
    }

    @GetMapping("/debug/db")
    public Map<String, Object> dbCheck(@RequestParam(defaultValue = "") String keyword) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            int total = sidoService.count(keyword);
            res.put("ok", true);
            res.put("total", total);
        } catch (Exception e) {
            res.put("ok", false);
            res.put("error", e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage()));
            // unwrap causes for better diagnostics
            Throwable t = e.getCause();
            int i = 1;
            while (t != null && i <= 5) {
                res.put("cause" + i, t.getClass().getName() + ": " + String.valueOf(t.getMessage()));
                t = t.getCause();
                i++;
            }
        }
        return res;
    }
}
