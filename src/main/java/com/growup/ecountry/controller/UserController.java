package com.growup.ecountry.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.growup.ecountry.config.TokenProvider;
import com.growup.ecountry.dto.*;
import com.growup.ecountry.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final TokenProvider jwt;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDTO<NullType>> signup(@RequestBody UserDTO userDTO) {
        Boolean result = userService.create(userDTO);
        String msg = result ? "회원가입에 성공하셨습니다" : "이미 존재하는 회원입니다";
        return ResponseEntity.ok(new ApiResponseDTO<>(result,msg,null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<String>> login(@RequestBody UserDTO userDTO) {
        ApiResponseDTO<Long> result = userService.login(userDTO);
        Token token = result.getSuccess() ? new Token(jwt.generateToken(result.getResult(), false))
                                          : new Token(null);
        return ResponseEntity.ok(new ApiResponseDTO<>(result.getSuccess(), result.getMessage(), token.getToken()));
    }
    //선생님/학생 개인정보조회
    @GetMapping("/info")
    public ResponseEntity<ApiResponseDTO<?>> userInfo(@RequestHeader(value = "Authorization") String token) {
        TokenDTO authToken = jwt.validateToken(token);
        if (authToken.getId() != 0) {
            ApiResponseDTO<?> apiData = userService.userInfo(authToken.getId(), authToken.getIsStudent());
            Object result = apiData.getResult();
            if (result instanceof UserDTO) {
                UserDTO userDTO = (UserDTO) result;
                UserData userData = new UserData(userDTO.getId(), userDTO.getName(), userDTO.getUserId());
                return ResponseEntity.ok(new ApiResponseDTO<>(true, apiData.getMessage(), userData));
            } else if (result instanceof StudentDTO) {
                StudentDTO studentDTO = (StudentDTO) result;
                StudentData studentData = new StudentData(studentDTO.getId(), studentDTO.getName(), studentDTO.getRollNumber(), studentDTO.getRating());
                return ResponseEntity.ok(new ApiResponseDTO<>(true, apiData.getMessage(), studentData));
            }
        }
        return ResponseEntity.ok(new ApiResponseDTO<>(false, "인증 실패", null));
    }

    @GetMapping("/auth")
    public ResponseEntity<ApiResponseDTO<TokenDTO>> authUser(@RequestHeader(value = "Authorization") String token) {
        TokenDTO authToken = jwt.validateToken(token);
        if(authToken.getId() == 0) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "사용자 인증 실패", null));
        } else {
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "사용자 인증 완료", authToken));
        }
    }

    @PatchMapping("/change")
    public ResponseEntity<ApiResponseDTO<NullType>> pwUpdate(@RequestHeader(value= "Authorization") String token, @RequestBody UserDTO userDTO) {
        TokenDTO authToken = jwt.validateToken(token);
        if(authToken.getId() != 0){
            Boolean result = userService.pwUpdate(authToken.getId(), userDTO.getPw());
            String msg = result ? "비밀번호를 성공적으로 변경하였습니다" : "비밀번호 변경에 실패하였습니다";
            return ResponseEntity.ok(new ApiResponseDTO<>(result,msg,null));
        }
        return ResponseEntity.ok(new ApiResponseDTO<>(false,"비밀번호 변경에 실패하였습니다",null));
    }


    //이미지 변경
    @PatchMapping
    public ResponseEntity<ApiResponseDTO<NullType>> imgUpdate(@RequestBody UserDTO userDTO){
        Boolean result = userService.imgUpdate(userDTO);
        String msg = result ? "이미지 변경에 성공하였습니다" : "이미지 변경에 실패하였습니다";
        return ResponseEntity.ok(new ApiResponseDTO<>(result,msg,null));
    }

    //국가리스트조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<List<CountryDTO>>> findCountryList(@PathVariable Long id){
        return ResponseEntity.ok(userService.findCountryList(id));
    }

    static class Token{
        @JsonProperty
        private final String token;
        public Token(String token){
            this.token = token;
        }
        public String getToken(){
            return this.token;
        }
    }
    static class UserData {
        @JsonProperty
        private final Long id;
        @JsonProperty
        private final String name;
        @JsonProperty
        private final String userId;
        public UserData(Long id, String name, String userId) {
            this.id = id;
            this.name = name;
            this.userId = userId;
        }
    }
    static class StudentData {
        @JsonProperty
        private final Long id;
        @JsonProperty
        private final String name;
        @JsonProperty
        private final Integer rollNumber;
        @JsonProperty
        private final Integer rating;
        public StudentData(Long id, String name, Integer rollNumber, Integer rating) {
            this.id = id;
            this.name = name;
            this.rollNumber = rollNumber;
            this.rating = rating;
        }
    }
}

