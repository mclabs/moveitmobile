// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2008, Thanasis Delenikas, Athens/GREECE.
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.smslib.smssvr;

import java.io.*;
import java.util.*;
import java.sql.*;
import org.smslib.*;
import org.smslib.modem.*;
import org.smslib.test.*;

/**
 * <b>SMSSvr Application</b><br>
 * SMSSvr is a simple, configurable application which serves as an interface
 * between your modems and a two-table database, one table for the inbound
 * messages and one for the outbound messages. SMSSvr will read inbound messages
 * from your modems and store them in the database and will look in the database
 * for outbound messages and will forward them via your modems.<br>
 * <br>
 * <b>Configuration File.</b><br>
 * Please review the <b>SMSSvr.conf</b> configuration file. The inline comments
 * will help you setup your configuration.<br>
 * <br>
 * The most recent documentation for SMSSvr can be found here:
 * <a href="http://smslib.org/index.php?page=smssvr">http://smslib.org/index.php?page=smssvr</a>
 */
public class SMSSvr
{
	private Service srv;
	private Properties props;
	private OutboundNotification outboundNotification;
	private CallNotification callNotification;
	private boolean shutdown = false;

	public SMSSvr()
	{
		srv = new Service();
		srv.setLoadBalancer(new RoundRobinLoadBalancer(srv));
		outboundNotification = new OutboundNotification();
		callNotification = new CallNotification();
		Runtime.getRuntime().addShutdownHook(new Shutdown());
	}

	private void initialize() throws Exception
	{
		int i;
		String propName;
		FileInputStream f;

		props = new Properties();
		if (System.getProperty("smssvr.configdir") != null) f = new FileInputStream(System.getProperty("smssvr.configdir") + "SMSSvr.conf");
		else if (System.getProperty("smssvr.configfile") != null) f = new FileInputStream(System.getProperty("smssvr.configfile"));
		else throw new org.smslib.SMSLibException("Cannot find SMSSvr configuration file!");
		props.load(f);
		f.close();
		i = 0;
		propName = "gateway." + i + ".";
		while (props.getProperty(propName + "type", "").length() > 0)
		{
			if (props.getProperty(propName + "type").equalsIgnoreCase("serial_modem"))
			{
				SerialModemGateway gateway = new SerialModemGateway(props.getProperty(propName + "id"), props.getProperty(propName + "comport"), Integer.parseInt(props.getProperty(propName + "baudrate")), props.getProperty(propName + "manufacturer"), props.getProperty(propName + "model"), srv);
				if (props.getProperty(propName + "protocol").equalsIgnoreCase("PDU")) gateway.setProtocol(MessageProtocols.PDU);
				else if (props.getProperty(propName + "protocol").equalsIgnoreCase("TEXT")) gateway.setProtocol(MessageProtocols.TEXT);
				else throw new Exception("Incorrect parameter: " + propName + "protocol");
				gateway.setSimPin(props.getProperty(propName + "pin"));
				if (props.getProperty(propName + "inbound").equalsIgnoreCase("YES")) gateway.setInbound(true);
				else if (props.getProperty(propName + "inbound").equalsIgnoreCase("NO")) gateway.setInbound(false);
				else throw new Exception("Incorrect parameter: " + propName + "inbound");
				if (props.getProperty(propName + "outbound").equalsIgnoreCase("YES")) gateway.setOutbound(true);
				else if (props.getProperty(propName + "outbound").equalsIgnoreCase("NO")) gateway.setOutbound(false);
				else throw new Exception("Incorrect parameter: " + propName + "outbound");
				gateway.setOutboundNotification(outboundNotification);
				gateway.setCallNotification(callNotification);
				srv.getLogger().info("Adding " + props.getProperty(propName + "id"));
				srv.addGateway(gateway);
			}
			else if (props.getProperty(propName + "type").equalsIgnoreCase("test"))
			{
				TestGateway gateway = new TestGateway(props.getProperty(propName + "id"), srv);
				gateway.setOutboundNotification(outboundNotification);
				gateway.setFailCycle(5);
				srv.getLogger().info("Adding " + props.getProperty(propName + "id"));
				srv.addGateway(gateway);
			}
			i++;
			propName = "gateway." + i + ".";
		}
		Class.forName(props.getProperty("database.driver"));
	}

	private void resetQueuedMessages() throws Exception
	{
		Connection con;
		Statement cmd;

		con = getDbConnection();
		if (con == null) return;
		cmd = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		cmd.executeUpdate("update " + props.getProperty("database.tables.sms_out", "smssvr_out") + " set status = 'U' where status = 'Q'");
		con.commit();
		cmd.close();
		con.close();
	}

	private class Shutdown extends Thread
	{
		public void run()
		{
			srv.getLogger().info("Shutting down, please wait...");
			shutdown = true;
			try
			{
				srv.stopService();
			}
			catch (Exception e)
			{
				srv.getLogger().error(e);
			}
		}
	}

	private void saveToDatabase(List msgList) throws Exception
	{
		InboundMessage msg;
		Connection con;
		Statement cmd;
		ResultSet rs;
		int readCount;

		readCount = 0;
		con = getDbConnection();
		if (con == null) return;
		cmd = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		rs = cmd.executeQuery("select * from " + props.getProperty("database.tables.sms_in", "smssvr_in") + " where id = -1");
		for (int i = 0; i < msgList.size(); i++)
		{
			readCount++;
			if (readCount > Integer.parseInt(props.getProperty("settings.max_in"))) break;
			msg = (InboundMessage) msgList.get(i);
			if ((msg.getType() == MessageTypes.INBOUND) || (msg.getType() == MessageTypes.STATUSREPORT))
			{
				rs.moveToInsertRow();
				rs.updateInt("process", 0);
				rs.updateString("originator", msg.getOriginator());
				if (msg.getType() == MessageTypes.INBOUND) rs.updateString("type", "I");
				else if (msg.getType() == MessageTypes.STATUSREPORT) rs.updateString("type", "S");
				if (msg.getEncoding() == MessageEncodings.ENC7BIT) rs.updateString("encoding", "7");
				else if (msg.getEncoding() == MessageEncodings.ENC8BIT) rs.updateString("encoding", "8");
				else if (msg.getEncoding() == MessageEncodings.ENCUCS2) rs.updateString("encoding", "U");
				if (msg.getDate() != null) rs.updateTimestamp("message_date", new Timestamp(msg.getDate().getTime()));
				rs.updateTimestamp("receive_date", new Timestamp(new java.util.Date().getTime()));
				rs.updateString("text", msg.getText().replaceAll("'", "''"));
				rs.updateString("gateway_id", msg.getGatewayId());
				rs.insertRow();
				srv.getLogger().info("<<< From: " + msg.getGatewayId() + " : " + msg.getOriginator());
			}
		}
		rs.close();
		cmd.close();
		con.commit();
		con.close();
	}

	private void sendMessages() throws Exception
	{
		OutboundMessage msg;
		Connection con;
		Statement cmd;
		ResultSet rs;
		int sendCount;

		sendCount = 0;
		con = getDbConnection();
		if (con == null) return;
		cmd = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		if (props.getProperty("database.type").equalsIgnoreCase("mysql"))
			rs = cmd.executeQuery("select * from " + props.getProperty("database.tables.sms_out", "smssvr_out") + " where status = 'U' order by priority, id");
		else
			rs = cmd.executeQuery("select *, case when priority = 'H' then 1 when priority = 'N' then 2 when priority = 'L' then 3 else 4 end as prioritynum from " + props.getProperty("database.tables.sms_out", "smssvr_out") + " where status = 'U' order by prioritynum, id");
		while (rs.next())
		{
			if (checkTimeFrame(rs.getString("priority")))
			{
				sendCount++;
				if (sendCount > Integer.parseInt(props.getProperty("settings.max_out"))) break;
				msg = new OutboundMessage(rs.getString("recipient"), rs.getString("text"));
				if (rs.getString("priority").equalsIgnoreCase("L")) msg.setPriority(MessagePriorities.LOW);
				else if (rs.getString("priority").equalsIgnoreCase("N")) msg.setPriority(MessagePriorities.NORMAL);
				else if (rs.getString("priority").equalsIgnoreCase("H")) msg.setPriority(MessagePriorities.HIGH);
				msg.setId("" + rs.getString("id"));
				if (rs.getString("encoding").equals("7")) msg.setEncoding(MessageEncodings.ENC7BIT);
				else if (rs.getString("encoding").equals("8")) msg.setEncoding(MessageEncodings.ENC8BIT);
				else msg.setEncoding(MessageEncodings.ENCUCS2);
				if (rs.getInt("status_report") == 1) msg.setStatusReport(true);
				if (rs.getInt("flash_sms") == 1) msg.setFlashSms(true);
				if (rs.getInt("src_port") != -1)
				{
					msg.setSrcPort(rs.getInt("src_port"));
					msg.setDstPort(rs.getInt("dst_port"));
				}
				if (rs.getString("originator") != null) msg.setFrom(rs.getString("originator"));
				rs.updateString("status", "Q"); 
				rs.updateRow();
				con.commit();
				if (props.getProperty("settings.send_mode", "sync").equalsIgnoreCase(("sync")))
				{
					if (!rs.getString("gateway_id").equals("*")) srv.sendMessage(msg, rs.getString("gateway_id"));
					else srv.sendMessage(msg);
					updateOutboundDatabase(msg);
					srv.getLogger().info(">>> SEND TO: " + msg.getGatewayId() + " : " + msg.getRecipient());
				}
				else
				{
					if (!rs.getString("gateway_id").equals("*")) srv.queueMessage(msg, rs.getString("gateway_id"));
					else srv.queueMessage(msg);
					srv.getLogger().info(">>> QUEUE TO: " + msg.getGatewayId() + " : " + msg.getRecipient());
				}
			}
		}
		rs.close();
		cmd.close();
		con.close();
	}

	private void readMessages() throws Exception
	{
		List msgList;

		msgList = new ArrayList();
		srv.readMessages(msgList, MessageClasses.ALL);
		saveToDatabase(msgList);
		if (props.getProperty("settings.delete_after_processing").equalsIgnoreCase("YES"))
		{
			for (int i = 0, n = msgList.size(); i < n; i++)
				srv.deleteMessage((InboundMessage) msgList.get(i));
		}
	}

	private boolean checkTimeFrame(String priority)
	{
		String timeFrame;
		String from, to, current;
		Calendar cal = Calendar.getInstance();

		if (priority.equalsIgnoreCase("L")) timeFrame = props.getProperty("settings.timeframe.low", "0000-2359");
		else if (priority.equalsIgnoreCase("N")) timeFrame = props.getProperty("settings.timeframe.normal", "0000-2359");
		else if (priority.equalsIgnoreCase("H")) timeFrame = props.getProperty("settings.timeframe.high", "0000-2359");
		else timeFrame = "0000-2359";
		from = timeFrame.substring(0, 4);
		to = timeFrame.substring(5, 9);
		cal.setTime(new java.util.Date());
		current = cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + cal.get(Calendar.HOUR_OF_DAY) : "" + cal.get(Calendar.HOUR_OF_DAY);
		current += cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : "" + cal.get(Calendar.MINUTE);
		if ((Integer.parseInt(current) >= Integer.parseInt(from)) && (Integer.parseInt(current) < Integer.parseInt(to))) return true;
		else return false;
	}

	private void execute() throws Exception
	{
		initialize();
		resetQueuedMessages();
		try
		{
			srv.startService();
			while (true)
			{
				srv.getLogger().info("Processing @ " + new java.util.Date());
				try
				{
					if (!shutdown)
					{
						srv.getLogger().debug("** GATEWAY STATISTICS **");
						for (int i = 0; i < srv.getGatewayList().size(); i ++)
						{
							AGateway gtw = (AGateway) srv.getGatewayList().get(i);
							srv.getLogger().debug("Gateway: " + gtw.getGatewayId() + ", Load: " + srv.getGatewayQueueLoad(gtw.getGatewayId()));
						}
						srv.getLogger().debug("** GATEWAY STATISTICS **");
					}
					if (!shutdown) readMessages();
					if (!shutdown) sendMessages();
				}
				catch (Exception e)
				{
					srv.getLogger().error("SMSSvr error!", e);
				}
				Thread.sleep(Integer.parseInt(props.getProperty("settings.poll_interval", "60")) * 1000);
			}
		}
		catch (Exception e)
		{
			srv.stopService();
			e.printStackTrace();
			srv.getLogger().fatal("Fatal error during Service initialization, aborting...");
		}
	}

	private synchronized void updateOutboundDatabase(OutboundMessage msg) throws Exception
	{
		Connection con = null;
		ResultSet rs = null;
		Statement cmd = null;

		con = getDbConnection();
		if (con == null) return;
		cmd = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		rs = cmd.executeQuery("select * from " + props.getProperty("database.tables.sms_out", "smssvr_out") + " where id = " + msg.getId());
		if (rs.next())
		{
			if (msg.getMessageStatus() == MessageStatuses.SENT)
			{
				rs.updateString("status", "S");
				rs.updateTimestamp("sent_date", new Timestamp(msg.getDispatchDate().getTime()));
				rs.updateString("gateway_id", msg.getGatewayId());
			}
			else if (msg.getMessageStatus() == MessageStatuses.FAILED)
			{
				int errors = rs.getInt("errors");
				errors++;
				rs.updateInt("errors", errors);
				if (errors > Integer.parseInt(props.getProperty("settings.retries", "2"))) rs.updateString("status", "F");
				else rs.updateString("status", "U");
			}
			if (msg.getRefNo() != null && msg.getRefNo().length() > 0) rs.updateString("ref_no", msg.getRefNo());
			rs.updateRow();
			con.commit();
			rs.close();
			cmd.close();
			con.close();
		}
	}

	private class OutboundNotification implements IOutboundMessageNotification
	{
		public void process(String gatewayId, OutboundMessage msg)
		{
			try
			{
				updateOutboundDatabase(msg);
			}
			catch (Exception e)
			{
				srv.getLogger().fatal(e);
			}
		}
	}

	private class CallNotification implements ICallNotification
	{
		public void process(String gatewayId, String callerId)
		{
			Connection con = null;
			Statement cmd = null;
			ResultSet rs = null;

			try
			{
				con = getDbConnection();
				if (con == null) return;
				cmd = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs = cmd.executeQuery("select * from " + props.getProperty("database.tables.calls", "smssvr_calls") + " where id = -1");
				rs.moveToInsertRow();
				rs.updateTimestamp("call_date", new Timestamp(new java.util.Date().getTime()));
				rs.updateString("gateway_id", gatewayId);
				rs.updateString("caller_id", callerId);
				rs.insertRow();
				con.commit();
				rs.close();
				cmd.close();
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				try
				{
					if (rs != null) rs.close();
					if (cmd != null) cmd.close();
					if (con != null) con.close();
				}
				catch (Exception e1)
				{
					srv.getLogger().fatal(e1);
				}
			}
		}
	}

	private Connection getDbConnection()
	{
		Connection dbCon = null;

		while (!shutdown)
		{
			try
			{
				dbCon = DriverManager.getConnection(props.getProperty("database.url"), props.getProperty("database.username", ""), props.getProperty("database.password", ""));
				dbCon.setAutoCommit(false);
				break;
			}
			catch (SQLException e)
			{
				srv.getLogger().warn("Database lost, trying to get connection back...");
				try { Thread.sleep(5000); } catch (Exception e1) {}
			}
		}
		return dbCon;
	}

	public static void main(String[] args)
	{
		SMSSvr app = new SMSSvr();

		// This is done just to keep SMSSvr alive in case of fatal errors.
		while (true)
		{
			try
			{
				app.execute();
			}
			catch (FileNotFoundException e)
			{
				System.out.println("Cannot find SMSSvr configuration file!");
				break;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
