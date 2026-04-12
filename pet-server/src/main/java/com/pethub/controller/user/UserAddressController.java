package com.pethub.controller.user;

import com.pethub.common.context.BaseContext;
import com.pethub.common.result.Result;
import com.pethub.pojo.dto.AddressSaveDTO;
import com.pethub.pojo.vo.AddressVO;
import com.pethub.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端收货地址接口。
 * 用于管理当前登录用户自己的地址簿。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;

    /**
     * 查询当前用户的地址列表。
     */
    @GetMapping
    public Result<List<AddressVO>> list() {
        return Result.success(userAddressService.listByCurrentUser(BaseContext.getCurrentId()));
    }

    /**
     * 查询某个地址详情。
     */
    @GetMapping("/{id}")
    public Result<AddressVO> getById(@PathVariable Long id) {
        return Result.success(userAddressService.getById(BaseContext.getCurrentId(), id));
    }

    /**
     * 查询当前用户默认地址。
     */
    @GetMapping("/default")
    public Result<AddressVO> getDefault() {
        return Result.success(userAddressService.getDefaultAddress(BaseContext.getCurrentId()));
    }

    /**
     * 新增收货地址。
     */
    @PostMapping
    public Result<Long> save(@RequestBody AddressSaveDTO addressSaveDTO) {
        return Result.success(userAddressService.save(BaseContext.getCurrentId(), addressSaveDTO));
    }

    /**
     * 更新收货地址。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody AddressSaveDTO addressSaveDTO) {
        userAddressService.update(BaseContext.getCurrentId(), id, addressSaveDTO);
        return Result.success();
    }

    /**
     * 设置默认地址。
     */
    @PatchMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        userAddressService.setDefault(BaseContext.getCurrentId(), id);
        return Result.success();
    }

    /**
     * 删除收货地址。
     */
    @DeleteMapping("/{id}")
    public Result<Void> removeById(@PathVariable Long id) {
        userAddressService.removeById(BaseContext.getCurrentId(), id);
        return Result.success();
    }
}
