package kr.co.ldk.sido.domain;

import lombok.Data;

@Data
public class Sido {
    private Integer seq;    // PK (auto increment)
    private String sido;    // 시도
    private String gugun;   // 구군
}

