package com.example.data.converter.service;

import org.springframework.data.repository.CrudRepository;

import com.example.data.converter.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

}
