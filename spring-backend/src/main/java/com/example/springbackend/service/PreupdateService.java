package com.example.springbackend.service;

import com.example.springbackend.dto.update.DriverUpdateDTO;
import com.example.springbackend.model.PreupdateData;
import com.example.springbackend.repository.PreupdateDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreupdateService {
    @Autowired
    private PreupdateDataRepository preupdateDataRepository;
    public List<PreupdateData> getAll() {
        return preupdateDataRepository.findAll();
    }

    public boolean saveUpdateRequest(DriverUpdateDTO driverUpdateDTO) {
        try {
            PreupdateData pd = new PreupdateData();
            pd.setName(driverUpdateDTO.getName());
            pd.setCity(driverUpdateDTO.getCity());
            pd.setSurname(driverUpdateDTO.getSurname());
            pd.setUsername(driverUpdateDTO.getUsername());
            pd.setPhoneNumber(driverUpdateDTO.getPhoneNumber());
            pd.setProfilePicture(driverUpdateDTO.getProfilePicture());
            pd.setMake(driverUpdateDTO.getMake());
            pd.setColour(driverUpdateDTO.getColour());
            pd.setBabySeat(driverUpdateDTO.getBabySeat());
            pd.setPetsAllowed(driverUpdateDTO.getPetsAllowed());
            pd.setModel(driverUpdateDTO.getModel());
            pd.setVehicleType(driverUpdateDTO.getVehicleType());
            pd.setLicensePlateNumber(driverUpdateDTO.getLicensePlateNumber());
            preupdateDataRepository.save(pd);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean removeUpdateRequest(DriverUpdateDTO driverUpdateDTO) {
        try {
            preupdateDataRepository.delete(preupdateDataRepository.findByUsername(driverUpdateDTO.getUsername()).get());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
