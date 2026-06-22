package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.UserAddress;
import gcy.system.service.IAddressService;
import gcy.system.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Resource
    private IAddressService addressService;

    @GetMapping("/list")
    public Result list() {
        Long userId = UserHolder.getUser().getId();
        return addressService.getAddressList(userId);
    }

    @PostMapping("/save")
    public Result save(@RequestBody UserAddress addr) {
        Long userId = UserHolder.getUser().getId();
        return addressService.saveAddress(addr, userId);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return addressService.deleteAddress(id);
    }

    @PutMapping("/default/{id}")
    public Result setDefault(@PathVariable Long id) {
        Long userId = UserHolder.getUser().getId();
        return addressService.setDefaultAddress(id, userId);
    }
}
