package com.cmps.ims.controller;

import com.cmps.ims.entity.Company;
import com.cmps.ims.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/supplier")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String str_code,
            @RequestParam(required = false) String end_code,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String tel,
            Model model) {

        // ① validate code format - numbers only
        if (str_code != null && !str_code.isEmpty() && !str_code.matches("[0-9]+")) {
            model.addAttribute("errorMessage", "企業コードは数字で入力してください。");
            model.addAttribute("companies", List.of());
            return "supplier/index";
        }
        if (end_code != null && !end_code.isEmpty() && !end_code.matches("[0-9]+")) {
            model.addAttribute("errorMessage", "企業コードは数字で入力してください。");
            model.addAttribute("companies", List.of());
            return "supplier/index";
        }
        
        if (tel != null && !tel.isEmpty() && !tel.matches("[0-9\\-]+")) {
            model.addAttribute("errorMessage", "電話番号は半角数字とハイフン(-)で入力してください。");
            model.addAttribute("companies", List.of());
            return "supplier/index";
        }

        // ② validate range order
        if (str_code != null && !str_code.isEmpty()
                && end_code != null && !end_code.isEmpty()
                && str_code.compareTo(end_code) > 0) {
            model.addAttribute("errorMessage", "企業コードの範囲が正しくありません。開始コードは終了コード以下を入力してください。");
            model.addAttribute("companies", List.of());
            return "supplier/index";
        }

        List<Company> companies = companyService.search(str_code, end_code, companyName, tel);
        model.addAttribute("companies", companies);
        return "supplier/index";
    }
    
    @GetMapping("/entry")
    public String entry(Model model) {
        model.addAttribute("company", new Company());
        return "supplier/entry";
    }

    @PostMapping("/entry")
    public String save(@Valid @ModelAttribute Company company,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "supplier/entry";
        }
        try {
            boolean isNew = company.getId() == null;
            if (isNew) {
                company.setCreatedMemberId(1);
            }
            company.setUpdateMemberId(1);
            companyService.save(company);

            if (isNew) {
                redirectAttributes.addFlashAttribute("successMessage", "企業を新規登録しました。");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "企業情報を更新しました。");
            }
            return "redirect:/supplier";

        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", "この企業コードはすでに登録されています。別のコードを入力してください。");
            return "supplier/entry";
        }
    }

    @GetMapping("/entry/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Company company = companyService.findById(id);
        model.addAttribute("company", company);
        return "supplier/entry";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        companyService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "企業を削除しました。");
        return "redirect:/supplier";
    }
}