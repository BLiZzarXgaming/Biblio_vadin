package com.example.application.service;

import com.example.application.entity.DTO.SupplierDto;
import com.example.application.entity.Mapper.SupplierMapper;
import com.example.application.entity.Supplier;

import com.example.application.repository.SupplierRepositoryV2;
import com.example.application.service.implementation.SupplierServiceV2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImplV2 implements SupplierServiceV2 {
    private final SupplierRepositoryV2 supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierServiceImplV2(SupplierRepositoryV2 supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public List<SupplierDto> findAll() {
        return supplierRepository.findAll().stream().map(supplierMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<SupplierDto> findById(Long id) {
        return supplierRepository.findById(id).map(supplierMapper::toDto);
    }

    @Override
    public Optional<SupplierDto> findByName(String name) {
        return supplierRepository.findByName(name).map(supplierMapper::toDto);
    }

    @Override
    public Optional<SupplierDto> findByContactInfo(String contactInfo) {
        return supplierRepository.findByContactInfo(contactInfo).map(supplierMapper::toDto);
    }

    @Override
    public SupplierDto save(SupplierDto supplier) {
        // Vérification d'unicité pour le nom ou les informations de contact
        Optional<Supplier> existingSupplierByName = supplierRepository.findByName(supplier.getName());
        if (existingSupplierByName.isPresent()) {
            throw new IllegalArgumentException("A supplier with the same name already exists.");
        }

        Optional<Supplier> existingSupplierByContactInfo = supplierRepository.findByContactInfo(supplier.getContactInfo());
        if (existingSupplierByContactInfo.isPresent()) {
            throw new IllegalArgumentException("A supplier with the same contact information already exists.");
        }

        return supplierMapper.toDto(supplierRepository.save(supplierMapper.toEntity(supplier)));
    }

    @Override
    public void deleteById(Long id) {
        supplierRepository.deleteById(id);
    }
}
