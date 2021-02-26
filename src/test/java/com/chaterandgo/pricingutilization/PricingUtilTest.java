package com.chaterandgo.pricingutilization;

import com.chaterandgo.pricingutilization.pricingutilization.PricingUtilizationService;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class PricingUtilTest {

    @Test
    public void testActualsCreate() {
        PricingUtilizationService service = new PricingUtilizationService();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("actualsCreateRQ.json");
        OutputStream os = new ByteArrayOutputStream();
        service.handleRequest(stream, os, null);
        assert(os != null);
    }

    @Test
    public void testActualsRead() {
        PricingUtilizationService service = new PricingUtilizationService();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("actualsReadRQ.json");
        OutputStream os = new ByteArrayOutputStream();
        service.handleRequest(stream, os, null);
        assert(os != null);
    }

    @Test
    public void testActualsUpdate() {
        PricingUtilizationService service = new PricingUtilizationService();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("actualsUpdateReplaceRQ.json");
        OutputStream os = new ByteArrayOutputStream();
        service.handleRequest(stream, os, null);
        assert(os != null);
    }

    @Test
    public void testActualsIncrement() {
        PricingUtilizationService service = new PricingUtilizationService();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("actualsIncrementRQ.json");
        OutputStream os = new ByteArrayOutputStream();
        service.handleRequest(stream, os, null);
        assert(os != null);
    }

    @Test
    public void testActualsDelete() {
        PricingUtilizationService service = new PricingUtilizationService();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("actualsDeleteRQ.json");
        OutputStream os = new ByteArrayOutputStream();
        service.handleRequest(stream, os, null);
        assert(os != null);
    }

    @Test
    public void testForecastCreate() {
        PricingUtilizationService service = new PricingUtilizationService();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("forecastCreateRQ.json");
        OutputStream os = new ByteArrayOutputStream();
        service.handleRequest(stream, os, null);
        assert(os != null);
    }

    @Test
    public void testForecastRead() {
        PricingUtilizationService service = new PricingUtilizationService();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("forecastReadRQ.json");
        OutputStream os = new ByteArrayOutputStream();
        service.handleRequest(stream, os, null);
        assert(os != null);
    }

    @Test
    public void testForecastUpdate() {
        PricingUtilizationService service = new PricingUtilizationService();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("forecastUpdateRQ.json");
        OutputStream os = new ByteArrayOutputStream();
        service.handleRequest(stream, os, null);
        assert(os != null);
    }

    @Test
    public void testForecastDelete() {
        PricingUtilizationService service = new PricingUtilizationService();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("forecastDeleteRQ.json");
        OutputStream os = new ByteArrayOutputStream();
        service.handleRequest(stream, os, null);
        assert(os != null);
    }
    @Test
    public void testUtilizationFromUI() {
        PricingUtilizationService service = new PricingUtilizationService();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("UIRequest.json");
        OutputStream os = new ByteArrayOutputStream();
        service.handleRequest(stream, os, null);
        assert(os != null);
    }
}
