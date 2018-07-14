package io.github.blockchain_royale.prototype

import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.servlet.ServletHolder
import javax.servlet.MultipartConfigElement
import org.eclipse.jetty.server.ServerConnector
import javax.xml.crypto.Data
import org.eclipse.jetty.servlet.ServletHandler

object Server {
  val server = new org.eclipse.jetty.server.Server(7341);

  val gameServlet = new ServletHolder(new GameServlet());
  val context = new ServletHandler();
  context.addServlet(gameServlet)

  server.setHandler(context)
  server.start
  System.out.println("Started: 8822");
  server.join
}