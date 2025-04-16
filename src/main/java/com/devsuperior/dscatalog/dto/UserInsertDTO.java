package com.devsuperior.dscatalog.dto;

public class UserInsertDTO extends UserDTO {

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserInsertDTO() {
        super();
    }

    public UserInsertDTO(String password) {
        this.password = password;
    }
}
