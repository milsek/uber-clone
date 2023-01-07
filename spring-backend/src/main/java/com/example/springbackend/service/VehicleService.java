package com.example.springbackend.service;

import com.example.springbackend.dto.display.VehiclePositionDisplayDTO;
import com.example.springbackend.dto.display.VehicleTypeDisplayDTO;
import com.example.springbackend.model.VehicleType;
import com.example.springbackend.repository.VehicleRepository;
import com.example.springbackend.repository.VehicleTypeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class VehicleService {

    @Autowired
    VehicleRepository vehicleRepository;
    @Autowired
    VehicleTypeRepository vehicleTypeRepository;
    @Autowired
    private ModelMapper modelMapper;

    public Collection<VehicleTypeDisplayDTO> getAllTypes() {
        Collection<VehicleType> vehicleTypes = vehicleTypeRepository.findAll();
        return vehicleTypes.stream().map(vehicleType -> modelMapper
                .map(vehicleType, VehicleTypeDisplayDTO.class)).toList();
    }

    public Collection<VehiclePositionDisplayDTO> getPositions() {
        return vehicleRepository.findVehiclesWhoseDriversAreActive().stream().map(vehicle -> modelMapper
                .map(vehicle, VehiclePositionDisplayDTO.class)).toList();
    }

}
