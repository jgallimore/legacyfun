/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable 
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.daytrader.web.prims;

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.daytrader.TradeConfig;
import org.daytrader.util.Log;

/**
 * This primitive is designed to run inside the TradeApplication and relies upon
 * the {@link org.daytrader.TradeConfig} class to set configuration parameters.
 * PingServlet2MDBQueue tests key functionality of a servlet call to a
 * post a message to an MDB Queue. The TradeBrokerMDB receives the message
 * This servlet makes use of the MDB EJB {@link org.daytrader.ejb.TradeBrokerMDB}
 * by posting a message to the MDB Queue
 */
public class PingServlet2MDBQueue extends HttpServlet {

    private static String initTime;
    private static int hitCount;
    private static ConnectionFactory connFactory;
    private static Queue queue;

    /**
     * forwards post requests to the doGet method
     * Creation date: (11/6/2000 10:52:39 AM)
     *
     * @param req javax.servlet.http.HttpServletRequest
     * @param res javax.servlet.http.HttpServletResponse
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        doGet(req, res);
    }


    /**
     * this is the main method of the servlet that will service all get requests.
     *
     * @param req HttpServletRequest
     * @param res HttpServletResponce
     */

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        res.setContentType("text/html");
        java.io.PrintWriter out = res.getWriter();
        //use a stringbuffer to avoid concatenation of Strings
        StringBuffer output = new StringBuffer(100);
        output.append(
                "<html><head><title>PingServlet2MDBQueue</title></head>"
                        + "<body><HR><FONT size=\"+2\" color=\"#000066\">PingServlet2MDBQueue<BR></FONT>"
                        + "<FONT size=\"-1\" color=\"#000066\">"
                        + "Tests the basic operation of a servlet posting a message to an EJB MDB through a JMS Queue.");

        try {

            Connection conn = connFactory.createConnection();

            try {
                TextMessage message = null;
                int iter = TradeConfig.getPrimIterations();
                for (int ii = 0; ii < iter; ii++) {
                    Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    try {
                        MessageProducer producer = sess.createProducer(queue);

                        message = sess.createTextMessage();

                        String command = "ping";
                        message.setStringProperty("command", command);
                        message.setLongProperty("publishTime", System.currentTimeMillis());
                        message.setText("Ping message for queue java:comp/env/jms/TradeBrokerQueue sent from PingServlet2MDBQueue at " + new java.util.Date());
                        producer.send(message);
                    } finally {
                        sess.close();
                    }
                }

                //write out the output
                output.append("<HR>initTime: ").append(initTime);
                output.append("<BR>Hit Count: ").append(hitCount++);
                output.append("<HR>Posted Text message to java:comp/env/jms/TradeBrokerQueue destination");
                output.append("<BR>Message: ").append(message);
                output.append("<BR><BR>Message text: ").append(message.getText());
                output.append(
                        "<BR><HR></FONT></BODY></HTML>");
                out.println(output.toString());

            }
            catch (Exception e) {
                Log.error("PingServlet2MDBQueue.doGet(...):exception posting message to TradeBrokerQueue destination ");
                throw e;
            } finally {
                conn.close();
            }
        } //this is where I actually handle the exceptions
        catch (Exception e) {
            Log.error(e, "PingServlet2MDBQueue.doGet(...): error");
            res.sendError(500, "PingServlet2MDBQueue.doGet(...): error, " + e.toString());

        }
    }


    /**
     * returns a string of information about the servlet
     *
     * @return info String: contains info about the servlet
     */

    public String getServletInfo() {
        return "web primitive, configured with trade runtime configs, tests Servlet to Session EJB path";

    }

    /**
     * called when the class is loaded to initialize the servlet
     *
     * @param config ServletConfig:
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        hitCount = 0;
        initTime = new java.util.Date().toString();
        try {
            InitialContext context = new InitialContext();
            connFactory = (ConnectionFactory) context.lookup("java:comp/env/jms/QueueConnectionFactory");
            queue = (Queue) context.lookup("java:comp/env/jms/TradeBrokerQueue");
        } catch (NamingException e) {
            Log.error("PingServlet2MDBQueue:init() -- error on intialization of JMS factories, queues", e);
            throw new ServletException(e);
        }
    }

}