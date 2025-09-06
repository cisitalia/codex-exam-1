���ƿ�! �Ʒ��� ����� �� ����(mybatis\_test.md ���)�� ���״�Ρ� ���� **���� ������ �ּ� ����**�Դϴ�.
��Ű��: `kr.co.ldk`, ����: Gradle(Kotlin), MyBatis + Thymeleaf, **HTML �������� ����Ʈ/����¡ + CRUD** �׽�Ʈ���� �����߾��.
(�� �̹� ���� MySQL�� `ldk_common.sido` ���̺�� �� 200�� �����Ͱ� �ִٰ� ����)


# 3) ������/DTO

```java
// src/main/java/kr/co/ldk/sido/domain/Sido.java
package kr.co.ldk.sido.domain;

import lombok.Data;

@Data
public class Sido {
    private Integer seq;     // PK (auto increment)
    private String sido;     // �õ�
    private String gugun;    // ����
}
```

---

# 4) Mapper �������̽�

```java
// src/main/java/kr/co/ldk/sido/mapper/SidoMapper.java
package kr.co.ldk.sido.mapper;

import kr.co.ldk.sido.domain.Sido;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SidoMapper {
    // �ܰ�
    Sido findById(@Param("seq") Integer seq);

    // ����¡ ��ȸ (������ �˻���)
    List<Sido> findPage(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    // �� ���ڵ� ��
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

# 6) ���� (���� ����)

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

# 7) ��Ʈ�ѷ� (HTML ������ �׽�Ʈ��)

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

# 8) Thymeleaf ���ø�

## 8-1. ����Ʈ + ����¡

`src/main/resources/templates/sido/list.html`

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"/>
  <title>�õ� ����Ʈ</title>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <style>
    body { font-family: system-ui, -apple-system, "Malgun Gothic", "���� ���", sans-serif; padding: 24px; }
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
<h1>�õ� ����Ʈ</h1>

<div class="toolbar">
  <form th:action="@{/sido}" method="get">
    <input type="text" name="keyword" th:value="${keyword}" placeholder="�õ�/���� �˻�"/>
    <input type="hidden" name="size" th:value="${size}"/>
    <button type="submit">�˻�</button>
  </form>
  <a th:href="@{/sido/new}">+ ���� �����</a>
  <span th:text="|�� ${total}��|"></span>
</div>

<table>
  <thead>
    <tr>
      <th style="width:80px;">SEQ</th>
      <th>�õ�</th>
      <th>����</th>
      <th style="width:200px;">�׼�</th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="row : ${rows}">
      <td th:text="${row.seq}"></td>
      <td th:text="${row.sido}"></td>
      <td th:text="${row.gugun}"></td>
      <td class="actions">
        <a th:href="@{|/sido/${row.seq}/edit|}">����</a>
        <form th:action="@{|/sido/${row.seq}/delete|}" method="post" style="display:inline;">
          <button type="submit" onclick="return confirm('�����ұ��?')">����</button>
        </form>
      </td>
    </tr>
  </tbody>
</table>

<div class="pager" th:if="${totalPages > 1}">
  <a th:if="${page > 1}"
     th:href="@{|/sido?page=${page-1}&size=${size}&keyword=${keyword}|}">����</a>
  <span th:each="p : ${#numbers.sequence(1, totalPages)}"
        th:classappend="${p == page} ? 'active'"
        th:text="${p}"
        th:if="${p == 1 || p == totalPages || (p >= page-2 && p <= page+2)}"
        th:with="href=@{|/sido?page=${p}&size=${size}&keyword=${keyword}|}"
        onclick="location.href=this.getAttribute('data-href');"
        th:attr="data-href=${href}"
        style="cursor:pointer;"></span>
  <a th:if="${page < totalPages}"
     th:href="@{|/sido?page=${page+1}&size=${size}&keyword=${keyword}|}">����</a>
</div>

</body>
</html>
```

## 8-2. ���/���� ��

`src/main/resources/templates/sido/form.html`

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"/>
  <title>�õ� ����</title>
  <style>
    body { font-family: system-ui, -apple-system, "Malgun Gothic", "���� ���", sans-serif; padding: 24px; }
    form { display:flex; flex-direction:column; gap:12px; max-width:400px; }
    label { font-weight:600; }
    input { padding:8px; border:1px solid #e5e7eb; border-radius:6px; }
    .row { display:flex; gap:12px; }
    .row > div { flex:1; }
  </style>
</head>
<body>
<h1 th:text="${sido.seq} == null ? '�� �õ�' : '�õ� ����'"></h1>

<form th:action="${sido.seq} == null ? @{/sido} : @{|/sido/${sido.seq}|}" method="post">
  <div class="row">
    <div>
      <label>�õ�</label>
      <input type="text" name="sido" th:value="${sido.sido}" required/>
    </div>
    <div>
      <label>����</label>
      <input type="text" name="gugun" th:value="${sido.gugun}" required/>
    </div>
  </div>
  <div>
    <button type="submit">����</button>
    <a href="/sido">���</a>
  </div>
</form>
</body>
</html>
```

---

# 9) ����

```bash
# .env�� DB_USERNAME/DB_PASSWORD ä���
./gradlew bootRun
# ���������� http://localhost:8080/sido
# - 200�� �����Ͱ� ������ �⺻ size=10 ���� ����¡ Ȯ�� ����
# - �˻� keyword �� sido/gugun LIKE �˻�
# - ���� �����/����/���� �׽�Ʈ ����
```

---

# 10) �޸�/���ǻ���

* **���ڼ�/Ÿ���� �ϰ���**: `characterEncoding=utf8mb4`, `serverTimezone=Asia/Seoul`, `connectionTimeZone=SERVER`�� URL�� ����߽��ϴ�.
* **��Ű����**: �� ���̺��� `ldk_common.sido`�̹Ƿ� ������ URL ��� `ldk_common`���� �����߽��ϴ�. (������ �ִ� `test` DB���� ȥ���� ���� ���� ����)
* **SQL init**: ���ÿ� �̹� �����Ͱ� �����Ƿ� `spring.sql.init.mode=never`.
* **H2**: ���ϸ� �����ص� �˴ϴ�(����� runtimeOnly).
* **����¡**: `LIMIT #{limit} OFFSET #{offset}` �ܼ� ���. 200���̸� ����մϴ�.
* **������ ��Ÿ**: `om.h2database:h2` �� `com.h2database:h2`�� ����.

---
