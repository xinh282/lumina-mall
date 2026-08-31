package com.lumina.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumina.dto.AddressDTO;
import com.lumina.entity.Address;
import com.lumina.mapper.AddressMapper;
import java.util.List;
import com.lumina.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    public Address getDefault(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
               .eq(Address::getIsDefault, 1)
               .last("LIMIT 1");
        Address addr = addressMapper.selectOne(wrapper);
        if (addr != null) return addr;

        // 没有默认地址时，返回最近一次使用的地址
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
               .orderByDesc(Address::getUpdateTime)
               .last("LIMIT 1");
        return addressMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public void save(Long userId, AddressDTO dto) {
        boolean setDefault = dto.getSaveAsDefault() != null && dto.getSaveAsDefault() == 1;

        // 如果设为默认，先取消旧默认
        if (setDefault) {
            Address oldDefault = getDefault(userId);
            if (oldDefault != null) {
                oldDefault.setIsDefault(0);
                addressMapper.updateById(oldDefault);
            }
        }

        // 查找是否已有相同地址记录
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
               .eq(Address::getReceiverName, dto.getReceiverName())
               .eq(Address::getReceiverPhone, dto.getReceiverPhone())
               .eq(Address::getReceiverAddress, dto.getReceiverAddress())
               .last("LIMIT 1");
        Address existing = addressMapper.selectOne(wrapper);

        if (existing != null) {
            // 已有相同地址，更新默认标记
            if (setDefault && existing.getIsDefault() != 1) {
                existing.setIsDefault(1);
                addressMapper.updateById(existing);
            }
            return;
        }

        Address addr = new Address();
        addr.setUserId(userId);
        addr.setReceiverName(dto.getReceiverName());
        addr.setReceiverPhone(dto.getReceiverPhone());
        addr.setReceiverAddress(dto.getReceiverAddress());
        addr.setIsDefault(setDefault ? 1 : 0);
        addressMapper.insert(addr);
    }

    @Override
    public List<Address> listByUser(Long userId) {
        LambdaQueryWrapper<Address> w = new LambdaQueryWrapper<>();
        w.eq(Address::getUserId, userId).orderByDesc(Address::getIsDefault).orderByDesc(Address::getCreateTime);
        return addressMapper.selectList(w);
    }

    @Override
    public void delete(Long id, Long userId) {
        Address addr = addressMapper.selectById(id);
        if (addr != null && addr.getUserId().equals(userId)) {
            addressMapper.deleteById(id);
        }
    }
}
