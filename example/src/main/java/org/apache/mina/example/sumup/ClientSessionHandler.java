/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.mina.example.sumup;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionLogger;
import org.apache.mina.example.sumup.message.AddMessage;
import org.apache.mina.example.sumup.message.ResultMessage;

/**
 * {@link IoHandler} for SumUp client.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class ClientSessionHandler extends IoHandlerAdapter {
    private final int[] values;

    private boolean finished;

    public ClientSessionHandler(int[] values) {
        this.values = values;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public void sessionOpened(IoSession session) {
        // send summation requests
        for (int i = 0; i < values.length; i++) {
            AddMessage m = new AddMessage();
            m.setSequence(i);
            m.setValue(values[i]);
            session.write(m);
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        // server only sends ResultMessage. otherwise, we will have to identify
        // its type using instanceof operator.
        ResultMessage rm = (ResultMessage) message;
        if (rm.isOk()) {
            // server returned OK code.
            // if received the result message which has the last sequence
            // number,
            // it is time to disconnect.
            if (rm.getSequence() == values.length - 1) {
                // print the sum and disconnect.
                IoSessionLogger.info(session, "The sum: " + rm.getValue());
                session.close();
                finished = true;
            }
        } else {
            // seever returned error code because of overflow, etc.
            IoSessionLogger.warn(session, "Server error, disconnecting...");
            session.close();
            finished = true;
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        session.close();
    }
}