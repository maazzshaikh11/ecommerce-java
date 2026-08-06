package com.ecommerce.controller;

public record RegisterRequest(String email, String password, String firstName, String lastName) {}
