/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mock.smtp.server;

import java.util.Objects;

import com.google.common.base.Preconditions;

class Response {
    enum SMTPStatusCode {
        OK_200(200),
        SYSTEM_STATUS_211(211),
        HELP_214(214),
        SERVICE_READY(220),
        SERVICE_CLOSING_CHANNEL_221(221),
        ACTION_COMPLETE_250(250),
        USER_NOT_LOCAL_251(251),
        UNKNOW_USER_252(252),
        START_MAIL_INPUT_354(354),
        SERVICE_NOT_AVAILABLE_421(421),
        REQUESTED_MAIL_ACTION_NOT_TAKEN_450(450),
        REQUESTED_ACTION_ABORTED_451(451),
        REQUESTED_ACTION_NOT_TAKEN_452(452),
        SYNTAX_ERROR_500(500),
        SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS_501(501),
        COMMAND_NOT_IMPLEMENTED_502(502),
        BAD_SEQUENCE_OF_COMMANDS_503(503),
        COMMAND_PARAMETER_NOT_IMPLEMENTED_504(504),
        DOES_NOT_ACCEPT_MAIL_521(521),
        ACCESS_DENIED_530(530),
        REQUESTED_ACTION_NOT_TAKEN_550(550),
        USER_NOT_LOCAL_551(551),
        REQUESTED_MAIL_ACTION_ABORTED_552(552),
        REQUESTED_ACTION_NOT_TAKEN_553(553),
        TRANSACTION_FAILED_554(554);

        private final int code;

        private SMTPStatusCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public static Response serverReject(SMTPStatusCode code, String message) {
        return new Response(code, message, true);
    }

    public static Response serverAccept(SMTPStatusCode code, String message) {
        return new Response(code, message, false);
    }

    private final SMTPStatusCode code;
    private final String message;
    private final boolean serverRejected;

    private Response(SMTPStatusCode code, String message, boolean serverRejected) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(code);

        this.code = code;
        this.message = message;
        this.serverRejected = serverRejected;
    }

    String asReplyString() {
        return code.getCode() + " " + message;
    }

    boolean isServerRejected() {
        return serverRejected;
    }

    @Override
    public final boolean equals(Object o) {
        if (o instanceof Response) {
            Response response = (Response) o;

            return Objects.equals(this.serverRejected, response.serverRejected)
                && Objects.equals(this.code, response.code)
                && Objects.equals(this.message, response.message);
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(code, message, serverRejected);
    }
}
