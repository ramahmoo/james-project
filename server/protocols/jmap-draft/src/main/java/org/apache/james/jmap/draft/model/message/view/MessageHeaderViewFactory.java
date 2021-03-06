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

package org.apache.james.jmap.draft.model.message.view;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.james.jmap.draft.model.BlobId;
import org.apache.james.jmap.draft.model.Emailer;
import org.apache.james.mailbox.BlobManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageIdManager;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.FetchGroup;
import org.apache.james.mailbox.model.MailboxId;
import org.apache.james.mailbox.model.MessageId;
import org.apache.james.mailbox.model.MessageResult;
import org.apache.james.mime4j.dom.Message;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

public class MessageHeaderViewFactory implements MessageViewFactory<MessageHeaderView> {
    private final BlobManager blobManager;
    private final MessageIdManager messageIdManager;

    @Inject
    @VisibleForTesting
    public MessageHeaderViewFactory(BlobManager blobManager, MessageIdManager messageIdManager) {
        this.blobManager = blobManager;
        this.messageIdManager = messageIdManager;
    }

    @Override
    public List<MessageHeaderView> fromMessageIds(List<MessageId> messageIds, MailboxSession mailboxSession) throws MailboxException {
        List<MessageResult> messages = messageIdManager.getMessages(messageIds, FetchGroup.HEADERS, mailboxSession);
        return Helpers.toMessageViews(messages, this::fromMessageResults);
    }

    private MessageHeaderView fromMessageResults(Collection<MessageResult> messageResults) throws MailboxException, IOException {
        Helpers.assertOneMessageId(messageResults);

        MessageResult firstMessageResult = messageResults.iterator().next();
        List<MailboxId> mailboxIds = Helpers.getMailboxIds(messageResults);

        Message mimeMessage = Helpers.parse(firstMessageResult.getFullContent().getInputStream());

        return MessageHeaderView.messageHeaderBuilder()
            .id(firstMessageResult.getMessageId())
            .mailboxIds(mailboxIds)
            .blobId(BlobId.of(blobManager.toBlobId(firstMessageResult.getMessageId())))
            .threadId(firstMessageResult.getMessageId().serialize())
            .keywords(Helpers.getKeywords(messageResults))
            .size(firstMessageResult.getSize())
            .inReplyToMessageId(Helpers.getHeaderValue(mimeMessage, "in-reply-to"))
            .subject(Strings.nullToEmpty(mimeMessage.getSubject()).trim())
            .headers(Helpers.toHeaderMap(mimeMessage.getHeader().getFields()))
            .from(Emailer.firstFromMailboxList(mimeMessage.getFrom()))
            .to(Emailer.fromAddressList(mimeMessage.getTo()))
            .cc(Emailer.fromAddressList(mimeMessage.getCc()))
            .bcc(Emailer.fromAddressList(mimeMessage.getBcc()))
            .replyTo(Emailer.fromAddressList(mimeMessage.getReplyTo()))
            .date(Helpers.getDateFromHeaderOrInternalDateOtherwise(mimeMessage, firstMessageResult))
            .build();
    }
}
