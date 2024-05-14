package com.growup.ecountry.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.growup.ecountry.dto.AccountListDTO;
import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.CountryDTO;
import com.growup.ecountry.dto.NoticeDTO;
import com.growup.ecountry.dto.StudentDTO;
import com.growup.ecountry.entity.*;
import com.growup.ecountry.repository.AccountListRepository;
import com.growup.ecountry.repository.AccountRepository;
import com.growup.ecountry.repository.CountryRepository;
import com.growup.ecountry.repository.NoticeRepository;
import com.growup.ecountry.repository.StudentRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.lang.model.type.NullType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final CountryRepository countryRepository;
    private final AccountRepository accountRepository;
    private final AccountListRepository accountListRepository;
    private final NoticeRepository noticeRepository;
    //국민등록(수기)
    public ApiResponseDTO<NullType> studentAdd(Long countryId, List<StudentDTO> students){
        Optional<Countries> countryExist = countryRepository.findById(countryId);
        if(countryExist.isPresent()){
            Countries countries = countryExist.get();
            for(StudentDTO student : students){
                Students studentEntity = Students.builder()
                        .name(student.getName())
                        .rollNumber(student.getRollNumber())
                        .pw(student.getPw())
                        .img(student.getImg())
                        .countryId(countries.getId()).build();
                studentRepository.save(studentEntity);
                List<AccountLists> accountInfo = accountListRepository.findByCountryIdAndDivisionAndAvailable(countryId, false, true);
                Accounts accounts = Accounts.builder()
                        .balance(0).accountListId(accountInfo.get(0).getId())
                        .studentId(studentEntity.getId()).build();
                        accountRepository.save(accounts);
            }
            return new ApiResponseDTO<>(true,"국민등록 성공",null);
        }
        else {
            return new ApiResponseDTO<>(false,"국가가 존재하지 않습니다",null);
        }
    }
    //국민등록(엑셀)
    public ApiResponseDTO<NullType> studentAddExcel(Long countryId, MultipartFile file) throws IOException {
        Optional<Countries> countryExist = countryRepository.findById(countryId);
        if(countryExist.isPresent()){
            Countries countries = countryExist.get();
            String fileName = file.getOriginalFilename();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (!extension.equals("csv")) {
                throw new IOException("CSV파일만 업로드 해주세요.");
            }
            try (InputStream inputStream = file.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                 String line;
                 reader.readLine();

                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");
                    //columns[0]: name, columns[1]: roll number,columns[2]: password
                    Students student = Students.builder()
                                    .name(columns[0])
                                    .rollNumber(Integer.parseInt(columns[1]))
                                    .pw(columns[2])
                                    .countryId(countries.getId()).build();
                    studentRepository.save(student);
                    Accounts accounts = Accounts.builder()
                            .balance(0)
                            .studentId(student.getId()).build();
                    accountRepository.save(accounts);
                }
                return new ApiResponseDTO<>(true,"국민등록 성공",null);
            }
        }
        else {
            return new ApiResponseDTO<>(false,"국가가 존재하지 않습니다",null);
        }
    }
    //국민조회
    public ApiResponseDTO<List<StudentDTO>> studentList(Long countryId){
            List<Students> students = studentRepository.findAllByCountryId(countryId);
            List<StudentDTO> studentDTOList = new ArrayList<>();
            for(Students student : students){
                StudentDTO studentDTO = StudentDTO.builder()
                        .id(student.getId())
                        .name(student.getName())
                        .rollNumber(student.getRollNumber())
                        .rating(student.getRating())
                        .build();
                studentDTOList.add(studentDTO);
            }
            return new ApiResponseDTO<>(true,"국민조회 성공",studentDTOList);
    }
    //국민삭제
    public ApiResponseDTO<NullType> studentDelete(Long countryId,Long id){
        Optional<Students> studentExist = studentRepository.findByIdANDCountryId(id,countryId);
        if(studentExist.isPresent()){
            Students student = studentExist.get();
            studentRepository.deleteById(student.getId());
            return new ApiResponseDTO<>(true,"국민삭제 성공",null);
        }
        else {
            return new ApiResponseDTO<>(false,"국민이 존재하지 않습니다",null);
        }
    }
    //국민수정
    public ApiResponseDTO<NullType> studentUpdate(Long countryId,StudentDTO studentDTO){
        Students student = studentRepository.findByIdANDCountryId(studentDTO.getId(),countryId).orElseThrow(()->{
            throw new IllegalArgumentException("학생 정보 혹은 국가 아이디가 존재하지 않습니다");
        });
        student = Students.builder()
                    .id(studentDTO.getId())
                    .name(studentDTO.getName())
                    .rollNumber(studentDTO.getRollNumber())
                    .pw(studentDTO.getPw())
                    .rating(studentDTO.getRating())
                    .countryId(student.getCountryId()).build();
            studentRepository.save(student);
            return new ApiResponseDTO<>(true,"국민수정 성공",null);
    }
    //학생로그인
    public ApiResponseDTO<Long> studentLogin(Long countryId,StudentDTO studentDTO){
            Optional<Countries> countryExist = countryRepository.findById(countryId);
            if(countryExist.isPresent()){
                Optional<Students> studentExist = studentRepository.findByNameANDPw(studentDTO.getName(),studentDTO.getPw());
                if(studentExist.isPresent()){
                    Students students = studentExist.get();
                    return new ApiResponseDTO<>(true,"학생 로그인 성공",students.getId());
                }
                else {
                    return new ApiResponseDTO<>(false,"사용자 정보가 일치하지 않습니다",null);
                }
            }
            else {
                return new ApiResponseDTO<>(false,"국가가 존재하지 않습니다",null);
            }
    }
    //학생비밀번호 변경
    public ApiResponseDTO<NullType> studentPwUpdate(Long id,StudentDTO studentDTO){
        Optional<Students> studentExist = studentRepository.findById(id);
        if(studentExist.isPresent()){
            Students student = studentExist.get();
            student = Students.builder()
                    .id(student.getId())
                    .name(student.getName())
                    .rollNumber(student.getRollNumber())
                    .pw(studentDTO.getPw())
                    .rating(student.getRating())
                    .countryId(student.getCountryId()).build();
            studentRepository.save(student);
            return new ApiResponseDTO<>(true,"비밀번호 변경 성공",null);
        }
        else {
            return new ApiResponseDTO<>(false,"국민이 존재하지 않습니다",null);
        }
    }
    //학생이미지 수정
    public ApiResponseDTO<NullType> studentImgUpdate(Long countryId,StudentDTO studentDTO){
        Optional<Students> studentExist = studentRepository.findByIdANDCountryId(studentDTO.getId(),countryId);
        if(studentExist.isPresent()){
            Students student = studentExist.get();
            student = Students.builder()
                    .id(student.getId())
                    .name(student.getName())
                    .rollNumber(student.getRollNumber())
                    .pw(student.getPw())
                    .rating(student.getRating())
                    .countryId(student.getCountryId())
                    .img(studentDTO.getImg()).build();
            studentRepository.save(student);
            return new ApiResponseDTO<>(true,"이미지 변경 성공",null);
        }
        else {
            return new ApiResponseDTO<>(false,"국민이 존재하지 않습니다",null);
        }
    }
    //알림조회
    public ApiResponseDTO<List<NoticeDTO>> noticeList(Long studentId){
        List<NoticeDTO> noticeDTOList = new ArrayList<>();
        Optional<Students> studentExist = studentRepository.findById(studentId);
        if(studentExist.isPresent()){
            Students student = studentExist.get();
            List<Notice> noticeList = noticeRepository.findAllByStudentId(student.getId());
            for(Notice notice : noticeList){
                NoticeDTO noticeDTO = NoticeDTO.builder()
                        .id(notice.getId())
                        .content(notice.getContent())
                        .isChecked(notice.getIsChecked())
                        .createdAt(notice.getCreatedAt())
                        .studentId(List.of(notice.getStudentId()))
                        .build();
                noticeDTOList.add(noticeDTO);
                //알림 확인으로 update
                Notice updateNotice = Notice.builder()
                        .id(notice.getId())
                        .content(notice.getContent())
                        .isChecked(true)
                        .createdAt(notice.getCreatedAt())
                        .studentId(notice.getStudentId())
                        .build();
                noticeRepository.save(updateNotice);
            }
            return new ApiResponseDTO<>(true,"알림조회 성공",noticeDTOList);
        }
        else {
            return new ApiResponseDTO<>(false,"알림 조회 실패: 국민이 존재하지 않습니다",null);
        }
    }
    //알림추가(국가ID?)
    public ApiResponseDTO<NullType> noticeAdd(Long countryId,NoticeDTO noticeDTO){
        Optional<Countries> countryExist = countryRepository.findById(countryId);
        if(countryExist.isPresent()){
            for(Long studentId : noticeDTO.getStudentId()){
                Optional<Students> studentExist = studentRepository.findById(studentId);
                if (studentExist.isPresent()) {
                    Students student = studentExist.get();
                    Notice notice = Notice.builder()
                            .content(noticeDTO.getContent())
                            .studentId(student.getId())
                            .build();
                    noticeRepository.save(notice);
                }
            }
            return new ApiResponseDTO<>(true,"알림이 발송되었습니다",null);
        }
        else {
            return new ApiResponseDTO<>(false,"알림 발송에 실패하였습니다",null);
        }
    }
//    // Date를 문자열로 형식화하는 함수
//    private Date formatDate(Date date) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String formattedDate = sdf.format(date);
//        try {
//            return sdf.parse(formattedDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
