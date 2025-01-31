package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {

    @PostMapping("/test")
    public ResponseMessage testEndpoint(@RequestBody InputValue inputValue) {
        return new ResponseMessage("Success! Received value: " + inputValue.getValue());
    }

    public static class InputValue {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class ResponseMessage {
        private String message;

        public ResponseMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
} 