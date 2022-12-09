package com.example.springbackend.util;

import com.example.springbackend.dto.display.*;
import com.example.springbackend.model.*;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DomainMapper {
    @Bean
    public ModelMapper ModelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        configure(modelMapper);
        addConverters(modelMapper);

        return modelMapper;
    }
    private void configure(ModelMapper modelMapper) {
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
    }


    private void addConverters(ModelMapper modelMapper) {
        Converter<Driver, AccountDisplayDTO> driverToAccountDisplayDtoConverter = context -> {
            Driver source = context.getSource();
            DriverAccountDisplayDTO destination = modelMapper.map(source, DriverAccountDisplayDTO.class);
            destination.setAccountType("driver");
            return destination;
        };
        Converter<Passenger, AccountDisplayDTO> passengerToAccountDisplayDtoConverter = context -> {
            Passenger source = context.getSource();
            PassengerAccountDisplayDTO destination = modelMapper.map(source, PassengerAccountDisplayDTO.class);
            destination.setAccountType("passenger");
            return destination;
        };
        Converter<Admin, AccountDisplayDTO> adminToAccountDisplayDtoConverter = context -> {
            AccountDisplayDTO destination = context.getDestination();
            destination.setAccountType("admin");
            return destination;
        };

        Converter<Driver, SessionDisplayDTO> driverToSessionDisplayDtoConverter = context -> {
            SessionDisplayDTO destination = context.getDestination();
            destination.setAccountType("driver");
            return destination;
        };
        Converter<Passenger, SessionDisplayDTO> passengerToSessionDisplayDtoConverter = context -> {
            SessionDisplayDTO destination = context.getDestination();
            destination.setAccountType("passenger");
            return destination;
        };
        Converter<Admin, SessionDisplayDTO> adminToSessionDisplayDtoConverter = context -> {
            SessionDisplayDTO destination = context.getDestination();
            destination.setAccountType("admin");
            return destination;
        };

        modelMapper.createTypeMap(Driver.class, AccountDisplayDTO.class).setPostConverter(driverToAccountDisplayDtoConverter);
        modelMapper.createTypeMap(Passenger.class, AccountDisplayDTO.class).setPostConverter(passengerToAccountDisplayDtoConverter);
        modelMapper.createTypeMap(Admin.class, AccountDisplayDTO.class).setPostConverter(adminToAccountDisplayDtoConverter);
        modelMapper.createTypeMap(Driver.class, SessionDisplayDTO.class).setPostConverter(driverToSessionDisplayDtoConverter);
        modelMapper.createTypeMap(Passenger.class, SessionDisplayDTO.class).setPostConverter(passengerToSessionDisplayDtoConverter);
        modelMapper.createTypeMap(Admin.class, SessionDisplayDTO.class).setPostConverter(adminToSessionDisplayDtoConverter);
    }
}
