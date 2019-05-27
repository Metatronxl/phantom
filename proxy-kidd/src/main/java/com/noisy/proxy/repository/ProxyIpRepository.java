package com.noisy.proxy.repository;

import com.noisy.proxy.entity.ProxyIp;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author lei.X
 * @date 2019/5/27
 */
public interface ProxyIpRepository  extends JpaRepository<ProxyIp,Long>{


    ProxyIp findByIpAndPort(String ip,int port);

}
