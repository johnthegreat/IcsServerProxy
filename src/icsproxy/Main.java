/*
 * MIT License
 * 
 * Copyright (c) 2017 John Nahlen (john.nahlen@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package icsproxy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class Main {
	private static int serverPort;
	private static IcsServerProxy server;
	private static IcsInfo icsInfo;
	
	public static void main(String[] args) throws Exception {
		String host = "freechess.org";
		int port = 23;
		
		if (args.length == 3) {
			try {
				serverPort = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				serverPort = 23;
			}
			
			host = args[1];
			try {
				port = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				port = 23;
			}
		} else {
			System.out.println("usage: myport host port");
			System.exit(0);
		}
		
		System.out.println("Welcome to Ics Server Proxy 1.0\r\n" +
				"Copyright (c) 2013, 2017 John Nahlen (john.nahlen@gmail.com)\r\n");
		
		icsInfo = new IcsInfo(host, port);
		setupServer();
		setupInputCommands();
	}
	
	private static void setupInputCommands() {
		final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						String command = bufferedReader.readLine();
						if (command.equals("Show Connections")) {
							server.printActiveConnections();
						}
					} catch (Exception e) {
						e.printStackTrace(System.err);
						break;
					}
				}
			}
		}).start();
	}
	
	private static void setupServer() throws Exception {
		server = new IcsServerProxy(serverPort);
		System.out.println("Server socket listener initialized on port " + serverPort);
		
		server.setServerInfo(icsInfo);
		new Thread(new Runnable() {
			public void run() {
				// server.connections.size() < server.SERVER_MAX_CONNECTIONS
				while (true) {
					try {
						final Socket socket = server.getServerSocket().accept();
						System.out.println("Client accepted: " + socket.getInetAddress().toString());
						
						new Thread(new Runnable() {
							public void run() {
								try {
									server.initConnection(socket);
								} catch(Exception e) {
									e.printStackTrace(System.err);
								}
							}
						}).start();
					}
					catch (Exception e) {
						e.printStackTrace(System.err);
					}
				}
			}
		}).start();
	}
}
