package com.zml.shiro_oauth2_server.shiro;

import com.zml.shiro_oauth2_server.cache.ICacheClient;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MySessionDAO extends AbstractSessionDAO {

    Logger logger = LoggerFactory.getLogger(MySessionDAO.class);
//    @Setter
    @Autowired
    private ICacheClient cacheClient;
    private static final String sessionPrefix = "shiro_oauth2_session:";
    @Override
    protected Serializable doCreate(Session session) {
        logger.info("-----MySessionDAO.doCreate---------");
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session,sessionId);
        cacheClient.set(sessionPrefix+sessionId,Integer.parseInt(session.getTimeout() + ""),session);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable serializable) {
        logger.info("-----MySessionDAO.doReadSession---------");
        Session session = cacheClient.get(sessionPrefix + serializable);
        return session;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        logger.info("-----MySessionDAO.update---------");
        cacheClient.set(sessionPrefix+session.getId(),Integer.parseInt(session.getTimeout() + ""),session);
    }

    @Override
    public void delete(Session session) {
        logger.info("-----MySessionDAO.delete---------");
        String key = sessionPrefix+session.getId();
        cacheClient.delete(key);

    }

    @Override
    public Collection<Session> getActiveSessions() {
        logger.info("-----MySessionDAO.getActiveSessions---------");
        List<Session> sessionList = new ArrayList<>();
        Set<String> strings = cacheClient.keySet(sessionPrefix + "*");
        strings.forEach(str->{
            Session session = cacheClient.get(str);
            sessionList.add(session);
        });
        return sessionList;
    }
}
