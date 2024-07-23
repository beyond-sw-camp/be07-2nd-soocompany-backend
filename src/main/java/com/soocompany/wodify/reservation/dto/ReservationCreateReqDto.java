package com.soocompany.wodify.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.soocompany.wodify.box.domain.Box;
import com.soocompany.wodify.member.domain.Member;
import com.soocompany.wodify.reservation.domain.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateReqDto {
    private Long boxId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @JsonFormat(pattern = "kk:mm:ss")
    private LocalTime time;
    private int maximumPeople;
    private Long coachId;


    public Reservation toEntity(Box box) {
        return Reservation.builder()
                .box(box)
                .date(this.date)
                .time(this.time)
                .maximumPeople(this.maximumPeople)
                .availablePeople(this.maximumPeople)
                .build();
    }
}