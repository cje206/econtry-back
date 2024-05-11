package com.growup.ecountry.controller;

import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.SeatDTO;
import com.growup.ecountry.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.util.List;

@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
public class SeatController {
    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<NullType>> setSeat(@RequestBody List<SeatDTO> seatDTOS) {
        seatService.createSeat(seatDTOS);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "자리 배치 등록 완료"));
    }

    @GetMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<SeatDTO>>> getSeat(@PathVariable Long countryId) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "자리 배치 정보 조회 완료", seatService.getSeat(countryId)));
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponseDTO<NullType>> updateSeat(@RequestBody List<SeatDTO> seatDTOS) {
        seatService.updateSeat(seatDTOS);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "자리 배치 등록 완료"));
    }
}