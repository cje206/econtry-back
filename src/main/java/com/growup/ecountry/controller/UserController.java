package com.growup.ecountry.controller;

import com.growup.ecountry.dto.ResponseDTO;
import com.growup.ecountry.dto.UserDTO;
import com.growup.ecountry.entity.Users;
import com.growup.ecountry.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Users> signup(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.create(userDTO));
    }

    //UserDTO 타입 → ResponseDTO 타입 + Security에서 제공하는 Login 기능 써야됨..
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@RequestBody UserDTO userDTO) {
            //success + message 반환
            return ResponseEntity.ok(userService.findByUserIdAndPw(userDTO));
    }
    //@PutMapping("/change")

    @PutMapping
    public ResponseEntity<ResponseDTO> imgUpdate(@RequestBody UserDTO userDTO){
        return ResponseEntity.ok(userService.imgUpdateService(userDTO));
    }
}