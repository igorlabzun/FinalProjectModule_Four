package com.labzun.domain;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labzun.dao.CityDAO;
import com.labzun.dao.CountryDAO;
import com.labzun.redis.CityCountry;
import com.labzun.redis.Language;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class MySessionFactory {
    SessionFactory sessionFactory;
    private final RedisClient redisClient;
    private final ObjectMapper mapper;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;


    public MySessionFactory() {
        sessionFactory = prepareRelationalDb();
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        redisClient = prepareRedisClient();
        mapper = new ObjectMapper();

    }

    private RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            System.out.println("\nConnected to Redis\n"+ connection);
        }
        return redisClient;
    }
    void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    private SessionFactory prepareRelationalDb() {
        final SessionFactory sessionFactory;
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/world");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "aSd123Rtu");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        properties.put(Environment.HBM2DDL_AUTO, "validate");
        properties.put(Environment.STATEMENT_BATCH_SIZE, "100");

        sessionFactory = new Configuration()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .addProperties(properties)
                .buildSessionFactory();
        return sessionFactory;
    }
    public void shutdown() {
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }

    public List<City> fetchData(MySessionFactory mySessionFactory) {
        try (Session session = mySessionFactory.sessionFactory.getCurrentSession()) {
            List<City> allCities = new ArrayList<>();
            session.beginTransaction();
            List<Country> countries = mySessionFactory.countryDAO.getAll();
            System.out.println("Loaded " + countries.size() + " countries from the database.");

            int totalCount = mySessionFactory.cityDAO.getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(mySessionFactory.cityDAO.getItems(i, step));
            }
            session.getTransaction().commit();
            return allCities;
        }
    }
    List<CityCountry> transformData(List<City> cities) {
        return cities.stream().map(city -> {
            CityCountry result = new CityCountry();
            result.setId(city.getId());
            result.setName(city.getName());
            result.setPopulation(city.getPopulation());
            result.setDistrict(city.getDistrict());

            Country country = city.getCountry();
            result.setAlternativeCountryCode(country.getAlternativeCountryCode());
            result.setContinent(country.getContinent());
            result.setCountryCode(country.getCode());
            result.setCountryName(country.getName());
            result.setCountryPopulation(country.getPopulation());
            result.setCountryRegion(country.getRegion());
            result.setCountrySurfaceArea(country.getSurfaceArea());
            Set<CountryLanguage> countryLanguages = country.getLanguages();
            Set<Language> languages = countryLanguages.stream().map(s -> {
                Language language = new Language();
                language.setLanguage(s.getLanguage());
                language.setOfficial(s.getOfficial());
                language.setPercentage(s.getPercentage());
                return language;
            }).collect(Collectors.toSet());
            result.setLanguages(languages);

            return result;
        }).collect(Collectors.toList());
    }
    void testRedisData(List<Integer> ids) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id : ids) {
                String value = sync.get(String.valueOf(id));
                try {
                    mapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    void testMysqlData(List<Integer> ids) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityDAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
                System.out.println("Loaded "+ languages);
            }
            session.getTransaction().commit();
        }
    }
}