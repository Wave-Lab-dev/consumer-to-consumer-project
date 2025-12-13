package com.example.yongeunmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.yongeunmarket.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
