package com.growup.ecountry.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.growup.ecountry.config.TokenProvider;
import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.NoticeDTO;
import com.growup.ecountry.dto.StudentDTO;
import com.growup.ecountry.dto.TokenDTO;
import com.growup.ecountry.service.StudentService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.lang.model.type.NullType;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;
    private final TokenProvider jwt;

    //국민등록(수기)
    @PostMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> studentAdd(@PathVariable Long countryId, @RequestBody List<StudentDTO> studentDTOS){
        return ResponseEntity.ok(studentService.studentAdd(countryId,studentDTOS));
    }
    //국민등록(엑셀)
    @PostMapping("/add/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> studentAddExcel(@PathVariable Long countryId, @RequestParam("file")MultipartFile file) throws IOException {
        return ResponseEntity.ok(studentService.studentAddExcel(countryId,file));
    }

    //국민조회
    @GetMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<StudentData>>> studentList(@PathVariable Long countryId){
        List<StudentData> studentDataList = new ArrayList<>();
        ApiResponseDTO<List<StudentDTO>> apiData = studentService.studentList(countryId);
        List<StudentDTO> students = apiData.getResult();
        for(StudentDTO student : students) {
            StudentData studentData = new StudentData(student.getId(), student.getName(), student.getRollNumber(), student.getRating());
            studentDataList.add(studentData);
        }
        return ResponseEntity.ok(new ApiResponseDTO<>(apiData.getSuccess(), apiData.getMessage(),studentDataList));
    }
    //국민삭제
    @DeleteMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> studentDelete(@PathVariable Long countryId,@RequestBody StudentDTO studentDTO){
        return ResponseEntity.ok(studentService.studentDelete(countryId,studentDTO.getId()));
    }
    //국민수정
    @PatchMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> studentUpdate(@PathVariable Long countryId,@RequestBody StudentDTO studentDTO){
        return ResponseEntity.ok(studentService.studentUpdate(countryId,studentDTO));
    }
    //학생로그인
    @PostMapping("/user/{countryId}")
    public ResponseEntity<ApiResponseDTO<String>> studentLogin(@PathVariable Long countryId,@RequestBody StudentDTO studentDTO){
        ApiResponseDTO<Long> result = studentService.studentLogin(countryId,studentDTO);
        Token token = result.getSuccess() ? new Token(jwt.generateToken(result.getResult(),true)) : new Token(null);
        return ResponseEntity.ok(new ApiResponseDTO<>(result.getSuccess(), result.getMessage(), token.getToken()));
    }
    //학생비밀번호 변경
    @PatchMapping("/user")
    public ResponseEntity<ApiResponseDTO<NullType>> studentPwUpdate(@RequestHeader("Authorization") String token,@RequestBody StudentDTO studentDTO){
        TokenDTO authToken = jwt.validateToken(token);
        if(authToken != null) {
            return ResponseEntity.ok(studentService.studentPwUpdate(authToken.getId(), studentDTO));
        }
        else {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"사용자 인증에 실패하였습니다",null));
        }
    }
    //학생이미지 수정
    @PatchMapping("/user/img/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> studentImgUpdate(@PathVariable Long countryId,@RequestBody StudentDTO studentDTO){
        return ResponseEntity.ok(studentService.studentImgUpdate(countryId,studentDTO));
    }
    //알림조회
    @GetMapping("/notice")
    public ResponseEntity<ApiResponseDTO<List<NoticeData>>> noticeCheck(@RequestHeader("Authorization") String token){
        List<NoticeData> noticeDataList = new ArrayList<>();
        TokenDTO authToken = jwt.validateToken(token);
        if(authToken != null){
            ApiResponseDTO<List<NoticeDTO>> apiData = studentService.noticeList(authToken.getId());
            List<NoticeDTO> notices = apiData.getResult();
            for(NoticeDTO notice : notices) {
                if(notice.getIsChecked() == false){
                    NoticeData noticeData = new NoticeData(notice.getId(), notice.getContent(), 1, notice.getCreatedAt());
                    noticeDataList.add(noticeData);
                }
                else { // 이미 조회한걸 다시 보는 경우
                    NoticeData noticeData = new NoticeData(notice.getId(), notice.getContent(), 1, notice.getCreatedAt());
                    noticeDataList.add(noticeData);
                }
            }
            return ResponseEntity.ok(new ApiResponseDTO<>(apiData.getSuccess(), apiData.getMessage(),noticeDataList));
        }
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"사용자 인증에 실패하였습니다",noticeDataList));
    }
    //알림추가
    @PostMapping("/notice/add/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> noticeAdd(@PathVariable Long countryId,@RequestBody NoticeDTO noticeDTO){
        return ResponseEntity.ok(studentService.noticeAdd(countryId,noticeDTO));
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

    static class NoticeData {
        @JsonProperty
        private final Long id;
        @JsonProperty
        private final String content;
        @JsonProperty
        private final Integer isChecked;
        @JsonProperty
        private final Date createAt;
        public NoticeData(Long id, String content, Integer isChecked, Date createAt) {
            this.id = id;
            this.content = content;
            this.isChecked = isChecked;
            this.createAt = createAt;
        }
    }

    //토큰 발급
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
}
