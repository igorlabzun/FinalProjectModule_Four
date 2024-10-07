package com.labzun.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.labzun.domain.City;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class CityDAOTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<City> cityQuery;

    @Mock
    private Query<Long> countQuery;

    @InjectMocks
    private CityDAO cityDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void getListOfCitiesInSpecifiedRange() {

        City city1 = new City(1, "Kiev");
        City city2 = new City(2, "Lvov");

        List<City> expectedCities = Arrays.asList(city1, city2);


        when(session.createQuery("select c from City c", City.class)).thenReturn(cityQuery);
        when(cityQuery.setFirstResult(0)).thenReturn(cityQuery);
        when(cityQuery.setMaxResults(2)).thenReturn(cityQuery);
        when(cityQuery.list()).thenReturn(expectedCities);


        List<City> actualCities = cityDAO.getItems(0, 2);


        assertEquals(expectedCities, actualCities);
        verify(session).createQuery("select c from City c", City.class);
        verify(cityQuery).setFirstResult(0);
        verify(cityQuery).setMaxResults(2);
        verify(cityQuery).list();
    }

    @Test
    void getTotalCountOfCities() {

        when(session.createQuery("select count(c) from City c", Long.class)).thenReturn(countQuery);
        when(countQuery.uniqueResult()).thenReturn(5L);


        int actualCount = cityDAO.getTotalCount();


        assertEquals(5, actualCount);
        verify(session).createQuery("select count(c) from City c", Long.class);
        verify(countQuery).uniqueResult();
    }

    @Test
    void getByIdOfCities() {

        City city = new City(1,"Kiev");


        when(session.createQuery("select c from City c join fetch c.country where c.id = :ID", City.class)).thenReturn(cityQuery);
        when(cityQuery.setParameter("ID", 1)).thenReturn(cityQuery);
        when(cityQuery.getSingleResult()).thenReturn(city);


        City actualCity = cityDAO.getById(1);


        assertEquals(city, actualCity);
        verify(session).createQuery("select c from City c join fetch c.country where c.id = :ID", City.class);
        verify(cityQuery).setParameter("ID", 1);
        verify(cityQuery).getSingleResult();
    }
}