package com.example.application.service.implementation;

import com.example.application.entity.DTO.SupplierDto;
import com.example.application.entity.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierServiceV2 {
    List<SupplierDto> findAll();
    Optional<SupplierDto> findById(Long id);
    Optional<SupplierDto> findByName(String name);
    Optional<SupplierDto> findByContactInfo(String contactInfo);
    SupplierDto save(SupplierDto supplier);
    void deleteById(Long id);
}
