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

package org.springframework.otp.erlang.core;

import org.springframework.otp.erlang.ErlangErrorRpcException;
import org.springframework.otp.erlang.ErlangBadRpcException;
import org.springframework.otp.erlang.OtpException;
import org.springframework.otp.erlang.connection.ConnectionFactory;
import org.springframework.otp.erlang.support.ErlangAccessor;
import org.springframework.otp.erlang.support.ErlangUtils;
import org.springframework.otp.erlang.support.converter.ErlangConverter;
import org.springframework.otp.erlang.support.converter.SimpleErlangConverter;
import org.springframework.util.Assert;

import com.ericsson.otp.erlang.*;


/**
 * @author Mark Pollack
 */
public class ErlangTemplate extends ErlangAccessor implements ErlangOperations {


	private volatile ErlangConverter erlangConverter = new SimpleErlangConverter();

	public ErlangTemplate(ConnectionFactory connectionFactory) {
		setConnectionFactory(connectionFactory);
		afterPropertiesSet();
	}
	
	public OtpErlangObject executeErlangRpc(final String module, final String function, final OtpErlangList args) {
		return execute(new ConnectionCallback<OtpErlangObject>() {
			public OtpErlangObject doInConnection(OtpConnection connection) throws Exception {	
				logger.debug("Sending RPC for module [" + module + "] function [" + function + "] args [" + args);
				connection.sendRPC(module, function, args);		
				//TODO consider dedicated response object.
				OtpErlangObject response = connection.receiveRPC();
				logger.debug("Response received = " + response.toString());
				handleResponseError(module, function, response);
				return response;
			}
		});
	}
	
	public void handleResponseError(String module, String function, OtpErlangObject result) {
		//{badrpc,{'EXIT',{undef,[{rabbit_access_control,list_users,[[]]},{rpc,'-handle_call/3-fun-0-',5}]}}}

		if (result instanceof OtpErlangTuple) {
			OtpErlangTuple msg = (OtpErlangTuple)result;
			OtpErlangObject[] elements = msg.elements();
			if (msg.elementAt(0) instanceof OtpErlangAtom)
			{
				OtpErlangAtom responseAtom = (OtpErlangAtom)msg.elementAt(0);
				//TODO consider error handler strategy.
				if (responseAtom.atomValue().equals("badrpc")) {		
					if (msg.elementAt(1) instanceof OtpErlangTuple) {
						throw new ErlangBadRpcException( (OtpErlangTuple)msg.elementAt(1));
					} else {
						throw new ErlangBadRpcException( msg.elementAt(1).toString());
					}
				} else if (responseAtom.atomValue().equals("error")) {
					if (msg.elementAt(1) instanceof OtpErlangTuple) {
						throw new ErlangErrorRpcException( (OtpErlangTuple)msg.elementAt(1));
					} else {
						throw new ErlangErrorRpcException( msg.elementAt(1).toString());
					}
				}
			}
		}
	}
	
	public OtpErlangObject executeErlangRpc(String module, String function, OtpErlangObject... args) {
		return executeRpc(module, function, new OtpErlangList(args));
	}
		
	public OtpErlangObject executeRpc(String module, String function, Object... args) {
		return executeErlangRpc(module, function, (OtpErlangList) erlangConverter.toErlang(args));
	}
	
	public Object executeAndConvertRpc(String module, String function, ErlangConverter converterToUse, Object... args) {
		return converterToUse.fromErlang(executeRpc(module, function, converterToUse.toErlang(args)));
	}
	
	public Object executeAndConvertRpc(String module, String function, Object... args) {		
		return erlangConverter.fromErlangRpc(module, function, executeErlangRpc(module, function, (OtpErlangList)erlangConverter.toErlang(args)));
	}
		
	public ErlangConverter getErlangConverter() {
		return erlangConverter;
	}

	public void setErlangConverter(ErlangConverter erlangConverter) {
		this.erlangConverter = erlangConverter;
	}

	public <T> T execute(ConnectionCallback<T> action) throws OtpException {

		Assert.notNull(action, "Callback object must not be null");
		OtpConnection con = null;
		try {		
			con = createConnection();
			return action.doInConnection(con);
		}	
		catch (OtpException ex) {
			throw ex;			
		}
		catch (Exception ex) {
			throw convertOtpAccessException(ex);			
		}
		finally {		
			org.springframework.otp.erlang.connection.ConnectionFactoryUtils.releaseConnection(con, getConnectionFactory());			
		}
		
		
		
		
	}
	
	/**
	 * Convert the specified checked {@link javax.jms.JMSException JMSException} to
	 * a Spring runtime {@link org.springframework.jms.JmsException JmsException}
	 * equivalent.
	 * <p>The default implementation delegates to the
	 * {@link org.springframework.jms.support.JmsUtils#convertJmsAccessException} method.
	 * @param ex the original checked {@link JMSException} to convert
	 * @return the Spring runtime {@link JmsException} wrapping <code>ex</code>
	 * @see org.springframework.jms.support.JmsUtils#convertJmsAccessException
	 */
	protected OtpException convertOtpAccessException(Exception ex) {
		return ErlangUtils.convertOtpAccessException(ex);
	}




	
	
	

	

}
