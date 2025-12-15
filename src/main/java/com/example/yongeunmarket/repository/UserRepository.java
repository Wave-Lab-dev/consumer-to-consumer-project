package com.example.yongeunmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.yongeunmarket.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
