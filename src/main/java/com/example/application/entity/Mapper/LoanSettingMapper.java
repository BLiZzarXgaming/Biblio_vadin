package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.LoanSettingDto;
import com.example.application.entity.LoanSetting;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanSettingMapper {

        LoanSetting toEntity(LoanSettingDto loanSetting);
        LoanSettingDto toDto(LoanSetting loanSetting);
}
