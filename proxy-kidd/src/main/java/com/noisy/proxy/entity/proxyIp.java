package com.noisy.proxy.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;

/**
 * @author lei.X
 * @date 2019/5/27
 */
@Entity
@Data
@Table(name = "proxy_ip")
public class proxyIp {

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String ip;
    @Column
    private int protocol;
    @Column(nullable = false)
    private int port;
    @Column(nullable = false)
    private int type;
    @Column
    private String location;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @LastModifiedDate
    @Column(name = "modify_time")
    private Date modifyTime;
}
