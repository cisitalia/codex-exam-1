좋아요! 아래는 당신이 준 스펙(mybatis\_test.md 요약)에 “그대로” 맞춘 **실행 가능한 최소 예제**입니다.
패키지: `kr.co.ldk`, 빌드: Gradle(Kotlin), MyBatis + Thymeleaf, **HTML 페이지로 리스트/페이징 + CRUD** 테스트까지 포함했어요.
(※ 이미 로컬 MySQL에 `ldk_common.sido` 테이블과 약 200건 데이터가 있다고 가정)


# 3) 도메인/DTO

```java
// src/main/java/kr/co/ldk/sido/domain/Sido.java
package kr.co.ldk.sido.domain;

import lombok.Data;

@Data
public class Sido {
    private Integer seq;     // PK (auto increment)
    private String sido;     // 시도
    private String gugun;    // 구군
}
```

---

# 4) Mapper 인터페이스

```java
// src/main/java/kr/co/ldk/sido/mapper/SidoMapper.java
package kr.co.ldk.sido.mapper;

import kr.co.ldk.sido.domain.Sido;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SidoMapper {
    // 단건
    Sido findById(@Param("seq") Integer seq);

    // 페이징 조회 (선택적 검색어)
    List<Sido> findPage(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    // 총 레코드 수
    int count(@Param("keyword") String keyword);

    // CRUD
    int insert(Sido sido);
    int update(Sido sido);
    int delete(@Param("seq") Integer seq);
}
```

---

# 5) Mapper XML

```xml
<!-- src/main/resources/mapper/SidoMapper.xml -->
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="kr.co.ldk.sido.mapper.SidoMapper">

  <resultMap id="SidoMap" type="kr.co.ldk.sido.domain.Sido">
    <id property="seq" column="seq"/>
    <result property="sido" column="sido"/>
    <result property="gugun" column="gugun"/>
  </resultMap>

  <sql id="Base_Columns"> seq, sido, gugun </sql>

  <select id="findById" parameterType="int" resultMap="SidoMap">
    SELECT <include refid="Base_Columns"/>
    FROM ldk_common.sido
    WHERE seq = #{seq}
  </select>

  <select id="count" resultType="int">
    SELECT COUNT(*)
    FROM ldk_common.sido
    <where>
      <if test="keyword != null and keyword != ''">
        (sido LIKE CONCAT('%', #{keyword}, '%')
         OR gugun LIKE CONCAT('%', #{keyword}, '%'))
      </if>
    </where>
  </select>

  <select id="findPage" resultMap="SidoMap">
    SELECT <include refid="Base_Columns"/>
    FROM ldk_common.sido
    <where>
      <if test="keyword != null and keyword != ''">
        (sido LIKE CONCAT('%', #{keyword}, '%')
         OR gugun LIKE CONCAT('%', #{keyword}, '%'))
      </if>
    </where>
    ORDER BY seq DESC
    LIMIT #{limit} OFFSET #{offset}
  </select>

  <insert id="insert" parameterType="kr.co.ldk.sido.domain.Sido" useGeneratedKeys="true" keyProperty="seq">
    INSERT INTO ldk_common.sido (sido, gugun)
    VALUES (#{sido}, #{gugun})
  </insert>

  <update id="update" parameterType="kr.co.ldk.sido.domain.Sido">
    UPDATE ldk_common.sido
    SET sido = #{sido},
        gugun = #{gugun}
    WHERE seq = #{seq}
  </update>

  <delete id="delete" parameterType="int">
    DELETE FROM ldk_common.sido WHERE seq = #{seq}
  </delete>

</mapper>
```

---

# 6) 서비스 (간단 래핑)

```java
// src/main/java/kr/co/ldk/sido/service/SidoService.java
package kr.co.ldk.sido.service;

import kr.co.ldk.sido.domain.Sido;
import kr.co.ldk.sido.mapper.SidoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SidoService {
    private final SidoMapper mapper;

    public SidoService(SidoMapper mapper) { this.mapper = mapper; }

    public Sido get(Integer seq) { return mapper.findById(seq); }

    public int count(String keyword) { return mapper.count(keyword); }

    public List<Sido> page(String keyword, int page, int size) {
        int p = Math.max(1, page);
        int s = Math.max(1, size);
        int offset = (p - 1) * s;
        return mapper.findPage(keyword, offset, s);
    }

    public void create(Sido s) { mapper.insert(s); }
    public void update(Sido s) { mapper.update(s); }
    public void delete(Integer seq) { mapper.delete(seq); }
}
```

---

# 7) 컨트롤러 (HTML 페이지 테스트용)

```java
// src/main/java/kr/co/ldk/sido/web/SidoController.java
package kr.co.ldk.sido.web;

import kr.co.ldk.sido.domain.Sido;
import kr.co.ldk.sido.service.SidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/sido")
public class SidoController {
    private final SidoService service;

    public SidoController(SidoService service) { this.service = service; }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        int total = service.count(keyword);
        List<Sido> rows = service.page(keyword, page, size);
        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("rows", rows);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);

        return "sido/list";
    }

    @GetMapping("/new")
    public String formNew(Model model) {
        model.addAttribute("sido", new Sido());
        return "sido/form";
    }

    @PostMapping
    public String create(@ModelAttribute Sido sido) {
        service.create(sido);
        return "redirect:/sido";
    }

    @GetMapping("/{seq}/edit")
    public String formEdit(@PathVariable Integer seq, Model model) {
        model.addAttribute("sido", service.get(seq));
        return "sido/form";
    }

    @PostMapping("/{seq}")
    public String update(@PathVariable Integer seq, @ModelAttribute Sido sido) {
        sido.setSeq(seq);
        service.update(sido);
        return "redirect:/sido";
    }

    @PostMapping("/{seq}/delete")
    public String delete(@PathVariable Integer seq) {
        service.delete(seq);
        return "redirect:/sido";
    }
}
```

---

# 8) Thymeleaf 템플릿

## 8-1. 리스트 + 페이징

`src/main/resources/templates/sido/list.html`

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"/>
  <title>시도 리스트</title>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <style>
    body { font-family: system-ui, -apple-system, "Malgun Gothic", "맑은 고딕", sans-serif; padding: 24px; }
    table { width: 100%; border-collapse: collapse; }
    th, td { padding: 8px 10px; border-bottom: 1px solid #e5e7eb; }
    th { text-align: left; background: #f9fafb; }
    .toolbar { display:flex; gap:8px; align-items:center; margin-bottom:12px; }
    .pager { display:flex; gap:6px; margin-top:14px; }
    .pager a, .pager span { padding:4px 8px; border:1px solid #e5e7eb; border-radius:6px; text-decoration:none; color:#111827; }
    .pager .active { background:#111827; color:#fff; }
    .actions form { display:inline; }
  </style>
</head>
<body>
<h1>시도 리스트</h1>

<div class="toolbar">
  <form th:action="@{/sido}" method="get">
    <input type="text" name="keyword" th:value="${keyword}" placeholder="시도/구군 검색"/>
    <input type="hidden" name="size" th:value="${size}"/>
    <button type="submit">검색</button>
  </form>
  <a th:href="@{/sido/new}">+ 새로 만들기</a>
  <span th:text="|총 ${total}건|"></span>
</div>

<table>
  <thead>
    <tr>
      <th style="width:80px;">SEQ</th>
      <th>시도</th>
      <th>구군</th>
      <th style="width:200px;">액션</th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="row : ${rows}">
      <td th:text="${row.seq}"></td>
      <td th:text="${row.sido}"></td>
      <td th:text="${row.gugun}"></td>
      <td class="actions">
        <a th:href="@{|/sido/${row.seq}/edit|}">수정</a>
        <form th:action="@{|/sido/${row.seq}/delete|}" method="post" style="display:inline;">
          <button type="submit" onclick="return confirm('삭제할까요?')">삭제</button>
        </form>
      </td>
    </tr>
  </tbody>
</table>

<div class="pager" th:if="${totalPages > 1}">
  <a th:if="${page > 1}"
     th:href="@{|/sido?page=${page-1}&size=${size}&keyword=${keyword}|}">이전</a>
  <span th:each="p : ${#numbers.sequence(1, totalPages)}"
        th:classappend="${p == page} ? 'active'"
        th:text="${p}"
        th:if="${p == 1 || p == totalPages || (p >= page-2 && p <= page+2)}"
        th:with="href=@{|/sido?page=${p}&size=${size}&keyword=${keyword}|}"
        onclick="location.href=this.getAttribute('data-href');"
        th:attr="data-href=${href}"
        style="cursor:pointer;"></span>
  <a th:if="${page < totalPages}"
     th:href="@{|/sido?page=${page+1}&size=${size}&keyword=${keyword}|}">다음</a>
</div>

</body>
</html>
```

## 8-2. 등록/수정 폼

`src/main/resources/templates/sido/form.html`

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"/>
  <title>시도 편집</title>
  <style>
    body { font-family: system-ui, -apple-system, "Malgun Gothic", "맑은 고딕", sans-serif; padding: 24px; }
    form { display:flex; flex-direction:column; gap:12px; max-width:400px; }
    label { font-weight:600; }
    input { padding:8px; border:1px solid #e5e7eb; border-radius:6px; }
    .row { display:flex; gap:12px; }
    .row > div { flex:1; }
  </style>
</head>
<body>
<h1 th:text="${sido.seq} == null ? '새 시도' : '시도 수정'"></h1>

<form th:action="${sido.seq} == null ? @{/sido} : @{|/sido/${sido.seq}|}" method="post">
  <div class="row">
    <div>
      <label>시도</label>
      <input type="text" name="sido" th:value="${sido.sido}" required/>
    </div>
    <div>
      <label>구군</label>
      <input type="text" name="gugun" th:value="${sido.gugun}" required/>
    </div>
  </div>
  <div>
    <button type="submit">저장</button>
    <a href="/sido">목록</a>
  </div>
</form>
</body>
</html>
```

---

# 9) 실행

```bash
# .env에 DB_USERNAME/DB_PASSWORD 채우기
./gradlew bootRun
# 브라우저에서 http://localhost:8080/sido
# - 200건 데이터가 있으면 기본 size=10 기준 페이징 확인 가능
# - 검색 keyword 로 sido/gugun LIKE 검색
# - 새로 만들기/수정/삭제 테스트 가능
```

---

# 10) 메모/주의사항

* **문자셋/타임존 일관성**: `characterEncoding=utf8mb4`, `serverTimezone=Asia/Seoul`, `connectionTimeZone=SERVER`를 URL에 명시했습니다.
* **스키마명**: 실 테이블이 `ldk_common.sido`이므로 쿼리와 URL 모두 `ldk_common`으로 통일했습니다. (질문에 있던 `test` DB명은 혼선을 막기 위해 수정)
* **SQL init**: 로컬에 이미 데이터가 있으므로 `spring.sql.init.mode=never`.
* **H2**: 원하면 제거해도 됩니다(현재는 runtimeOnly).
* **페이징**: `LIMIT #{limit} OFFSET #{offset}` 단순 방식. 200건이면 충분합니다.
* **의존성 오타**: `om.h2database:h2` → `com.h2database:h2`로 교정.

---
