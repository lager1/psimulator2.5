//License
/***
 * Java TelnetD library (embeddable telnet daemon)
 * Copyright (c) 2000-2005 Dieter Wimberger
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ***/

package telnetd.net;

import java.net.InetAddress;
import java.util.Properties;

/**
 * Interface defining a generic IP level connection
 * filter.<br>
 * Due to the fact that this task depends heavily on
 * application context, I chose a very generic way
 * of applying IP level connection filtering.
 * <br><br>
 * Implementations should consider following issues:
 * <ul>
 * <li>performance
 * <li>administration (maybe via an admin shell)
 * <li>logging denials
 * </ul>
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 */
public interface ConnectionFilter {

  /**
   * Initializes this <tt>ConnectionFilter</tt>.
   *
   * @param props the properties of the listener that instantiated
   *              this <tt>ConnectionFilter</tt>.
   */
  public void initialize(Properties props);

  /**
   * Tests if a given ip address is allowed to connect.
   *
   * @param ip the address to be tested.
   * @return true if allowed to connect, false otherwise.
   */
  public boolean isAllowed(InetAddress ip);

}//interface ConnectionFilter