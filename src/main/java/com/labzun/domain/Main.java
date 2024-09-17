package com.labzun.domain;
import com.labzun.redis.CityCountry;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        MySessionFactory mySessionFactory = new MySessionFactory();
        List<City> allCities = mySessionFactory.fetchData(mySessionFactory);
        List<CityCountry> preparedData = mySessionFactory.transformData(allCities);
        mySessionFactory.pushToRedis(preparedData);
        mySessionFactory.sessionFactory.getCurrentSession().close();
        List<Integer> ids = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

        long startRedis = System.currentTimeMillis();
        mySessionFactory.testRedisData(ids);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        mySessionFactory.testMysqlData(ids);
        long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));
        mySessionFactory.shutdown();
    }
}