package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.UserAddress;

/**
 * 用户地址管理服务接口
 * <p>
 * 提供用户收货地址的增删改查及默认地址设置等核心业务能力。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IAddressService {

    /**
     * 根据用户ID获取该用户的所有收货地址列表
     *
     * @param userId 用户唯一标识，用于查询该用户下的全部地址
     * @return 包含地址列表的操作结果，成功时data字段为地址集合，失败时携带错误信息
     */
    Result getAddressList(Long userId);

    /**
     * 保存或更新用户收货地址
     * <p>
     * 若地址已存在则更新，否则新增一条地址记录。
     * </p>
     *
     * @param addr   用户地址实体，包含收货人、联系电话、详细地址等字段
     * @param userId 当前操作用户的唯一标识，用于绑定地址归属
     * @return 操作结果，成功时包含保存后的地址信息，失败时携带错误描述
     */
    Result saveAddress(UserAddress addr, Long userId);

    /**
     * 根据地址ID删除指定收货地址
     *
     * @param id 地址唯一标识，指向待删除的地址记录
     * @return 操作结果，成功时表明删除完成，失败时携带错误信息（如地址不存在）
     */
    Result deleteAddress(Long id);

    /**
     * 将指定地址设置为用户的默认收货地址
     * <p>
     * 设置后该用户原有的默认地址将被取消，仅保留当前地址为默认。
     * </p>
     *
     * @param id     地址唯一标识，指向要设为默认的地址记录
     * @param userId 当前操作用户的唯一标识，用于校验地址归属权限
     * @return 操作结果，成功时表明默认地址已更新，失败时携带错误描述
     */
    Result setDefaultAddress(Long id, Long userId);
}
