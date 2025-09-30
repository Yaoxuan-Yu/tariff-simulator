package com.example.tariff.dto;

public class LoginResponse {
    private boolean success;
    private UserDto user;
    private String error;

    public LoginResponse() {}

    public LoginResponse(boolean success, UserDto user) {
        this.success = success;
        this.user = user;
    }

    public LoginResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public static class UserDto {
        private String id;
        private String name;
        private String email;

        public UserDto() {}

        public UserDto(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
