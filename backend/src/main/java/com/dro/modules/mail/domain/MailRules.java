package com.dro.modules.mail.domain;

public final class MailRules {

    public static final int SUBJECT_MAX_LENGTH = 80;
    public static final int BODY_MAX_LENGTH = 1000;
    public static final int MAX_PAGE_SIZE = 50;
    public static final int MAX_MESSAGES_PER_MINUTE = 10;
    public static final int SOURCE_TYPE_MAX_LENGTH = 64;
    public static final int ACTION_TYPE_MAX_LENGTH = 64;
    public static final int DELIVERY_KEY_MAX_LENGTH = 128;

    private MailRules() {
    }
}
