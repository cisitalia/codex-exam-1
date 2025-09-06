package kr.co.ldk.sido.service;

import kr.co.ldk.sido.domain.Sido;
import kr.co.ldk.sido.mapper.SidoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SidoService {
    private final SidoMapper mapper;

    public SidoService(SidoMapper mapper) {
        this.mapper = mapper;
    }

    public Sido get(Integer seq) {
        return mapper.findById(seq);
    }

    public int count(String keyword) {
        return mapper.count(keyword);
    }

    public List<Sido> page(String keyword, int page, int size) {
        int p = Math.max(1, page);
        int s = Math.max(1, size);
        int offset = (p - 1) * s;
        return mapper.findPage(keyword, offset, s);
    }

    public void create(Sido s) {
        mapper.insert(s);
    }

    public void update(Sido s) {
        mapper.update(s);
    }

    public void delete(Integer seq) {
        mapper.delete(seq);
    }
}

