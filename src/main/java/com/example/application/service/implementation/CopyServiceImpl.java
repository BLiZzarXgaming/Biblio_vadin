package com.example.application.service.implementation;

import com.example.application.entity.Copy;
import com.example.application.entity.Item;
import com.example.application.repository.CopyRepository;
import com.example.application.repository.ItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CopyServiceImpl {

    private CopyRepository copyRepository;
    private ItemRepository itemRepository;

    public CopyServiceImpl(CopyRepository copyRepository, ItemRepository itemRepository) {
        this.copyRepository = copyRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public void save(Copy copy) {
        Item item = copy.getItem();
        if (item != null && item.getId() == null) {
            itemRepository.save(item);
        }
        copyRepository.save(copy);
    }


}
