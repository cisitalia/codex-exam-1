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

    public SidoController(SidoService service) {
        this.service = service;
    }

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

        int currentPage = Math.max(1, page);
        int blockSize = 10;
        int blockStart = ((currentPage - 1) / blockSize) * blockSize + 1;
        int blockEnd = Math.min(blockStart + blockSize - 1, Math.max(totalPages, 1));
        int prevPage = Math.max(1, currentPage - 1);
        int nextPage = Math.min(Math.max(totalPages, 1), currentPage + 1);

        model.addAttribute("rows", rows);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", size);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("blockStart", blockStart);
        model.addAttribute("blockEnd", blockEnd);
        model.addAttribute("prevPage", prevPage);
        model.addAttribute("nextPage", nextPage);
        model.addAttribute("firstPage", 1);
        model.addAttribute("lastPage", Math.max(totalPages, 1));

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
