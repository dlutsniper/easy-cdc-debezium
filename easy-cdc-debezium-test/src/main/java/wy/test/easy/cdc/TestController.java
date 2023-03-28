package wy.test.easy.cdc;

import com.dlutsniper.easy.cdc.observability.metrics.DebeziumMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private DebeziumMetrics debeziumMetrics;

    @GetMapping
    public String test() {
        return debeziumMetrics.logMetrics();
    }
}
