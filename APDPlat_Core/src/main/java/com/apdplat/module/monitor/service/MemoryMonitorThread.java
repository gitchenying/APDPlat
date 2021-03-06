/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apdplat.module.monitor.service;

import com.apdplat.module.monitor.model.MemoryState;
import com.apdplat.module.system.service.LogQueue;
import com.apdplat.module.system.service.SystemListener;
import com.apdplat.platform.log.APDPlatLogger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 *
 * @author ysc
 */
public class MemoryMonitorThread extends Thread{
    protected static final APDPlatLogger log = new APDPlatLogger(MemoryMonitorThread.class);
    public boolean running=true;
    private int circle=10;
    public MemoryMonitorThread(int circle){
        this.setDaemon(true);
        this.setName("内存监视线程(Memory monitor thread)");
        log.info("内存监视间隔为(Memory monitor interval) "+circle+" 分钟(min)");
        this.circle=circle;
    }
    
    @Override
    public void run(){
        log.info("内存监视线程启动(Launch memory monitor thread)");
        while(running){
            log();
            try {
                Thread.sleep(circle*60*1000);
            } catch (InterruptedException ex) {
                if(!running){
                    log.info("内存监视线程退出(Exit memory monitor thread)");
                }else{
                    log.error("内存监视线程出错(Error in memory monitor thread)",ex);
                }
            }
        }
    }
    private void log(){        
        float max=(float)Runtime.getRuntime().maxMemory()/1000000;
        float total=(float)Runtime.getRuntime().totalMemory()/1000000;
        float free=(float)Runtime.getRuntime().freeMemory()/1000000;
        
        MemoryState logger=new MemoryState();
        try {
            logger.setServerIP(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            log.error("用户记录日志出错(Error in user record log)",ex);
        }
        logger.setAppName(SystemListener.getContextPath());
        logger.setRecordTime(new Date());
        logger.setMaxMemory(max);
        logger.setTotalMemory(total);
        logger.setFreeMemory(free);
        logger.setUsableMemory(logger.getMaxMemory()-logger.getTotalMemory()+logger.getFreeMemory());
        LogQueue.addLog(logger);
    }
}