package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.SupplierDto;
import com.example.application.entity.Supplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

        Supplier toEntity(SupplierDto supplier);
        SupplierDto toDto(Supplier supplier);
}
