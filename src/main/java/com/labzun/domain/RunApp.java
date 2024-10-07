package com.labzun.domain;

import com.labzun.redis.CityCountry;



import java.util.List;

public class RunApp {

    public void init(){
    ConnectionManager connectionManager = new ConnectionManager();

        List<City> allCities = connectionManager.fetchData(connectionManager);
        List<CityCountry> preparedData = connectionManager.transformData(allCities);
        connectionManager.pushToRedis(preparedData);
        connectionManager.sessionFactory.getCurrentSession().close();
        List<Integer> ids = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

        long startRedis = System.currentTimeMillis();
        connectionManager.getDataFromRedis(ids);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        connectionManager.getDataFromMysql(ids);
        long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));
        connectionManager.shutdown();
     }
}
