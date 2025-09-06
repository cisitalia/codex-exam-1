package kr.co.ldk.sido.mapper;

import kr.co.ldk.sido.domain.Sido;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SidoMapper {
    Sido findById(@Param("seq") Integer seq);

    List<Sido> findPage(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    int count(@Param("keyword") String keyword);

    int insert(Sido sido);
    int update(Sido sido);
    int delete(@Param("seq") Integer seq);
}

