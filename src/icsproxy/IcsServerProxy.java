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

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IcsServerProxy {
	// this would make 40 threads: 1 read, 1 write per user.
	public int SERVER_MAX_CONNECTIONS = 20;
	private List<UserSession> connections;
	private ServerSocket serverSocket;
	private IcsInfo serverInfo;
	
	//
	// Constructors
	//
	
	public IcsServerProxy(int portNumber) throws Exception {
		this.serverSocket = new ServerSocket(portNumber);
		this.connections = new ArrayList<UserSession>();
	}
	
	//
	// Connection / Socket methods
	//
	
	public void initConnection(Socket socket) throws Exception {
		MinimalServerConnection serverConnection = new MinimalServerConnection();
		serverConnection.openConnection(serverInfo.getAddress(), serverInfo.getPort());
		
		UserSession userSession = new UserSession();
		userSession.setSocket(socket);
		userSession.setServerConnection(serverConnection);
		this.addConnection(userSession);
		
		// hate having to add 2 threads per user though...
		this.addReadThread(userSession);
		this.addWriteThread(userSession);
	}
	
	protected byte[] readBytes(Socket socket) throws Exception {
		byte[] bytes = new byte[1600];
		int bytesRead = socket.getInputStream().read(bytes);
		return Arrays.copyOf(bytes, bytesRead);
	}
	
	protected boolean writeToSocket(Socket socket,byte[] bytes) throws Exception {
		if (!socket.isClosed() && !socket.isOutputShutdown()) {
			socket.getOutputStream().write(bytes);
			return true;
		}
		return false;
	}
	
	protected void addReadThread(final UserSession userSession) {
		new Thread(new Runnable() {
			public void run() {
				Socket socket = userSession.getSocket();
				while (!socket.isClosed()) {
					try {
						MinimalServerConnection conn = userSession.getServerConnection();
						byte[] bytes = readBytes(socket);
						System.out.println(new String(bytes));
						System.out.println(Arrays.toString(bytes));
						conn.writeBytes(bytes);
					} catch (Exception e) {
						e.printStackTrace(System.err);
						break;
					}
				}
				
				try {
					userSession.getSocket().close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
				IcsServerProxy.this.removeConnection(userSession);
				System.out.println("Thread ending");
			}
		}).start();
	}
	
	protected void addWriteThread(final UserSession userSession) {
		new Thread(new Runnable() {
			public void run() {
				Socket socket = userSession.getSocket();
				while(!socket.isClosed()) {
					try {
						MinimalServerConnection conn = userSession.getServerConnection();
						writeToSocket(socket,conn.read());
					} catch (Exception e) {
						e.printStackTrace(System.err);
						break;
					}
				}
				
				try {
					userSession.getSocket().close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
				IcsServerProxy.this.removeConnection(userSession);
				System.out.println("Thread ending");
			}
		}).start();
	}
	
	//
	// Misc Utils
	//
	
	public void printActiveConnections() {
		System.out.println(String.format("%-32s", "Session Id"));
		if (connections.size() > 0) {
			for(UserSession sess : connections) {
				System.out.println(String.format("%-32s",sess.getSessionIdentifier()));
			}
		} else {
			System.out.println(String.format("%-32s","(none)"));
		}
	}
	
	//
	// Getters / Setters
	//
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	public void addConnection(UserSession userSession) {
		connections.add(userSession);
	}
	
	public void removeConnection(UserSession userSession) {
		connections.remove(userSession);
	}
	
	public void setServerInfo(IcsInfo serverInfo) {
		this.serverInfo = serverInfo;
	}
}
