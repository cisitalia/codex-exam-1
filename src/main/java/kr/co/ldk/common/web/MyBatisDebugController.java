package kr.co.ldk.common.web;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MyBatisDebugController {
    private final SqlSessionFactory sqlSessionFactory;

    public MyBatisDebugController(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @GetMapping("/debug/mybatis")
    public Map<String, Object> check() throws Exception {
        Map<String, Object> res = new LinkedHashMap<>();

        // 1) Mapper XML 리소스 존재 확인
        PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
        Resource[] resources = r.getResources("classpath*:mapper/**/*.xml");
        List<String> names = new ArrayList<>();
        for (Resource resource : resources) {
            names.add(resource.getDescription());
        }
        res.put("mapperXmls", names);

        // 2) 매핑된 statement 존재 여부 확인
        String ns = "kr.co.ldk.sido.mapper.SidoMapper";
        res.put("has.findById", sqlSessionFactory.getConfiguration().hasStatement(ns + ".findById"));
        res.put("has.count", sqlSessionFactory.getConfiguration().hasStatement(ns + ".count"));
        res.put("has.findPage", sqlSessionFactory.getConfiguration().hasStatement(ns + ".findPage"));
        res.put("has.insert", sqlSessionFactory.getConfiguration().hasStatement(ns + ".insert"));
        res.put("has.update", sqlSessionFactory.getConfiguration().hasStatement(ns + ".update"));
        res.put("has.delete", sqlSessionFactory.getConfiguration().hasStatement(ns + ".delete"));

        return res;
    }
}

