package com.example.application.service.implementation;

import com.example.application.entity.Supplier;
import com.example.application.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierServiceImpl {

    private SupplierRepository supplierRepository;

    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier findFirstByName(String name) {
        return supplierRepository.findFirstByName(name);
    }

    public int save(Supplier supplier) {
        Supplier existingSupplier = supplierRepository.findFirstByName(supplier.getName());
        if (existingSupplier != null) {
            return 0;
        }
        supplierRepository.save(supplier);
        return 1;
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id).orElse(null);
    }
}
