package com.noisy.proxy.service;

import com.noisy.proxy.entity.ProxyInfo;
import com.noisy.proxy.entity.ProxyIp;
import com.noisy.proxy.repository.ProxyIpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

/**
 * @author lei.X
 * @date 2019/5/27
 */
@Service("proxyIpService")
@Slf4j
@Transactional
public class ProxyIpService {

    @Resource
    private ProxyIpRepository proxyIpRepository;


    /**
     * 如果已经存在IP与port相同的数据，则不重复插入
     * @param proxyInfo
     */
    public void saveNewProxyIp(ProxyInfo proxyInfo){


        String ip = proxyInfo.getIp();
        int port = proxyInfo.getPort();


        ProxyIp dbProxy = proxyIpRepository.findByIpAndPort(ip,port);
        if (dbProxy == null){
            ProxyIp proxyIp = new ProxyIp();
            proxyIp.setIp(proxyInfo.getIp());
            proxyIp.setPort(proxyInfo.getPort());
            proxyIp.setType(proxyInfo.getType());
            proxyIp.setLocation(proxyInfo.getLocation());
            proxyIp.setProtocol(proxyInfo.getProtocol());
            proxyIpRepository.save(proxyIp);
            log.info("new proxyInfo insert into database success");
        }else{
            log.warn("ip-port has been insert into database, so refuse insert");
        }
    }




}
