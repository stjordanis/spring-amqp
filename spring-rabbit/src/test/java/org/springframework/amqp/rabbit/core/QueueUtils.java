package org.springframework.amqp.rabbit.core;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP.Queue;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;

public class QueueUtils {

	static void declareTestQueue(RabbitTemplate template,
			final String routingKey) {
		// declare and bind queue
		template.execute(new ChannelCallback<String>() {
			public String doInRabbit(Channel channel) throws Exception {
				Queue.DeclareOk res = channel.queueDeclare(Constants.QUEUE_NAME);
				String queueName = res.getQueue();
				System.out.println("Queue Name = " + queueName);
				channel.queueBind(queueName, Constants.EXCHANGE_NAME, routingKey);
				return queueName;
			}
		});
	}

}
