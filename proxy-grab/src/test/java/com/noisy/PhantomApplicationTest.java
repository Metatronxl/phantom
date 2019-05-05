//package com.noisy;
//
//import com.noisy.entity.AgentIp;
//import com.noisy.mapper.AgentIpMapper;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.List;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = GrabCenter.class)
//public class PhantomApplicationTest {
//
//    @Autowired
//    private AgentIpMapper agentIpMapper;
//
//    @Test
//    public void testSelect() {
//        System.out.println(("----- selectAll method test ------"));
//        List<AgentIp> agentIpList = agentIpMapper.selectList(null);
////        Assert.assertEquals(5, agentIpList.size());
//        agentIpList.forEach(System.out::println);
//    }
//}