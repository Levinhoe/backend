package com.huiyi.medical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @TableName("drug")
public class Drug {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private String manufacturer;
    private String description;
    private LocalDateTime createTime;
}
