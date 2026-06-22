package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.UserAddress;
import gcy.system.service.IAddressService;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

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
