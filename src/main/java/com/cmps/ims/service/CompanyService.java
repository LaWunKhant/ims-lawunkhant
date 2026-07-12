package com.cmps.ims.service;

import com.cmps.ims.entity.Company;
import com.cmps.ims.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    public Company findById(Integer id) {
        return companyRepository.findById(id).orElseThrow();
    }

    public void save(Company company) {
        companyRepository.save(company);
    }

    public void deleteById(Integer id) {
        companyRepository.deleteById(id);
    }

    public List<Company> search(String strCode, String endCode, String companyName, String tel) {
        List<Company> all = companyRepository.findAll();
        return all.stream()
            .filter(c -> (strCode == null || strCode.isEmpty() || c.getCompanyCode().compareTo(strCode) >= 0))
            .filter(c -> (endCode == null || endCode.isEmpty() || c.getCompanyCode().compareTo(endCode) <= 0))
            .filter(c -> (companyName == null || companyName.isEmpty() || c.getCompanyName().contains(companyName)))
            .filter(c -> (tel == null || tel.isEmpty() || (c.getTel() != null && c.getTel().contains(tel))))
            .collect(Collectors.toList());
    }
}