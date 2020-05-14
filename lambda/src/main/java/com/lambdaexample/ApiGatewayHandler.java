package com.lambdaexample;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.UUID;


public class ApiGatewayHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>{
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static final String FROM = "thaiwan7@gmail.com";


    static final String DYNAMO_DB_TABLE_NAME = "emailmessages";

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB = new DynamoDB(client);


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context)
    {

        Util.logEnvironment(event, context, gson);
        Request request = gson.fromJson(event.getBody(), Request.class);

        try {
            sendEmail(context, request.getName(), request.getMessage(), request.getEmail());
            putMessageToDynamoDB(context, request.getMessage());
            return getResponse(200, "Success",
                    "Message was sent to " + request.getEmail() + " and written to DynamoDB");
        } catch (Exception e) {
            Util.logMessage(context, e.getMessage());
            return getResponse(500, "Failure", e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent getResponse(int code, String status, String message) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Credentials", "true");
        response.setHeaders(headers);
        Result result = new Result(status, message);
        response.setStatusCode(code);
        response.setBody(gson.toJson(result));
        return response;
    }

    private void sendEmail(Context context, String subject, String message, String email) throws Exception {
        try {
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withRegion(Regions.US_EAST_1).build();
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(email))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(message)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(subject)))
                    .withSource(FROM);
            client.sendEmail(request);
            Util.logMessage(context, "Email was sent!");
        } catch (Exception ex) {
            Util.logMessage(context, "The email was not sent. Error message: "
                    + ex.getMessage());
            throw new Exception(ex);
        }
    }

    private void putMessageToDynamoDB(Context context, String message) throws Exception {
        try {
            Table table = dynamoDB.getTable(DYNAMO_DB_TABLE_NAME);
            Item item = new Item().withPrimaryKey("message_id", UUID.randomUUID().toString())
                    .withString("message", message);
            table.putItem(item);
        } catch (ResourceNotFoundException e) {
            Util.logMessage(context, String.format("Error: The table \"%s\" can't be found.\n", DYNAMO_DB_TABLE_NAME));
            Util.logMessage(context,"Be sure that it exists and that you've typed its name correctly!");
            throw new Exception(e);
        } catch (AmazonServiceException e) {
            Util.logMessage(context, e.getMessage());
            throw new Exception(e);
        }
        Util.logMessage(context,"Done!");

    }
}