package unam.fes.model;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String username;
    private String password;
}