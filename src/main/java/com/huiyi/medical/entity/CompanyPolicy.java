package com.huiyi.medical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @TableName("company_policy")
public class CompanyPolicy {
    @TableId(type = IdType.AUTO) private Long id;
    private Long companyId;
    private String title;
    private String content;
    private LocalDate publishDate;
    private LocalDateTime createTime;
}
