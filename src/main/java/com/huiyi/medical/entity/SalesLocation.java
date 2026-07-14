package com.huiyi.medical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @TableName("sales_location")
public class SalesLocation {
    @TableId(type = IdType.AUTO) private Long id;
    private String supplierId;
    private String name;
    private String phone;
    private String address;
    private Long cityId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private LocalDateTime createTime;
}
