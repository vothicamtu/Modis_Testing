package com.modis.constants;

public class TestIDs {

    //  LOADING SCREEN 
    public static final String LOADING_SCREEN = "loading_screen";
    public static final String LOADING_LOGIN_BUTTON = "loading_login_button";
    public static final String LOADING_SIGNUP_BUTTON = "loading_signup_button";
    public static final String LOADING_LOGO = "loading_logo";

    //  LOGIN SCREEN 
    public static final String LOGIN_SCREEN = "login_screen";
    public static final String LOGIN_TITLE_TEXT = "login_title_text";
    public static final String LOGIN_USERNAME_INPUT = "login_username_input";
    public static final String LOGIN_PASSWORD_INPUT = "login_password_input";
    public static final String LOGIN_SUBMIT_BUTTON = "login_submit_button";
    public static final String LOGIN_SIGNUP_LINK = "login_signup_link";
    public static final String LOGIN_FORGOT_PASSWORD = "login_forgot_password";

    //  SIGNUP SCREEN 
    public static final String SIGNUP_SCREEN = "signup_screen";
    public static final String SIGNUP_TITLE_TEXT = "signup_title_text";
    public static final String SIGNUP_USERNAME_INPUT = "signup_username_input";
    public static final String SIGNUP_FULLNAME_INPUT = "signup_fullname_input";
    public static final String SIGNUP_EMAIL_INPUT = "signup_email_input";
    public static final String SIGNUP_PHONE_INPUT = "signup_phone_input";
    public static final String SIGNUP_PASSWORD_INPUT = "signup_password_input";
    public static final String SIGNUP_CONFIRM_PASSWORD_INPUT = "signup_confirm_password_input";
    public static final String SIGNUP_SUBMIT_BUTTON = "signup_submit_button";
    public static final String SIGNUP_LOGIN_LINK = "signup_login_link";
    public static final String SIGNUP_BACK_BUTTON = "signup_back_button";

    //  HOME SCREEN 
    public static final String HOME_SCREEN = "home_screen";
    public static final String HOME_GESTURE_CONTAINER = "home_gesture_container";

    //  TAKE SCREEN (CAMERA) 
    public static final String TAKE_SCREEN = "take_screen";
    public static final String TAKE_CAMERA_AREA = "take_camera_area";
    public static final String TAKE_CAPTURE_BUTTON = "take_capture_button";
    public static final String TAKE_FLASH_BUTTON = "take_flash_button";
    public static final String TAKE_TOGGLE_CAMERA_BUTTON = "take_toggle_camera_button";
    public static final String TAKE_HISTORY = "take_history";

    //  SEND PHOTO SCREEN 
    public static final String SEND_PHOTO_SCREEN = "send_photo_screen";
    public static final String SEND_PHOTO_PREVIEW_IMAGE = "send_photo_preview_image";
    public static final String SEND_PHOTO_CAPTION_INPUT = "send_photo_caption_input";
    public static final String SEND_PHOTO_SEND_BUTTON = "send_photo_send_button";
    public static final String SEND_PHOTO_CLOSE_BUTTON = "send_photo_close_button";
    public static final String SEND_PHOTO_FRIEND_PREFIX = "send_photo_friend_"; // + userId
    public static final String SEND_PHOTO_SELECT_ALL = "send_photo_all_friends_button";
    public static final String SEND_PHOTO_FRIENDS_LIST = "send_photo_friends_flatlist";

    //  FEED SCREEN (React Emoji Comment) 
    // React Native hiện tại dùng id root cho feed là "home_feed_screen"
    public static final String FEED_SCREEN = "home_feed_screen";
    public static final String FEED_POST_ITEM_PREFIX = "feed_post_item_"; // + postId
    public static final String FEED_POST_IMAGE_PREFIX = "feed_post_image_"; // + postId
    public static final String FEED_COMMENT_BUTTON_PREFIX = "feed_comment_button_"; // + postId
    public static final String FEED_EMOJI_PREFIX = "feed_emoji_"; // + emoji + _ + postId
    // Feed UI dùng FlatList với testID="feed_flatlist"
    public static final String FEED_SCROLL_VIEW = "feed_flatlist";

    //  FRIENDS SCREEN 
    public static final String FRIENDS_SCREEN = "friends_screen";
    public static final String FRIENDS_BACK_BUTTON = "friends_back_button";
    public static final String FRIENDS_SCROLL = "friends_scroll";
    public static final String FRIENDS_SEARCH_INPUT = "friends_search_input";
    public static final String FRIENDS_CLEAR_SEARCH = "friends_search_input_clear_button";
    public static final String FRIENDS_LIST_EMPTY = "friends-list-empty";
    public static final String FRIENDS_LIST = "friends-list-flatlist";
    public static final String FRIEND_ITEM_PREFIX = "friend-item-"; // + friendReqId
    public static final String FRIEND_AVATAR_PREFIX = "friend-avatar-"; // + friendReqId
    public static final String FRIEND_NAME_PREFIX = "friend-name-"; // + friendReqId
    public static final String FRIEND_UNFRIEND_PREFIX = "friend-unfriend-"; // + friendReqId
    public static final String FRIEND_REQUEST_ITEM_PREFIX = "friend_request_item_"; // + normalized name
    public static final String FRIEND_REQUEST_AVATAR_SUFFIX = "_avatar";
    public static final String FRIEND_REQUEST_NAME_SUFFIX = "_name";
    public static final String FRIEND_REQUEST_ACCEPT_SUFFIX = "_accept_button";
    public static final String FRIEND_REQUEST_REJECT_SUFFIX = "_reject_button";

    //  PROFILE SCREEN 
    public static final String PROFILE_SCREEN = "profile_screen";
    public static final String PROFILE_BACK_BUTTON = "profile_back_button";
    public static final String PROFILE_AVATAR = "profile_avatar";
    public static final String PROFILE_USERNAME = "profile_username";
    public static final String PROFILE_EDIT_USERNAME_ITEM = "profile_edit_username_item";
    public static final String PROFILE_EDIT_PHONE_ITEM = "profile_edit_phone_item";
    public static final String PROFILE_EDIT_EMAIL_ITEM = "profile_edit_email_item";
    public static final String PROFILE_CHANGE_PASSWORD_ITEM = "profile_change_password_item";
    public static final String PROFILE_THEME_TOGGLE = "profile_theme_toggle";
    public static final String PROFILE_DELETE_ACCOUNT = "profile_delete_account";
    public static final String PROFILE_LOGOUT_BUTTON = "profile_logout_item";
    public static final String PROFILE_SHARE_APP = "profile_share_app";

    //  MESSAGE SCREEN 
    public static final String MESSAGE_SCREEN = "message_screen";
    public static final String MESSAGE_BACK_BUTTON = "messages-header-back-button";
    public static final String MESSAGE_CONVERSATION_LIST = "messages_list";
    public static final String MESSAGE_CONVERSATION_ITEM_PREFIX = "message_conversation_item_"; // + conversationId
    public static final String MESSAGE_CONVERSATION_LAST_MESSAGE_SUFFIX = "_last_message";
    public static final String MESSAGE_EMPTY_STATE = "messages_list_empty";

    //  CONVERSATION SCREEN 
    public static final String CONVERSATION_SCREEN = "conversation_screen";
    public static final String CONVERSATION_BACK_BUTTON = "conversation_back_button";
    public static final String CONVERSATION_HEADER = "conversation_header";
    public static final String CONVERSATION_MESSAGES_LIST = "conversation_messages_list";
    public static final String CONVERSATION_INPUT = "chat_input";
    public static final String CONVERSATION_SEND_BUTTON = "chat_send_button";
    public static final String CONVERSATION_MESSAGE_PREFIX = "conversation_message_"; // + messageId

    //  ALL IMAGES SCREEN 
    public static final String ALL_IMAGES_SCREEN = "all_images_screen";
    public static final String ALL_IMAGES_BACK_BUTTON = "all_images_back_button";
    public static final String ALL_IMAGES_GRID = "all_images_flatlist";
    public static final String ALL_IMAGES_ITEM_PREFIX = "all_images_grid_item_"; // + index
    public static final String ALL_IMAGES_EMPTY_STATE = "all_images_empty_state";

    //  TOP BAR COMPONENT 
    public static final String TOPBAR_CONTAINER = "topbar_container";
    public static final String TOPBAR_AVATAR_BUTTON = "topbar_avatar_button";
    public static final String TOPBAR_FRIENDS_BUTTON = "topbar_friends_button";
    public static final String TOPBAR_MESSAGE_BUTTON = "topbar_message_button";
    public static final String TOPBAR_FILTER_BUTTON = "topbar_filter_button";
    public static final String TOPBAR_TITLE = "topbar_title";

    //  BOTTOM BAR COMPONENT 
    public static final String BOTTOMBAR_CONTAINER = "bottombar_container";
    public static final String BOTTOMBAR_HOME_BUTTON = "bottombar_home_button";

    //  MODALS AND DIALOGS 
    public static final String MODAL_CONTAINER = "modal_container";
    public static final String MODAL_CLOSE_BUTTON = "modal_close_button";
    public static final String MODAL_CONFIRM_BUTTON = "modal_confirm_button";
    public static final String MODAL_CANCEL_BUTTON = "modal_cancel_button";
    public static final String ALERT_DIALOG = "alert_dialog";
    public static final String ALERT_OK_BUTTON = "alert_ok_button";
    public static final String ALERT_CANCEL_BUTTON = "alert_cancel_button";
    public static final String ERROR_DIALOG = "error_dialog";
    public static final String ERROR_DIALOG_TITLE = "error_dialog_title";
    public static final String ERROR_DIALOG_MESSAGE = "error_dialog_message";
    public static final String ERROR_DIALOG_OK_BUTTON = "error_dialog_ok_button";
    public static final String LOGIN_ERROR_DIALOG = "login_error_dialog";
    public static final String LOGIN_ERROR_OK_BUTTON = "login_error_ok_button";
    public static final String AUTH_DIALOG_CONTAINER = "auth_dialog_container";
    public static final String AUTH_DIALOG_TITLE = "auth_dialog_title";
    public static final String AUTH_DIALOG_MESSAGE = "auth_dialog_message";
    public static final String AUTH_DIALOG_OK_BUTTON = "auth_dialog_ok_button";

    //  LOADING AND PROGRESS 
    public static final String LOADING_SPINNER = "loading_spinner";
    public static final String PROGRESS_BAR = "progress_bar";
    public static final String REFRESH_INDICATOR = "refresh_indicator";

    //  SEARCH COMPONENTS 
    public static final String SEARCH_RESULTS_LIST = "search_results_list";
    public static final String SEARCH_RESULT_ITEM_PREFIX = "search_result_item_"; // + userId
    public static final String SEARCH_RESULT_ADD_BUTTON_PREFIX = "search_result_add_button_"; // + userId
    public static final String SEARCH_EMPTY_STATE = "search_empty_state";

    //  EMOJI PICKER 
    public static final String EMOJI_PICKER = "emoji_picker";
    public static final String EMOJI_BUTTON_PREFIX = "emoji_button_"; // + emoji
    public static final String EMOJI_CATEGORY_PREFIX = "emoji_category_"; // + category

    //  TOAST NOTIFICATIONS 
    public static final String TOAST_CONTAINER = "toast_container";
    public static final String TOAST_MESSAGE = "toast_message";
    public static final String TOAST_SUCCESS = "toast_success";
    public static final String TOAST_ERROR = "toast_error";
    public static final String TOAST_WARNING = "toast_warning";

    //  FORM VALIDATION 
    public static final String VALIDATION_ERROR_TEXT = "validation_error_text";
    public static final String FIELD_ERROR_PREFIX = "field_error_"; // + fieldName

    //  ACCESSIBILITY LABELS 
    public static final String ACCESSIBILITY_LABEL_LOGIN = "Màn hình đăng nhập";
    public static final String ACCESSIBILITY_LABEL_SIGNUP = "Màn hình đăng ký";
    public static final String ACCESSIBILITY_LABEL_HOME = "Màn hình chính";
    public static final String ACCESSIBILITY_LABEL_CAMERA = "Màn hình chụp ảnh";
    public static final String ACCESSIBILITY_LABEL_PROFILE = "Màn hình hồ sơ";
    public static final String ACCESSIBILITY_LABEL_FRIENDS = "Màn hình bạn bè";
    public static final String ACCESSIBILITY_LABEL_MESSAGES = "Màn hình tin nhắn";

    //  DYNAMIC ID BUILDERS
    public static String getFeedPostItemId(String postId) {
        return FEED_POST_ITEM_PREFIX + postId;
    }

    public static String getFeedPostImageId(String postId) {
        return FEED_POST_IMAGE_PREFIX + postId;
    }

    public static String getFeedEmojiId(String emoji, String postId) {
        return FEED_EMOJI_PREFIX + emoji + "_" + postId;
    }

    public static String getSendPhotoFriendId(String userId) {
        return SEND_PHOTO_FRIEND_PREFIX + userId;
    }

    public static String getFriendItemId(String friendReqId) {
        return FRIEND_ITEM_PREFIX + friendReqId;
    }

    public static String getFriendAvatarId(String friendReqId) {
        return FRIEND_AVATAR_PREFIX + friendReqId;
    }

    public static String getFriendNameId(String friendReqId) {
        return FRIEND_NAME_PREFIX + friendReqId;
    }

    public static String getFriendRequestItemId(String requestId) {
        return FRIEND_REQUEST_ITEM_PREFIX + requestId;
    }

    public static String getFriendRequestAvatarId(String requestId) {
        return getFriendRequestItemId(requestId) + FRIEND_REQUEST_AVATAR_SUFFIX;
    }

    public static String getFriendRequestNameId(String requestId) {
        return getFriendRequestItemId(requestId) + FRIEND_REQUEST_NAME_SUFFIX;
    }

    public static String getFriendRequestAcceptButtonId(String requestId) {
        return getFriendRequestItemId(requestId) + FRIEND_REQUEST_ACCEPT_SUFFIX;
    }

    public static String getFriendRequestRejectButtonId(String requestId) {
        return getFriendRequestItemId(requestId) + FRIEND_REQUEST_REJECT_SUFFIX;
    }

    public static String getSearchResultItemId(String userId) {
        return SEARCH_RESULT_ITEM_PREFIX + userId;
    }

    public static String getSearchResultAddButtonId(String userId) {
        return SEARCH_RESULT_ADD_BUTTON_PREFIX + userId;
    }

    // Private constructor to prevent instantiation
    private TestIDs() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
