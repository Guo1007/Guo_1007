package gcy.system.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartFormDTO {

    private String consignee;

    private String phone;

    private String address;

    private String remark;

    private List<OrderItemDTO> itemList;

}
