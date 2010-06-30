/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.amqp.rabbit.admin;

/**
 * Performs broker administration.  Uses {@link Queue}, {@link org.springframework.amqp.rabbit.config.Exchange},
 * and {@link Binding} as conveniences for code based configuration in Spring.
 */
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

import com.rabbitmq.client.AMQP;

/**
 * Performs administration tasks for broker administration.  
 * <p>Goal is to support full CRUD of Exchages, Queues, Bindings, User, VHosts, etc.
 * <p>Current implementations expose operations with basic typics via JMX.
 * 
 * @author Mark Pollack
 *
 */
public interface RabbitAdminOperations {

	//TODO the method naming is opposite to that in the channel interface...switch to Channel API style?
	
	// Declaration/Creation
	
	 AMQP.Queue.DeclareOk declareQueue(Queue queue);
	 
	 AMQP.Exchange.DeclareOk declareExchange(AbstractExchange exchange);
	 	
	 AMQP.Queue.BindOk declareBinding(Binding binding);
	 
	 
	 
	 // Can be used with JMX as the expose simple arguments.
	 
	 // Deletion
	 
	 AMQP.Queue.DeleteOk deleteQueue(String queueName);
	 
	 AMQP.Queue.DeleteOk deleteQueue(String queueName, boolean ifUnused, boolean ifEmpty);
	 
	 AMQP.Exchange.DeleteOk deleteExchange(String exchange);
	 
	 AMQP.Exchange.DeleteOk deleteExchange(String exchange, boolean ifUnused); 

	 
	 // Purge
	 
	 AMQP.Queue.PurgeOk purgeQueue(String queueName);
	 
	 AMQP.Queue.PurgeOk purgeQueue(String queueName, boolean nowait);
	 
	 // Unbind
	 
	 AMQP.Queue.UnbindOk unbindQueue(String queue, String exchange, String routingKey);
	 
	 AMQP.Queue.UnbindOk unbindQueue(String queue, String exchange, String routingKey, Map<String,Object> arguments);
	 
	 // Message Delivery
	 
	 void recoverAsync(boolean requeue);
	 
	 // Misc
	 
	 void addUser(String username, String password);
	 
	 void deleteUser(String username);
	 
	 void changeUserPassword(String username, String newPassword);
	 
	 List<String> listUsers();
	 
	 int addVhost(String vhostPath);
	 
	 int deleteVhost(String vhostPath);
	 
	 // TODO permissions
	 
	 void setPermissions(String username, Pattern configure, Pattern read, Pattern write);
	 
	 void setPermissions(String username, Pattern configure, Pattern read, Pattern write, String vhostPath);
	 
	 void clearPermissions(String username);
	 
	 void clearPermissions(String username, String vhostPath);
	 
	 List<String> listPermissions();
	 
	 List<String> listPermissions(String vhostPath);
	 
	 List<String> listUserPermissions(String username);
	 
	 
	 // Start/Stop/Re	 
	 
	 /**
	  * Starts the RabbitMQ application on an already running node. This command is typically run after performing other 
	  * management actions that required the RabbitMQ application to be stopped, e.g. reset. 
	  */
	 void startBrokerApplication();
	 
	 /**
	  * Stops the RabbitMQ application, leaving the Erlang node running. 
	  */
	 void stopBrokerApplication();
	 
	 /**
	  * Starts the Erlang node where RabbitMQ is running by shelling out to the directory specified by RABBIT_HOME and
	  * executing the standard named start script.  It spawns the shell command execution into its own thread.  
	  */
	 void startNode();
	 
	 /**
	  * Stops the halts the Erlang node on which RabbitMQ is running.  To restart the node you will need to execute
	  * the start script from a command line or via other means.
	  */
	 void stopNode();
	 
	 /**
	  * Removes the node from any cluster it belongs to, removes all data from the management database, 
	  * such as configured users and vhosts, and deletes all persistent messages. 
	  * <p>
	  * For {@link resetNode} and {@link forceReset} to succeed the RabbitMQ application must have
	  * been stopped, e.g. {@link stopBroker} 
	  */
	 void resetNode();
	 
	 /**
	  * The forceResetNode command differs from {@link resetNode()} in that it resets the node unconditionally, 
	  * regardless of the current management database state and cluster configuration. It should only be 
	  * used as a last resort if the database or cluster configuration has been corrupted. 
	  * <p>
	  * For {@link resetNode} and {@link forceReset} to succeed the RabbitMQ application must have
	  * been stopped, e.g. {@link stopBroker} 
	  */
	 void forceResetNode();
	 
	 /**
	  * Returns the status of the node.
	  * @return status of the node.
	  */
	 RabbitStatus getStatus();
		
	 
}
