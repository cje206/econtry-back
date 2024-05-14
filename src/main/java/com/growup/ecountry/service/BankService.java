package com.growup.ecountry.service;

import com.growup.ecountry.dto.BankDTO;
import com.growup.ecountry.entity.Banks;
import com.growup.ecountry.entity.Students;
import com.growup.ecountry.repository.AccountRepository;
import com.growup.ecountry.repository.BankRepository;
import com.growup.ecountry.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankService {
    private AccountRepository accountRepository;
    private BankRepository bankRepository;
    private StudentRepository studentRepository;

    public String getStudentName(Long accountId) {
        Long studentId = accountRepository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID 입니다.")).getStudentId();
        return studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID 입니다.")).getName();
    }

    public BankDTO createBank(BankDTO bankDTO) {
        Banks result = bankRepository.save(Banks.builder().transaction(bankDTO.getTransaction())
                .memo(bankDTO.getMemo()).depositId(bankDTO.getDepositId())
                .withdrawId(bankDTO.getWithdrawId()).build());
        return BankDTO.builder().id(result.getId()).transaction(result.getTransaction())
                .createdAt(result.getCreatedAt()).memo(result.getMemo()).depositId(result.getDepositId())
                .withdrawId(result.getWithdrawId()).depositName(getStudentName(result.getDepositId())).build();
    }

    public List<BankDTO> getBank(Long accountId) {
        return bankRepository.findByDepositIdOrWithdrawId(accountId, accountId).stream().map(list -> BankDTO.builder()
                .id(list.getId()).transaction(list.getTransaction()).createdAt(list.getCreatedAt())
                .memo(list.getMemo()).isPenalty(list.getIsPenalty()).depositId(list.getDepositId()).withdrawId(list.getWithdrawId())
                .depositName(getStudentName(list.getDepositId())).withdrawName(getStudentName(list.getWithdrawId())).build()).collect(Collectors.toList());
    }

//    public List<BankDTO> getBankList
}
