package com.vertx.poc;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

@Component
public class VertxMySQLConnection extends AbstractVerticle {
	
	@Autowired
	private Environment environment;
	
	Future<SQLConnection> sqlConnection = Future.future();
	private void createDatabaseConnecion()
	{
		JsonObject mySqlConfig = new JsonObject();
		mySqlConfig.put("host", "localhost");
		mySqlConfig.put("port", 3306);
		mySqlConfig.put("database", "test3");
		mySqlConfig.put("username", "root");
		mySqlConfig.put("password", "root");
		SQLClient mySQLClient = MySQLClient.createNonShared(vertx, mySqlConfig);
		
		mySQLClient.getConnection(res -> {
			  if (res.succeeded()) {
				  SQLConnection connection =   res.result();
				  
				  connection.execute("create table if not exists User(id int(10),country varchar(20),name varchar(20))", 
						  asyncResult -> {
							 if (asyncResult.succeeded())
							 {
								 System.out.println("TABLE USER CREATED");
							 }
							 else if (asyncResult.failed())
							 {
								 System.out.println(asyncResult.cause());
							 }
						  });
				  
				sqlConnection.complete(connection);
			    System.out.println("Connection Created Successfully" + sqlConnection.result().toString());

			  } else {
				  sqlConnection.fail(res.cause());
			  }
			});
	}
	
	@Override
	public void start(Future<Void> future)
	{	
		int port=Integer.valueOf(environment.getProperty("server.port"));
		createDatabaseConnecion();
		Router router = Router.router(vertx);
		
		router.route("/api/user*").handler(BodyHandler.create());
		router.get("/api/users").handler(this::getAllUsers);
		router.get("/api/users/:id").handler(this::getById);
		router.post("/api/user").handler(this::addOne);
		router.delete("/api/users/:id").handler(this::deleteOne);
		router.put("/api/users").handler(this::updateOne);
		
		vertx.createHttpServer().requestHandler(router::accept).listen(port, result -> {
			if (result.succeeded())
			{
				future.complete();
			}
			else
			{
				future.fail(result.cause());
			}
		});
	}
	
	private void getAllUsers(RoutingContext routingContext)
	{
		if (sqlConnection.isComplete())
		{
			SQLConnection connection = sqlConnection.result();
			connection.query("select * from User", asyncResult -> {
				if (asyncResult.succeeded())
				{
					List<User> users = asyncResult.result().getRows().stream().map(User::new).collect(Collectors.toList());
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(users));
				}
				else if(asyncResult.failed())
				{
					routingContext.response().setStatusCode(500).end("Error getting User from database");
				}
			});
		}
		else
		{
			routingContext.response().setStatusCode(500).end("SqlConnecation was not crated properly");
		}
	}
	
	private void addOne(RoutingContext routingContext)
	{
		if (sqlConnection.isComplete())
		{
			User user = Json.decodeValue(routingContext.getBodyAsString(), User.class);
			SQLConnection connection = sqlConnection.result();
			connection.update("insert into User(id, country, name) values("+ user.getId()+",'"+user.getCountry()+"','"+user.getName()+"')", asyncResult -> {
				if (asyncResult.succeeded())
				{
					routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(user));
				}
				else if(asyncResult.failed())
				{
					routingContext.response().setStatusCode(500).end("Error while inserting user into database");
				}
			});
		}
		else
		{
			routingContext.response().setStatusCode(500).end("SqlConnecation was not crated properly");
		}
	}
	
	private void getById(RoutingContext routingContext)
	{
		if (sqlConnection.isComplete())
		{
			SQLConnection connection = sqlConnection.result();
			connection.query("select * from User where id = " + routingContext.request().getParam("id"), asyncResult -> {
				if (asyncResult.succeeded())
				{
					List<User> users = asyncResult.result().getRows().stream().map(User::new).collect(Collectors.toList());
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(users));
				}
				else if(asyncResult.failed())
				{
					routingContext.response().setStatusCode(500).end("Error while fetching User from database");
				}
			});
		}
		else
		{
			routingContext.response().setStatusCode(500).end("SqlConnecation was not crated properly");
		}

	}
	
	private void deleteOne(RoutingContext routingContext)
	{
		if (sqlConnection.isComplete())
		{
			int id =Integer.parseInt(routingContext.request().getParam("id"));
			SQLConnection connection = sqlConnection.result();
			connection.update("delete from User where id="+ id , asyncResult -> {
				if (asyncResult.succeeded())
				{
					routingContext.response().end();
				}
				else if(asyncResult.failed())
				{
					routingContext.response().setStatusCode(500).end("Error while deleting User from database");
				}
			});
		}
		else
		{
			routingContext.response().setStatusCode(500).end("SqlConnecation was not crated properly");
		}
	}
	
	private void updateOne(RoutingContext routingContext)
	{
		if (sqlConnection.isComplete())
		{	
			User user = Json.decodeValue(routingContext.getBodyAsString(), User.class);
			SQLConnection connection = sqlConnection.result();
			connection.update("update User set country='"+ user.getCountry() +"', name='"+user.getName()+"' where id=" + user.getId() , asyncResult -> {
				if (asyncResult.succeeded())
				{
					routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(user));
				}
				else if(asyncResult.failed())
				{
					routingContext.response().setStatusCode(500).end("Error while updating User from database");
				}
			});
		}
		else
		{
			routingContext.response().setStatusCode(500).end("SqlConnecation was not crated properly");
		}
	}
}


