package com.shoptracker.expense.service;

import com.shoptracker.expense.dto.BuyerRequest;
import com.shoptracker.expense.exception.DuplicateBuyerException;
import com.shoptracker.expense.exception.ResourceNotFoundException;
import com.shoptracker.expense.model.Buyer;
import com.shoptracker.expense.service.excel.ExcelStorageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuyerService {

    private final ExcelStorageService storage;

    public BuyerService(ExcelStorageService storage) {
        this.storage = storage;
    }

    public List<Buyer> getAll() {
        return storage.getAllBuyers();
    }

    public Buyer getByName(String name) {
        return storage.findBuyer(name)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found: " + name));
    }

    public List<Buyer> search(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return storage.getAllBuyers().stream()
                .filter(b -> b.getName().toLowerCase().contains(q)
                        || (b.getMobile() != null && b.getMobile().contains(q)))
                .toList();
    }

    /** Creating a buyer also creates their dedicated worksheet on the next save. */
    public Buyer create(BuyerRequest req) {
        if (storage.buyerExists(req.getName())) {
            throw new DuplicateBuyerException("A buyer named '" + req.getName() + "' already exists");
        }
        Buyer buyer = Buyer.builder()
                .name(req.getName().trim())
                .mobile(req.getMobile())
                .address(req.getAddress())
                .openingBalance(req.getOpeningBalance())
                .currentBalance(req.getOpeningBalance())
                .build();
        return storage.addBuyer(buyer);
    }
}
