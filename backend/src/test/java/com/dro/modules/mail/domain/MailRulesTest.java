package com.dro.modules.mail.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MailRulesTest {

    @Test
    void subjectAndBodyLimits_areSuitableForTextOnlyMail() {
        assertEquals(80, MailRules.SUBJECT_MAX_LENGTH);
        assertEquals(1000, MailRules.BODY_MAX_LENGTH);
    }

    @Test
    void pageSize_isCappedToProtectTheInboxEndpoint() {
        assertEquals(50, MailRules.MAX_PAGE_SIZE);
    }

    @Test
    void messagesPerMinute_limitPreventsBasicSpam() {
        assertEquals(10, MailRules.MAX_MESSAGES_PER_MINUTE);
    }

    @Test
    void systemMessageMetadata_limitsMatchDatabaseColumns() {
        assertEquals(64, MailRules.SOURCE_TYPE_MAX_LENGTH);
        assertEquals(64, MailRules.ACTION_TYPE_MAX_LENGTH);
        assertEquals(128, MailRules.DELIVERY_KEY_MAX_LENGTH);
    }
}
