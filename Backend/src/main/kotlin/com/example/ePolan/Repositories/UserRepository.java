package com.example.ePolan.Repositories;

import com.example.ePolan.Model.Entities.Point;
import com.example.ePolan.Model.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
