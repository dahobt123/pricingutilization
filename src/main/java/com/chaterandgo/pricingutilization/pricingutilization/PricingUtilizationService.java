package com.chaterandgo.pricingutilization.pricingutilization;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.charterandgo.model.*;
import com.charterandgo.pricing.accessors.*;
import com.charterandgo.pricing.accessors.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.sql.*;
import java.util.*;


public class PricingUtilizationService implements RequestStreamHandler {

    private final Connection connection;
    private static final Map<String, String> ERROR_MESSAGES = new HashMap<>();
    private String language;
    private final UtilizationAccessor utilizationAccessor;

    static {
        ERROR_MESSAGES.put("5013", "unable to create aircraft utilization from UI");
        ERROR_MESSAGES.put("5014", "unable to delete aircraft utilization from UI");
        ERROR_MESSAGES.put("5012", "unable to update aircraft utilization");
        ERROR_MESSAGES.put("5040", "attempt to create aircraft utilization that already exists - use update instead");
        ERROR_MESSAGES.put("5041", "attempt to update aircraft utilization that does not exists - use create instead");
        ERROR_MESSAGES.put("5066", "Error reading utilizations");
        ERROR_MESSAGES.put("5067", "Error reading all utilizations");
        ERROR_MESSAGES.put("5068", "Error calculating utilization discount");
        ERROR_MESSAGES.put("6000", "Error reading utilization actuals");
        ERROR_MESSAGES.put("6002", "Error creating utilization actuals");
        ERROR_MESSAGES.put("6003", "Error deleting utilization actuals");
        ERROR_MESSAGES.put("6004", "Error updating utilization actuals");
        ERROR_MESSAGES.put("6005", "Error getting actuals counts by make and model");
    }


    public PricingUtilizationService() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection("jdbc:postgresql://database-2.cji1jjfi82gk.us-east-1.rds.amazonaws.com/CharterAndGO",
                            "postgres", "postgres1");
            utilizationAccessor = new UtilizationAccessor(connection);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        boolean isHttp = false;
        JSONObject result = new JSONObject();
        try {
            byte buff[] = new byte[200000];
            inputStream.read(buff);
            String input = new String(buff).trim();
            JSONObject request = new JSONObject(input);
            System.out.println("####Raw input###");
            System.out.println(request);
            if (request.has("path")) {
                String stringit = request.getString("body");
                request = new JSONObject(stringit);
                isHttp = true;
            }
            System.out.println("#################################################");
            System.out.println(request.toString());
            System.out.println("#################################################");
            System.out.println(request.toString());
            JSONObject commons = request.getJSONObject("commonParms");
            String action = commons.getString("view");
            String response = processPricingRequest(input, action);
            if (isHttp) {
                response = createHttpJson(response);
            }
            outputStream.write(response.getBytes());
            System.out.println(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String createHttpJson(String rr) {
        System.out.println("In HttpFormatter");
        JSONObject outer = new JSONObject();
        outer.put("isBase64Encoded", false);
        outer.put("statusCode", 200);
        JSONObject headers = new JSONObject();
        headers.put("Content-Type", "application/json");
        outer.put("headers", headers);
        outer.put("body", rr);
        System.out.println(outer.toString());
        return outer.toString();
    }

    private String processPricingRequest(String input, String action) {
        InputContext context = null;
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm").create();
            List<UtilizationForecast> utilizationItems;
            List<UtilizationActuals> utilizationActuals;
            switch (action) {
                case "UpdateUtilizationForecast":
                    UtilizationForecastUpdateRequest updateRequest = gson.fromJson((input), UtilizationForecastUpdateRequest.class);
                    context = updateRequest.getContext();
                    utilizationItems = utilizationAccessor.updateUtilizationFromUI(updateRequest);
                    return prepareResponse(utilizationItems.get(0), context);
                case "ReadUtilizationsForecast":
                    UtilizationForecastReadRequest utilRead = gson.fromJson((input), UtilizationForecastReadRequest.class);
                    context = utilRead.getContext();
                    utilizationItems = utilizationAccessor.readUtilizationForecast(utilRead);
                    return prepareListResponse(utilizationItems, context);
                case "ReadAllUtilizationsForecast":
                    UtilizationForecastReadRequest utilAllRead = gson.fromJson((input), UtilizationForecastReadRequest.class);
                    context = utilAllRead.getContext();
                    utilizationItems = utilizationAccessor.getAllUtilizationForecasts(utilAllRead);
                    return prepareListResponse(utilizationItems, context);
                case "CreateUtilizationForecast":
                    UtilizationForecastCreateRequest createRequest = gson.fromJson((input), UtilizationForecastCreateRequest.class);
                    context = createRequest.getContext();
                    utilizationItems = utilizationAccessor.createUtilizationForecast(createRequest);
                    return prepareResponse(utilizationItems.get(0), context);
                case "DeleteUtilizationForecast":
                    UtilizationForecastDeleteRequest deleteRequest = gson.fromJson((input), UtilizationForecastDeleteRequest.class);
                    context = deleteRequest.getContext();
                    utilizationAccessor.deleteUtilizationForecast(deleteRequest);
                    return processStandardResponse(context);
                case "UpdateUtilization":
                    UtilizationActualsUpdateRequest actualsUpdate = gson.fromJson((input), UtilizationActualsUpdateRequest.class);
                    context = actualsUpdate.getContext();
                    utilizationActuals = utilizationAccessor.updateActuals(actualsUpdate);
                    return prepareResponse(utilizationActuals.get(0), context);
                case "IncrementUtilization":
                    UtilizationActualsUpdateRequest actualsIncrement = gson.fromJson((input), UtilizationActualsUpdateRequest.class);
                    context = actualsIncrement.getContext();
                    utilizationActuals = utilizationAccessor.incrementActuals(actualsIncrement);
                    return prepareResponse(utilizationActuals.get(0), context);
                case "ReadUtilizations":
                    UtilizationActualsReadRequest actualsRead = gson.fromJson((input), UtilizationActualsReadRequest.class);
                    context = actualsRead.getContext();
                    utilizationActuals = utilizationAccessor.readActuals(actualsRead);
                    return prepareListResponse(utilizationActuals, context);
                case "CreateUtilization":
                    UtilizationActualsCreateRequest actualsCreate = gson.fromJson((input), UtilizationActualsCreateRequest.class);
                    context = actualsCreate.getContext();
                    utilizationActuals = utilizationAccessor.createActuals(actualsCreate);
                    return prepareResponse(utilizationActuals.get(0), context);
                case "DeleteUtilization":
                    UtilizationActualsDeleteRequest actualsDelete = gson.fromJson((input), UtilizationActualsDeleteRequest.class);
                    context = actualsDelete.getContext();
                    utilizationAccessor.deleteActuals(actualsDelete);
                    return processStandardResponse(context);
                case "UtilizationCombined":
                    UtilizationUIRequest utilizationUIRequest = gson.fromJson((input), UtilizationUIRequest.class);
                    context = utilizationUIRequest.getContext();
                    UtilizationUIData uiData = utilizationAccessor.getCombinedUtilizations(utilizationUIRequest);
                    return prepareResponse(uiData, context);

            }
        } catch (Exception e) {
            return processError(e, context);

        }
        return null;
    }

    private String processStandardResponse(InputContext context) {
        JSONObject response = new JSONObject();
        JSONObject stdResponse = new JSONObject();
        response.put("standardresponse", stdResponse);
        stdResponse.put("domainName", context.getDomainName());
        stdResponse.put("language", context.getLanguage());
        stdResponse.put("transactionid", context.getTransactionid());
        stdResponse.put("responsetype", "GOOD");
        stdResponse.put("returnCode", "0");
        stdResponse.put("responsetype", "GOOD");
        return response.toString();
    }

    private String prepareListResponse(List objs, InputContext context) {
        JSONObject response = new JSONObject();
        JSONObject stdResponse = new JSONObject();
        response.put("standardResponse", stdResponse);
        stdResponse.put("domainName", context.getDomainName());
        stdResponse.put("language", context.getLanguage());
        stdResponse.put("transactionid", context.getTransactionid());
        stdResponse.put("responsetype", "GOOD");
        stdResponse.put("returnCode", "0");
        stdResponse.put("responsetype", "GOOD");
        Object obj = objs.get(0);
        JSONArray array = new JSONArray();
        if (objs.get(0) instanceof UtilizationActuals) {
            for (Object obj1 : objs) {
                UtilizationActuals pt = (UtilizationActuals) obj;
                array.put(pt.toJson());
            }
        } else {
            for (Object obj1 : objs) {
                UtilizationForecast pt = (UtilizationForecast) obj;
                array.put(pt.toJson());
            }
        }
        response.put("responseMessage", array);
        return response.toString();
    }

    private String prepareResponse(Object obj, InputContext context) {
        JSONObject response = new JSONObject();
        JSONObject stdResponse = new JSONObject();
        response.put("standardResponse", stdResponse);
        stdResponse.put("domainName", context.getDomainName());
        stdResponse.put("language", context.getLanguage());
        stdResponse.put("transactionid", context.getTransactionid());
        stdResponse.put("responsetype", "GOOD");
        stdResponse.put("returnCode", "0");
        stdResponse.put("responsetype", "GOOD");
        if (obj instanceof UtilizationActuals) {
            UtilizationActuals uAct = (UtilizationActuals) obj;
            response.put("responseMessage", uAct.toJson());
        } else if (obj instanceof UtilizationForecast) {
            UtilizationForecast uAct = (UtilizationForecast) obj;
            response.put("responseMessage", uAct.toJson());
        } else if (obj instanceof UtilizationUIData) {
            UtilizationUIData data = (UtilizationUIData) obj;
            response.put("responseMessage", data.toJson());
        }
        return response.toString();
    }


    /**
     * Method to format a JSON error message when an underlying exception is throuwn
     *
     * @param e - the exception that was thrown and caught
     * @return - a JSON string containing the error message
     */
    private String processError(Exception e, InputContext context) {
        e.printStackTrace();
        JSONObject response = new JSONObject();
        JSONObject stdResponse = new JSONObject();
        stdResponse.put("standardresponse", stdResponse);
        stdResponse.put("count", 0);
        stdResponse.put("domainName", "Inventory");
        stdResponse.put("responsetype", "FATAL");
        stdResponse.put("returnCode", e.getMessage());
        stdResponse.put("language", context.getLanguage());
        stdResponse.put("transactionid", context.getTransactionid());
        String message = ERROR_MESSAGES.get(e.getMessage());
        if (message == null) {
            message = "Unknown error has occurred";
        }
        stdResponse.put("errormessage", message);
        return response.toString();
    }


    public UtilizationAccessor getUtilizationAccessor() {
        return utilizationAccessor;
    }
}



