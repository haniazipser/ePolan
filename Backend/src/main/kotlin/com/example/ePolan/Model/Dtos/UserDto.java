package com.example.ePolan.Model.Dtos;

import com.example.ePolan.Model.Entities.Participant;
import com.example.ePolan.Model.Entities.User;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class UserDto {
    String id;
    String email;
    String name;
    String surname;

    public UserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getFirstName();
        this.surname = user.getLastName();

    }
}
