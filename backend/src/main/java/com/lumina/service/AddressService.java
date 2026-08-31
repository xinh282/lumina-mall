package com.lumina.service;

import com.lumina.dto.AddressDTO;
import com.lumina.entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> listByUser(Long userId);
    Address getDefault(Long userId);
    void save(Long userId, AddressDTO dto);
    void delete(Long id, Long userId);
}
