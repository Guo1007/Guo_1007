package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.UserAddress;

public interface IAddressService {

    Result getAddressList(Long userId);

    Result saveAddress(UserAddress addr, Long userId);

    Result deleteAddress(Long id);

    Result setDefaultAddress(Long id, Long userId);
}
