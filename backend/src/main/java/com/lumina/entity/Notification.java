package com.lumina.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("l_notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer isRead;
    private String type;
    private Long refId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
