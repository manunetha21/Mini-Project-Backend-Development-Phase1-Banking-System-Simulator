package com.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.model.Transaction;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findBySourceAccountOrDestinationAccount(String src, String dest);
}
