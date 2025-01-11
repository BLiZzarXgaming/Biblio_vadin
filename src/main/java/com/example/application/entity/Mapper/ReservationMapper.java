package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.ReservationDto;
import com.example.application.entity.Reservation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CopyMapper.class, UserMapper.class})
public interface ReservationMapper {

    Reservation toEntity(ReservationDto reservation);
    ReservationDto toDto(Reservation reservation);
}
