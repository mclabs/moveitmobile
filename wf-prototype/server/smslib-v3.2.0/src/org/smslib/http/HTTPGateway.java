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

package org.smslib.http;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import org.smslib.*;

class HTTPGateway extends AGateway
{
	public HTTPGateway(String id, Service srv)
	{
		super(id, srv);
	}

	static List HttpPost(URL url, List requestList) throws IOException
	{
		List responseList = new ArrayList();
		URLConnection con;
		BufferedReader in;
		OutputStreamWriter out;
		StringBuffer req;
		String line;

		con = url.openConnection();
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		out = new OutputStreamWriter(con.getOutputStream());
		req = new StringBuffer();
		for (int i = 0, n = requestList.size(); i < n; i++)
		{
			if (i != 0) req.append("&");
			req.append(((HttpHeader) requestList.get(i)).key);
			req.append("=");
			if (((HttpHeader) requestList.get(i)).unicode)
			{
				StringBuffer tmp = new StringBuffer(200);
				byte[] uniBytes = ((HttpHeader) requestList.get(i)).value.getBytes("UnicodeBigUnmarked");
				for (int j = 0; j < uniBytes.length; j++)
					tmp.append(Integer.toHexString(uniBytes[j]).length() == 1 ? "0" + Integer.toHexString(uniBytes[j]) : Integer.toHexString(uniBytes[j]));
				req.append(tmp.toString().replaceAll("ff", ""));
			}
			else req.append(((HttpHeader) requestList.get(i)).value);
		}
		out.write(req.toString());
		out.flush();
		out.close();
		in = new BufferedReader(new InputStreamReader((con.getInputStream())));
		while ((line = in.readLine()) != null)
			responseList.add(line);
		in.close();
		return responseList;
	}

	static List HttpGet(URL url) throws IOException
	{
		List responseList = new ArrayList();

		URLConnection con = url.openConnection();
		con.setAllowUserInteraction(false);
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			responseList.add(inputLine);
		in.close();
		return responseList;
	}

	static class HttpHeader
	{
		public String key;
		public String value;
		public boolean unicode;

		public HttpHeader()
		{
			key = "";
			value = "";
			unicode = false;
		}

		public HttpHeader(String key, String value, boolean unicode)
		{
			this.key = key;
			this.value = value;
			this.unicode = unicode;
			/*
			 * this.key = URLEncoder.encode(key, "UTF-8");
			 * if (unicode) this.value = URLEncoder.encode(value, "UTF-16").replaceAll("%","").substring(4);
			 * else this.value = URLEncoder.encode(value,"UTF-8");
			 */
		}
	}

	String calculateMD5(String in)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] pre_md5 = md.digest(in.getBytes("LATIN1"));
			String md5 = "";
			for (int i = 0; i < 16; i++)
			{
				if (pre_md5[i] < 0)
				{
					md5 += Integer.toHexString(256 + pre_md5[i]);
				}
				else if (pre_md5[i] > 15)
				{
					md5 += Integer.toHexString(pre_md5[i]);
				}
				else
				{
					md5 += "0" + Integer.toHexString(pre_md5[i]);
				}
			}
			return md5;
		}
		catch (UnsupportedEncodingException ex)
		{
			logError("Unsupported encoding.", ex);
			return "";
		}
		catch (NoSuchAlgorithmException ex)
		{
			logError("No such algorithm.", ex);
			return "";
		}
	}
}
