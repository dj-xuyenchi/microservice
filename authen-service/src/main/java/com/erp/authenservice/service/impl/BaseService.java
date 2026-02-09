package com.erp.authenservice.service.impl;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

//import org.springframework.kafka.core.KafkaTemplate;
@Service
public class BaseService {
    @Autowired
    protected JdbcTemplate _j;
    @Autowired
    protected NamedParameterJdbcTemplate _np;
    @Autowired
    protected TransactionTemplate _t;
    @Autowired
    protected PlatformTransactionManager _tm;
//        @Autowired
//    protected KafkaTemplate<String, Object> _k;
    @Autowired
    protected Gson _g;

  //  protected TransactionStatus getStatusTrans() {
  //      return _tm.getTransaction(new DefaultTransactionDefinition());
  //  }
}
