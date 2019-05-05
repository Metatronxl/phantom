package com.noisy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author liuzhaoce
 * @since 2019-05-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
public class AgentIp implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ip;

    private String port;

    private String anonymity;

    private String type;

    private String location;

    private String operator;


}
