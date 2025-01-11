package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.Loan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CopyMapper.class, UserMapper.class})
public interface LoanMapper {

        Loan toEntity(LoanDto loan);
        LoanDto toDto(Loan loan);
}
